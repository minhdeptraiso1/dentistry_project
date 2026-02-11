package com.project.base_v1.repository;

import com.project.base_v1.entity.ServiceCatalog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, UUID>, JpaSpecificationExecutor<ServiceCatalog> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"steps"})
    @Query("SELECT s FROM ServiceCatalog s WHERE s.id = :id")
    Optional<ServiceCatalog> findDetailById(@Param("id") UUID id);

    @Query("""
                SELECT s.code
                FROM ServiceCatalog s
                ORDER BY s.code DESC
                LIMIT 1
            """)
    Optional<String> findLatestCode();
}
