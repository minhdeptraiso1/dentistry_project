package com.project.base_v1.service;

import com.project.base_v1.dto.request.service.CreateServiceRequest;
import com.project.base_v1.dto.request.service.ServiceSearchRequest;
import com.project.base_v1.dto.request.service.UpdateServiceRequest;
import com.project.base_v1.dto.response.service.ServiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServiceCatalogService {
    ServiceResponse create(CreateServiceRequest request);

    ServiceResponse getById(UUID id);

    Page<ServiceResponse> search(ServiceSearchRequest request, Pageable pageable);

    ServiceResponse update(UUID id, UpdateServiceRequest request);

    void delete(UUID id);
}
