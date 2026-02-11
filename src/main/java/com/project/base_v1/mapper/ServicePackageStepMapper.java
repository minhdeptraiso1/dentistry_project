package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.service.ServiceStepResponse;
import com.project.base_v1.entity.ServicePackageStep;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServicePackageStepMapper {
    ServiceStepResponse toResponse(ServicePackageStep step);
}
