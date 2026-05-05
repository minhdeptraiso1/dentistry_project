package com.project.base_v1.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.project.base_v1.dto.request.consultation.QuickConsultAiAnalysis;
import com.project.base_v1.dto.request.consultation.QuickConsultAiFinalReply;
import com.project.base_v1.dto.request.consultation.QuickConsultRequest;
import com.project.base_v1.dto.response.consultation.QuickConsultResponse;
import com.project.base_v1.dto.response.consultation.SuggestedDoctorResponse;
import com.project.base_v1.dto.response.consultation.SuggestedServiceResponse;
import com.project.base_v1.entity.PublicContent;
import com.project.base_v1.enums.PublicContentType;
import com.project.base_v1.repository.PublicContentRepository;
import com.project.base_v1.repository.spec.PublicContentSpecification;
import com.project.base_v1.service.QuickConsultService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class QuickConsultServiceImpl implements QuickConsultService {

    private static final String DISCLAIMER =
            "Thông tin chỉ mang tính gợi ý, bác sĩ sẽ tư vấn chính xác sau khi thăm khám.";

    private final PublicContentRepository publicContentRepository;
    private final QuickConsultAiPromptBuilder aiPromptBuilder;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public QuickConsultResponse consult(QuickConsultRequest request) {
        String message = normalize(request.message());

        try {
            QuickConsultAiAnalysis analysis = analyzeMessageByAi(request, message);

            List<String> serviceKeywords = mergeKeywords(
                    analysis.serviceKeywords(),
                    expandKeywords(message)
            );

            List<String> doctorKeywords = mergeKeywords(
                    analysis.doctorKeywords(),
                    serviceKeywords
            );

            List<PublicContent> serviceContents =
                    searchManyKeywords(PublicContentType.SERVICE, serviceKeywords, 5);

            List<PublicContent> doctorContents =
                    searchManyKeywords(PublicContentType.DOCTOR, doctorKeywords, 5);

            QuickConsultAiFinalReply finalReply =
                    buildFinalReplyByAi(message, analysis, serviceContents, doctorContents);

            return mapAiFinalReplyToResponse(
                    finalReply,
                    serviceContents,
                    doctorContents
            );

        } catch (Exception ex) {
            return consultByKeywordFallback(message);
        }
    }

    private QuickConsultAiAnalysis analyzeMessageByAi(QuickConsultRequest request, String message) throws Exception {
        String prompt = aiPromptBuilder.buildAnalyzePrompt(message, request.conversation());

        String aiRaw = chatClientBuilder.build()
                .prompt()
                .system("""
                        Bạn là bộ phân tích nhu cầu đặt lịch nha khoa.
                        Chỉ trả JSON hợp lệ.
                        Không chẩn đoán.
                        Không kê thuốc.
                        """)
                .user(prompt)
                .call()
                .content();

        String cleaned = cleanJson(aiRaw);

        return objectMapper.readValue(cleaned, QuickConsultAiAnalysis.class);
    }

    private QuickConsultAiFinalReply buildFinalReplyByAi(
            String message,
            QuickConsultAiAnalysis analysis,
            List<PublicContent> serviceContents,
            List<PublicContent> doctorContents
    ) throws Exception {
        String prompt = aiPromptBuilder.buildFinalReplyPrompt(
                message,
                analysis,
                serviceContents,
                doctorContents
        );

        String aiRaw = chatClientBuilder.build()
                .prompt()
                .system("""
                        Bạn là chatbot tư vấn nhanh cho phòng khám nha khoa.
                        Chỉ dùng dữ liệu được cung cấp.
                        Không chẩn đoán.
                        Không kê thuốc.
                        Không bịa dịch vụ hoặc bác sĩ.
                        Luôn trả JSON hợp lệ.
                        """)
                .user(prompt)
                .call()
                .content();

        String cleaned = cleanJson(aiRaw);

        return objectMapper.readValue(cleaned, QuickConsultAiFinalReply.class);
    }

    private QuickConsultResponse mapAiFinalReplyToResponse(
            QuickConsultAiFinalReply aiReply,
            List<PublicContent> serviceContents,
            List<PublicContent> doctorContents
    ) {
        Map<UUID, PublicContent> serviceMap = toRefIdMap(serviceContents);
        Map<UUID, PublicContent> doctorMap = toRefIdMap(doctorContents);

        List<SuggestedServiceResponse> services = Optional.ofNullable(aiReply.services())
                .orElse(List.of())
                .stream()
                .filter(pick -> pick.serviceId() != null)
                .filter(pick -> serviceMap.containsKey(pick.serviceId()))
                .limit(3)
                .map(pick -> {
                    PublicContent item = serviceMap.get(pick.serviceId());
                    return new SuggestedServiceResponse(
                            item.getRefId(),
                            item.getTitle(),
                            firstNonBlank(item.getDescription(), item.getSubtitle(), item.getContent()),
                            pick.reason()
                    );
                })
                .toList();

        List<SuggestedDoctorResponse> doctors = Optional.ofNullable(aiReply.doctors())
                .orElse(List.of())
                .stream()
                .filter(pick -> pick.doctorId() != null)
                .filter(pick -> doctorMap.containsKey(pick.doctorId()))
                .limit(3)
                .map(pick -> {
                    PublicContent item = doctorMap.get(pick.doctorId());
                    return new SuggestedDoctorResponse(
                            item.getRefId(),
                            item.getTitle(),
                            firstNonBlank(item.getDescription(), item.getSubtitle(), item.getContent()),
                            pick.reason()
                    );
                })
                .toList();

        return new QuickConsultResponse(
                firstNonBlank(aiReply.reply(), buildReply("", services, doctors)),
                services,
                doctors,
                Boolean.TRUE.equals(aiReply.needMoreInfo()),
                aiReply.nextQuestion(),
                firstNonBlank(aiReply.disclaimer(), DISCLAIMER)
        );
    }

    private QuickConsultResponse consultByKeywordFallback(String message) {
        List<String> keywords = expandKeywords(message);

        List<PublicContent> serviceContents = searchManyKeywords(PublicContentType.SERVICE, keywords, 3);
        List<PublicContent> doctorContents = searchManyKeywords(PublicContentType.DOCTOR, keywords, 3);

        List<SuggestedServiceResponse> suggestedServices = serviceContents.stream()
                .filter(item -> item.getRefId() != null)
                .map(item -> new SuggestedServiceResponse(
                        item.getRefId(),
                        item.getTitle(),
                        firstNonBlank(item.getDescription(), item.getSubtitle(), item.getContent()),
                        "Dịch vụ này có nội dung phù hợp với nhu cầu bạn vừa nhập."
                ))
                .toList();

        List<SuggestedDoctorResponse> suggestedDoctors = doctorContents.stream()
                .filter(item -> item.getRefId() != null)
                .map(item -> new SuggestedDoctorResponse(
                        item.getRefId(),
                        item.getTitle(),
                        firstNonBlank(item.getDescription(), item.getSubtitle(), item.getContent()),
                        "Bác sĩ này có thông tin public liên quan đến nhu cầu tư vấn."
                ))
                .toList();

        boolean hasSuggestion = !suggestedServices.isEmpty() || !suggestedDoctors.isEmpty();

        return new QuickConsultResponse(
                buildReply(message, suggestedServices, suggestedDoctors),
                suggestedServices,
                suggestedDoctors,
                !hasSuggestion,
                hasSuggestion ? null : "Bạn có thể mô tả rõ hơn tình trạng như đau răng, lấy cao răng, niềng răng, tẩy trắng hoặc răng khôn không?",
                DISCLAIMER
        );
    }

    private List<PublicContent> searchManyKeywords(PublicContentType type, List<String> keywords, int limit) {
        Map<String, PublicContent> resultMap = new LinkedHashMap<>();

        for (String keyword : Optional.ofNullable(keywords).orElse(List.of())) {
            if (keyword == null || keyword.isBlank()) {
                continue;
            }

            Pageable pageable = PageRequest.of(
                    0,
                    limit,
                    Sort.by(
                            Sort.Order.desc("featured"),
                            Sort.Order.asc("sortOrder"),
                            Sort.Order.desc("createdAt")
                    )
            );

            Specification<PublicContent> spec = Specification.allOf(
                    PublicContentSpecification.hasRefType(type),
                    PublicContentSpecification.hasActive(true),
                    PublicContentSpecification.hasKeyword(keyword)
            );

            List<PublicContent> rows = publicContentRepository.findAll(spec, pageable).getContent();

            for (PublicContent row : rows) {
                if (row.getId() != null) {
                    resultMap.putIfAbsent(row.getId().toString(), row);
                }
            }

            if (resultMap.size() >= limit) {
                break;
            }
        }

        return resultMap.values().stream()
                .limit(limit)
                .toList();
    }

    private List<String> expandKeywords(String message) {
        String text = message == null ? "" : message.toLowerCase();
        List<String> keywords = new ArrayList<>();

        if (!text.isBlank()) {
            keywords.add(text);
        }

        if (text.contains("cao răng") || text.contains("vôi răng") || text.contains("hôi miệng")
                || text.contains("mảng bám") || text.contains("vệ sinh răng")) {
            keywords.addAll(List.of("cao răng", "vôi răng", "vệ sinh răng", "nha chu", "tổng quát"));
        }

        if (text.contains("đau răng") || text.contains("sâu răng") || text.contains("ê buốt")
                || text.contains("nhức răng") || text.contains("răng đau")) {
            keywords.addAll(List.of("đau răng", "sâu răng", "trám răng", "điều trị tủy", "nội nha", "khám tổng quát"));
        }

        if (text.contains("niềng") || text.contains("hô") || text.contains("móm")
                || text.contains("lệch răng") || text.contains("chỉnh nha")) {
            keywords.addAll(List.of("chỉnh nha", "niềng răng", "hô", "móm"));
        }

        if (text.contains("răng khôn") || text.contains("nhổ") || text.contains("mọc lệch")) {
            keywords.addAll(List.of("răng khôn", "nhổ răng", "tiểu phẫu", "phẫu thuật"));
        }

        if (text.contains("trắng") || text.contains("ố vàng") || text.contains("xỉn màu")
                || text.contains("tẩy trắng")) {
            keywords.addAll(List.of("tẩy trắng", "trắng răng", "thẩm mỹ"));
        }

        if (text.contains("chảy máu") || text.contains("viêm lợi") || text.contains("sưng nướu")
                || text.contains("nha chu")) {
            keywords.addAll(List.of("nha chu", "viêm lợi", "lấy cao răng", "khám tổng quát"));
        }

        return keywords.stream()
                .filter(k -> k != null && !k.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String buildReply(
            String message,
            List<SuggestedServiceResponse> services,
            List<SuggestedDoctorResponse> doctors
    ) {
        if (services.isEmpty() && doctors.isEmpty()) {
            return "Mình chưa tìm thấy dịch vụ hoặc bác sĩ phù hợp từ nội dung bạn nhập. Bạn có thể mô tả rõ hơn tình trạng răng miệng để mình gợi ý chính xác hơn.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Mình tìm thấy một số gợi ý phù hợp với nhu cầu của bạn");

        if (message != null && !message.isBlank()) {
            sb.append(" về \"").append(message).append("\"");
        }

        sb.append(". ");

        if (!services.isEmpty()) {
            sb.append("Bạn có thể tham khảo dịch vụ ");
            sb.append(joinTitles(services.stream().map(SuggestedServiceResponse::title).toList()));
            sb.append(". ");
        }

        if (!doctors.isEmpty()) {
            sb.append("Bạn cũng có thể chọn bác sĩ phù hợp bên dưới để đặt lịch. ");
        }

        sb.append(DISCLAIMER);

        return sb.toString();
    }

    private Map<UUID, PublicContent> toRefIdMap(List<PublicContent> contents) {
        Map<UUID, PublicContent> map = new LinkedHashMap<>();

        for (PublicContent item : Optional.ofNullable(contents).orElse(List.of())) {
            if (item.getRefId() != null) {
                map.putIfAbsent(item.getRefId(), item);
            }
        }

        return map;
    }

    private List<String> mergeKeywords(List<String> primary, List<String> fallback) {
        return Stream.concat(
                        Optional.ofNullable(primary).orElse(List.of()).stream(),
                        Optional.ofNullable(fallback).orElse(List.of()).stream()
                )
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String joinTitles(List<String> titles) {
        List<String> cleanTitles = titles.stream()
                .filter(t -> t != null && !t.isBlank())
                .toList();

        if (cleanTitles.isEmpty()) {
            return "";
        }

        return String.join(", ", cleanTitles);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return limitText(value.trim(), 220);
            }
        }

        return "";
    }

    private String limitText(String value, int max) {
        if (value == null) {
            return "";
        }

        if (value.length() <= max) {
            return value;
        }

        return value.substring(0, max) + "...";
    }

    private String cleanJson(String raw) {
        if (raw == null) return "{}";

        raw = raw.replace("```json", "")
                .replace("```", "")
                .trim();

        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");

        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }

        return raw;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }
}