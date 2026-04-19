package com.project.base_v1.service;

import com.project.base_v1.dto.response.dashboard.DashboardAiInsightResponse;

public interface DashboardAiService {
    DashboardAiInsightResponse generateMonthlyInsight(int year, int month);
}