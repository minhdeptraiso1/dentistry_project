package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.service.ServiceResponse;
import com.project.base_v1.entity.ServiceCatalog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ServicePackageStepMapper.class})
public interface ServiceCatalogMapper {
    ServiceResponse toResponse(ServiceCatalog entity);
}
