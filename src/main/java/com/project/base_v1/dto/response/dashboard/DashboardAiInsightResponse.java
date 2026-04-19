package com.project.base_v1.dto.response.dashboard;

import java.util.List;

public record DashboardAiInsightResponse(
        String periodLabel,
        String executiveSummary,
        List<String> highlights,
        List<String> risks,
        List<String> recommendations,
        List<String> dataQualityNotes,
        List<MedicineProcurementSuggestionResponse> procurementSuggestions
) {
}