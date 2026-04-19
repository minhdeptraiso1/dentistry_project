package com.project.base_v1.repository.projection;

import java.util.UUID;

public interface MedicineDispensedSummaryProjection {
    UUID getMedicineId();

    String getMedicineCode();

    String getMedicineName();

    Integer getDispensedQty();
}