package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.service.CreatePackageStepRequest;
import com.project.base_v1.dto.request.service.CreateServiceRequest;
import com.project.base_v1.dto.request.service.ServiceSearchRequest;
import com.project.base_v1.dto.request.service.UpdateServiceRequest;
import com.project.base_v1.dto.response.service.ServiceResponse;
import com.project.base_v1.entity.ServiceCatalog;
import com.project.base_v1.entity.ServicePackageStep;
import com.project.base_v1.enums.ServiceType;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.ServiceCatalogMapper;
import com.project.base_v1.repository.ServiceCatalogRepository;
import com.project.base_v1.repository.spec.ServiceCatalogSpecification;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.ServiceCatalogService;
import com.project.base_v1.service.helper.ServiceCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceCatalogServiceImpl implements ServiceCatalogService {

    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ServiceCodeGenerator serviceCodeGenerator;
    private final ServiceCatalogMapper serviceCatalogMapper;

    @Override
    @Transactional
    public ServiceResponse create(CreateServiceRequest request) {

        validateCreateRequest(request);

        ServiceCatalog service = ServiceCatalog.builder()
                .id(UUID.randomUUID())
                .code(serviceCodeGenerator.nextCode(request.type()))
                .name(request.name())
                .type(request.type())
                .category(request.category())
                .description(request.description())
                .basePrice(request.basePrice())
                .unit(request.unit())
                .durationMin(request.durationMin())
                .active(true)
                .build();

        if (request.type() == ServiceType.SINGLE && request.steps() != null && !request.steps().isEmpty()) {
            throw new BusinessException(ErrorCode.SINGLE_SERVICE_SHOULD_NOT_HAVE_STEPS);
        }

        if (request.type() == ServiceType.PACKAGE) {
            List<ServicePackageStep> steps = toSteps(service, request.steps());
            service.setSteps(steps);
        }

        ServiceCatalog saved = serviceCatalogRepository.save(service);


        return serviceCatalogMapper.toResponse(
                serviceCatalogRepository.findDetailById(saved.getId()).orElse(saved)
        );
    }

    @Override
    public ServiceResponse getById(UUID id) {
        ServiceCatalog entity = serviceCatalogRepository.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_FOUND));
        return serviceCatalogMapper.toResponse(entity);
    }

    @Override
    public Page<ServiceResponse> search(ServiceSearchRequest request, Pageable pageable) {

        Specification<ServiceCatalog> spec = Specification.allOf(
                ServiceCatalogSpecification.keywordLike(request.keyword()),
                ServiceCatalogSpecification.hasType(request.type()),
                ServiceCatalogSpecification.isActive(request.active())
        );
        
        return serviceCatalogRepository.findAll(spec, pageable)
                .map(serviceCatalogMapper::toResponse);
    }

    @Override
    @Transactional
    public ServiceResponse update(UUID id, UpdateServiceRequest request) {

        ServiceCatalog entity = serviceCatalogRepository.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_FOUND));

        if (request.name() != null) entity.setName(request.name());
        if (request.category() != null) entity.setCategory(request.category());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.basePrice() != null) entity.setBasePrice(request.basePrice());
        if (request.unit() != null) entity.setUnit(request.unit());
        if (request.durationMin() != null) entity.setDurationMin(request.durationMin());
        if (request.active() != null) entity.setActive(request.active());

        // Nếu là PACKAGE và client gửi steps => replace toàn bộ
        if (entity.getType() == ServiceType.PACKAGE && request.steps() != null) {
            validateSteps(request.steps());

            // clear old (cascade ALL) -> sẽ delete row cũ
            entity.getSteps().clear();

            List<ServicePackageStep> newSteps = toSteps(entity, request.steps());
            entity.getSteps().addAll(newSteps);
        }

        ServiceCatalog saved = serviceCatalogRepository.save(entity);
        return serviceCatalogMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ServiceCatalog entity = serviceCatalogRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_FOUND));

        entity.setDeletedAt(Instant.now());
        entity.setDeletedBy(CurrentUser.username());
        serviceCatalogRepository.save(entity);
    }

    // ===================== VALIDATION & MAPPING =====================

    private void validateCreateRequest(CreateServiceRequest request) {
        if (request.type() == ServiceType.PACKAGE) {
            validateSteps(request.steps());
        }
    }

    private void validateSteps(List<CreatePackageStepRequest> steps) {

        if (steps == null || steps.isEmpty()) {
            throw new BusinessException(ErrorCode.PACKAGE_STEPS_REQUIRED);
        }

        Set<Integer> stepNos = new HashSet<>();

        for (CreatePackageStepRequest s : steps) {

            if (s.stepNo() == null || s.stepNo() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_STEP_NO);
            }

            if (!stepNos.add(s.stepNo())) {
                throw new BusinessException(ErrorCode.PACKAGE_STEP_NO_DUPLICATED);
            }

            if (s.quantity() == null || s.quantity() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_STEP_QUANTITY);
            }

            if (s.price() == null || s.price().signum() < 0) {
                throw new BusinessException(ErrorCode.INVALID_STEP_PRICE);
            }
        }
    }


    private List<ServicePackageStep> toSteps(ServiceCatalog service, List<CreatePackageStepRequest> reqSteps) {
        // sort by stepNo for consistent ordering
        List<CreatePackageStepRequest> sorted = new ArrayList<>(reqSteps);
        sorted.sort(Comparator.comparingInt(CreatePackageStepRequest::stepNo));

        List<ServicePackageStep> steps = new ArrayList<>();
        for (CreatePackageStepRequest s : sorted) {
            steps.add(ServicePackageStep.builder()
                    .id(UUID.randomUUID())
                    .service(service)
                    .stepNo(s.stepNo())
                    .stepName(s.stepName())
                    .stepDesc(s.stepDesc())
                    .price(s.price())
                    .quantity(s.quantity())
                    .build());
        }
        return steps;
    }
}
