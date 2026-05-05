package com.project.base_v1.dto.response.consultation;

import java.util.List;

public record QuickConsultResponse(
        String reply,
        List<SuggestedServiceResponse> suggestedServices,
        List<SuggestedDoctorResponse> suggestedDoctors,
        Boolean needMoreInfo,
        String nextQuestion,
        String disclaimer
) {
}