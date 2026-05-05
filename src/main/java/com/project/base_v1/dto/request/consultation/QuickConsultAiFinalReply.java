package com.project.base_v1.dto.request.consultation;

import java.util.List;

public record QuickConsultAiFinalReply(
        String reply,
        List<AiServicePick> services,
        List<AiDoctorPick> doctors,
        Boolean needMoreInfo,
        String nextQuestion,
        String disclaimer
) {
}