package com.project.base_v1.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.base_v1.dto.response.dashboard.DashboardAiContextResponse;
import com.project.base_v1.dto.response.dashboard.DashboardAiInsightResponse;
import com.project.base_v1.service.DashboardAiContextService;
import com.project.base_v1.service.DashboardAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardAiServiceImpl implements DashboardAiService {

    private final DashboardAiContextService dashboardAiContextService;
    private final DashboardAiPromptBuilder promptBuilder;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    @Override
    public DashboardAiInsightResponse generateMonthlyInsight(int year, int month) {
        DashboardAiContextResponse context = dashboardAiContextService.buildMonthlyContext(year, month);

        String prompt = promptBuilder.buildPrompt(context);

        String aiRaw = chatClientBuilder.build()
                .prompt()
                .system("""
                        Bạn là trợ lý phân tích kinh doanh cho phòng khám nha khoa.
                        Chỉ dùng dữ liệu được cung cấp.
                        Không bịa số liệu.
                        Nếu dữ liệu chưa đủ chắc chắn, hãy nói rõ.
                        Luôn trả về JSON hợp lệ.
                        """)
                .user(prompt)
                .call()
                .content();

        try {
            String cleaned = cleanJson(aiRaw);
            return objectMapper.readValue(cleaned, DashboardAiInsightResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Không thể parse kết quả AI", e);
        }
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
}