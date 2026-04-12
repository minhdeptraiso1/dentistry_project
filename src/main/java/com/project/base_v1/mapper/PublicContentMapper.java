package com.project.base_v1.mapper;

import com.project.base_v1.dto.response.publiccontent.PublicContentDetailResponse;
import com.project.base_v1.dto.response.publiccontent.PublicContentResponse;
import com.project.base_v1.dto.response.publiccontent.PublicContentSummaryResponse;
import com.project.base_v1.entity.PublicContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PublicContentMapper {

    PublicContentSummaryResponse toSummaryResponse(PublicContent entity);

    @Mapping(target = "subImages", source = "subImageUrls")
    PublicContentDetailResponse toDetailResponse(PublicContent entity);

    @Mapping(target = "subImages", source = "subImageUrls")
    PublicContentResponse toResponse(PublicContent entity);

    default List<String> map(String subImageUrls) {
        if (subImageUrls == null || subImageUrls.isBlank()) {
            return List.of();
        }

        return Arrays.stream(subImageUrls.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}