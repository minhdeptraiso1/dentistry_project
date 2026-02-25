package com.project.base_v1.repository;

import com.project.base_v1.entity.MedicinePriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MedicinePriceHistoryRepository extends JpaRepository<MedicinePriceHistory, UUID> {
    @EntityGraph(attributePaths = {"medicine"})
    Page<MedicinePriceHistory> findByMedicine_IdOrderByChangedAtDesc(UUID medicineId, Pageable pageable);

}
