package com.shopsphere.productservice.service;

import com.shopsphere.productservice.dto.request.CategoryCreateRequest;
import com.shopsphere.productservice.dto.request.CategoryUpdateRequest;
import com.shopsphere.productservice.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryCreateRequest request);

    CategoryResponse getCategoryById(Long id);

    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategory(
            Long id,
            CategoryUpdateRequest request
    );

    void deactivateCategory(Long id);
}
