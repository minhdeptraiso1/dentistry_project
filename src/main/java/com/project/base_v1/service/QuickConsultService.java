package com.project.base_v1.service;

import com.project.base_v1.dto.request.consultation.QuickConsultRequest;
import com.project.base_v1.dto.response.consultation.QuickConsultResponse;

public interface QuickConsultService {

    QuickConsultResponse consult(QuickConsultRequest request);
}