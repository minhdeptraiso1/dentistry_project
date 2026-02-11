package com.project.base_v1.repository.projection;

import java.time.LocalDate;
import java.util.UUID;

public interface MedicineStockSummaryProjection {
    UUID getMedicineId();

    String getMedicineCode();

    String getMedicineName();

    String getUnit();

    Integer getTotalRemaining();

    Integer getTotalBatches();

    LocalDate getNearestExpiryDate();

    Integer getExpiredBatchesCount();

    Integer getNearExpiryBatchesCount();
}
