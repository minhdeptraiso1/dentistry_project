package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.medicine.MedicinePriceHistoryResponse;
import com.project.base_v1.entity.MedicinePriceHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicinePriceHistoryMapper {

    @Mapping(target = "medicineId", source = "medicine.id")
    @Mapping(target = "medicineCode", source = "medicine.code")
    @Mapping(target = "medicineName", source = "medicine.name")
    MedicinePriceHistoryResponse toResponse(MedicinePriceHistory entity);
}
