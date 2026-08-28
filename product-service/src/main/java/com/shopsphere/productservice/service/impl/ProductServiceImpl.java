package com.shopsphere.productservice.service.impl;

import com.shopsphere.productservice.dto.request.ProductCreateRequest;
import com.shopsphere.productservice.dto.request.ProductStatusUpdateRequest;
import com.shopsphere.productservice.dto.request.ProductUpdateRequest;
import com.shopsphere.productservice.dto.response.ProductResponse;
import com.shopsphere.productservice.entity.Category;
import com.shopsphere.productservice.entity.Product;
import com.shopsphere.productservice.enums.ProductStatus;
import com.shopsphere.productservice.exception.DuplicateResourceException;
import com.shopsphere.productservice.exception.ResourceNotFoundException;
import com.shopsphere.productservice.mapper.ProductMapper;
import com.shopsphere.productservice.repository.CategoryRepository;
import com.shopsphere.productservice.repository.ProductRepository;
import com.shopsphere.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {

        log.info("Product request {}", request);

        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new DuplicateResourceException(
                    "Product with SKU already exists:: "+ request.sku()
            );
        }

        Category category = categoryRepository.findByIdAndActiveTrue(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active category not found with id:: " + request.categoryId()
                ));

        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setStatus(ProductStatus.ACTIVE);

        Product savedProducts = productRepository.save(product);

        log.info("Product saved successfully. Product ID: {}, SKU: {}", savedProducts.getId() , savedProducts.getSku());

        return productMapper.toResponse(savedProducts);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active products not found with id :: "+ id
                        ));

        log.info("product retrieved with id  {} and  name {} and category name {} ", product.getId() , product.getName() , product.getCategory().getName());

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAllByStatus(ProductStatus.ACTIVE)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {

        log.info("product dto {}", request);

        Product product = productRepository.findByIdAndStatus(id,ProductStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id ::"+ id)
                        );

        log.info("ProductResponse {}", product.getName());

        Category category = categoryRepository.findByIdAndActiveTrue(request.categoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + request.categoryId())
                        );

        log.info("Category {} ", category.getName());

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);

        log.info("Updated product {}", updatedProduct.getName());

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProductStatus(Long id, ProductStatusUpdateRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));

        product.setStatus(request.status());
        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }
}
