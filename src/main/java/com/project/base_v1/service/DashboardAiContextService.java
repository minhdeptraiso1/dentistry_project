package com.project.base_v1.service;

import com.project.base_v1.dto.response.dashboard.DashboardAiContextResponse;

public interface DashboardAiContextService {
    DashboardAiContextResponse buildMonthlyContext(int year, int month);
}