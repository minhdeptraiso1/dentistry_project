package com.project.base_v1.dto.request.consultation;

import java.util.List;

public record QuickConsultAiAnalysis(
        String intent,
        List<String> serviceKeywords,
        List<String> doctorKeywords,
        Boolean needMoreInfo,
        String nextQuestion,
        String urgency
) {
}