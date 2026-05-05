package com.project.base_v1.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.project.base_v1.dto.request.consultation.ChatMessageRequest;
import com.project.base_v1.dto.request.consultation.QuickConsultAiAnalysis;
import com.project.base_v1.entity.PublicContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QuickConsultAiPromptBuilder {

    private final ObjectMapper objectMapper;

    public String buildAnalyzePrompt(String message, List<ChatMessageRequest> conversation) {
        try {
            String conversationJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(conversation == null ? List.of() : conversation);

            return """
                    Bạn là bộ phân tích nhu cầu đặt lịch nha khoa.
                    
                    Nhiệm vụ:
                    - Đọc tin nhắn người dùng.
                    - Trích xuất intent và keyword để backend search database.
                    - Không chẩn đoán bệnh.
                    - Không kê thuốc.
                    - Không tư vấn điều trị chi tiết.
                    - Chỉ trả về JSON hợp lệ.
                    
                    Một số intent gợi ý:
                    - TOOTH_PAIN
                    - SCALING
                    - WHITENING
                    - ORTHODONTICS
                    - WISDOM_TOOTH
                    - GUM_PROBLEM
                    - GENERAL_CHECKUP
                    - UNKNOWN
                    
                    Quy tắc keyword:
                    - serviceKeywords dùng để tìm dịch vụ.
                    - doctorKeywords dùng để tìm bác sĩ.
                    - Keyword nên ngắn gọn bằng tiếng Việt.
                    - Nếu user nói đau răng/ê buốt/sâu răng, nên có keyword như: đau răng, ê buốt, sâu răng, trám răng, điều trị tủy, khám tổng quát.
                    - Nếu user nói lấy cao răng/vôi răng/hôi miệng, nên có: lấy cao răng, cao răng, vôi răng, vệ sinh răng, nha chu.
                    - Nếu user nói niềng/hô/móm/lệch răng, nên có: chỉnh nha, niềng răng.
                    - Nếu user nói răng khôn/nhổ, nên có: răng khôn, nhổ răng, tiểu phẫu.
                    - Nếu user nói trắng răng/ố vàng, nên có: tẩy trắng, thẩm mỹ.
                    
                    Tin nhắn mới nhất:
                    %s
                    
                    Lịch sử hội thoại:
                    %s
                    
                    Trả JSON đúng schema:
                    {
                      "intent": "string",
                      "serviceKeywords": ["string"],
                      "doctorKeywords": ["string"],
                      "needMoreInfo": false,
                      "nextQuestion": null,
                      "urgency": "LOW|MEDIUM|HIGH"
                    }
                    """.formatted(message, conversationJson);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không thể build analyze prompt", e);
        }
    }

    public String buildFinalReplyPrompt(
            String message,
            QuickConsultAiAnalysis analysis,
            List<PublicContent> services,
            List<PublicContent> doctors
    ) {
        try {
            String analysisJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(analysis);

            String servicesJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(toServiceContext(services));

            String doctorsJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(toDoctorContext(doctors));

            return """
                    Bạn là chatbot tư vấn nhanh cho website phòng khám nha khoa.
                    
                    Nhiệm vụ:
                    - Viết câu trả lời thân thiện, tự nhiên bằng tiếng Việt.
                    - Chỉ sử dụng dịch vụ và bác sĩ trong danh sách được cung cấp.
                    - Không bịa serviceId, doctorId, tên dịch vụ hoặc tên bác sĩ.
                    - Không chẩn đoán bệnh.
                    - Không kê thuốc.
                    - Không thay thế tư vấn của bác sĩ.
                    - Nếu không có dịch vụ hoặc bác sĩ phù hợp, hãy nói cần mô tả thêm hoặc đặt lịch khám tổng quát nếu có trong danh sách.
                    
                    Tin nhắn người dùng:
                    %s
                    
                    Phân tích AI bước 1:
                    %s
                    
                    Dịch vụ thật tìm được từ database:
                    %s
                    
                    Bác sĩ thật tìm được từ database:
                    %s
                    
                    Trả JSON hợp lệ đúng schema:
                    {
                      "reply": "string",
                      "services": [
                        {
                          "serviceId": "uuid",
                          "reason": "string"
                        }
                      ],
                      "doctors": [
                        {
                          "doctorId": "uuid",
                          "reason": "string"
                        }
                      ],
                      "needMoreInfo": false,
                      "nextQuestion": null,
                      "disclaimer": "Thông tin chỉ mang tính gợi ý, bác sĩ sẽ tư vấn chính xác sau khi thăm khám."
                    }
                    
                    Quy tắc:
                    - services tối đa 3 item.
                    - doctors tối đa 3 item.
                    - Nếu danh sách database rỗng thì trả services/doctors rỗng.
                    - reason ngắn gọn, dễ hiểu.
                    - disclaimer luôn có.
                    """.formatted(message, analysisJson, servicesJson, doctorsJson);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không thể build final reply prompt", e);
        }
    }

    private List<Map<String, Object>> toServiceContext(List<PublicContent> services) {
        if (services == null) return List.of();

        return services.stream()
                .filter(s -> s.getRefId() != null)
                .map(s -> Map.<String, Object>of(
                        "serviceId", s.getRefId().toString(),
                        "title", nullToEmpty(s.getTitle()),
                        "description", limitText(firstNonBlank(s.getDescription(), s.getSubtitle(), s.getContent()), 400)
                ))
                .toList();
    }

    private List<Map<String, Object>> toDoctorContext(List<PublicContent> doctors) {
        if (doctors == null) return List.of();

        return doctors.stream()
                .filter(d -> d.getRefId() != null)
                .map(d -> Map.<String, Object>of(
                        "doctorId", d.getRefId().toString(),
                        "doctorName", nullToEmpty(d.getTitle()),
                        "description", limitText(firstNonBlank(d.getDescription(), d.getSubtitle(), d.getContent()), 400)
                ))
                .toList();
    }

    private String firstNonBlank(String... values) {
        if (values == null) return "";

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return "";
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String limitText(String value, int max) {
        if (value == null) return "";
        if (value.length() <= max) return value;
        return value.substring(0, max) + "...";
    }
}