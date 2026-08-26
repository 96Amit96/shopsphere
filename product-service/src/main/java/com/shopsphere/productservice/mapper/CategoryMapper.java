package com.shopsphere.productservice.mapper;

import com.shopsphere.productservice.dto.request.CategoryCreateRequest;
import com.shopsphere.productservice.dto.response.CategoryResponse;
import com.shopsphere.productservice.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryCreateRequest request);

    CategoryResponse toResponse(Category category);
}
