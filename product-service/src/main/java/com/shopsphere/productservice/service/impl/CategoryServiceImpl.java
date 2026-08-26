package com.shopsphere.productservice.service.impl;

import com.shopsphere.productservice.dto.request.CategoryCreateRequest;
import com.shopsphere.productservice.dto.response.CategoryResponse;
import com.shopsphere.productservice.entity.Category;
import com.shopsphere.productservice.exception.DuplicateResourceException;
import com.shopsphere.productservice.exception.ResourceNotFoundException;
import com.shopsphere.productservice.mapper.CategoryMapper;
import com.shopsphere.productservice.repository.CategoryRepository;
import com.shopsphere.productservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    public CategoryResponse createCategory(CategoryCreateRequest request) {

        if(categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException(
                    "Category already exist:: "+ request.name()
            );
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: " + id
                        ));

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}
