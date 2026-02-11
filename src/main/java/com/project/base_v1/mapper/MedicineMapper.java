package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.medicine.MedicineResponse;
import com.project.base_v1.entity.Medicine;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MedicineMapper {
    MedicineResponse toResponse(Medicine entity);
}
