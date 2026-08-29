package com.shopsphere.productservice.service.impl;

import com.shopsphere.productservice.config.ProductSortConfig;
import com.shopsphere.productservice.dto.request.ProductCreateRequest;
import com.shopsphere.productservice.dto.request.ProductSearchRequest;
import com.shopsphere.productservice.dto.request.ProductStatusUpdateRequest;
import com.shopsphere.productservice.dto.request.ProductUpdateRequest;
import com.shopsphere.productservice.dto.response.ProductResponse;
import com.shopsphere.productservice.entity.Category;
import com.shopsphere.productservice.entity.Product;
import com.shopsphere.productservice.enums.ProductStatus;
import com.shopsphere.productservice.exception.DuplicateResourceException;
import com.shopsphere.productservice.exception.InvalidRequestException;
import com.shopsphere.productservice.exception.ResourceNotFoundException;
import com.shopsphere.productservice.mapper.ProductMapper;
import com.shopsphere.productservice.repository.CategoryRepository;
import com.shopsphere.productservice.repository.ProductRepository;
import com.shopsphere.productservice.service.ProductService;
import com.shopsphere.productservice.service.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        return productRepository.findAllByStatus(ProductStatus.ACTIVE, pageable).map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(ProductSearchRequest request) {

        int page = request.page() == null ? 0 : request.page();
        int size = request.size() == null ? 10 : request.size();

        String sortBy = request.sortBy() == null
                ? "createdAt"
                : request.sortBy();

        String direction = request.direction() == null
                ? "desc"
                : request.direction();

        // Pagination validation
        if (page < 0) {
            throw new InvalidRequestException(
                    "Page must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new InvalidRequestException(
                    "Page size must be between 1 and 100"
            );
        }

        if (request.minPrice() != null
                && request.maxPrice() != null
                && request.minPrice().compareTo(request.maxPrice()) > 0) {

            throw new InvalidRequestException(
                    "Minimum price must be less than or equal to maximum price"
            );
        }

        if (request.minPrice() != null
                && request.minPrice().compareTo(BigDecimal.ZERO) < 0) {

            throw new InvalidRequestException(
                    "Minimum price cannot be negative"
            );
        }

        if (request.maxPrice() != null
                && request.maxPrice().compareTo(BigDecimal.ZERO) < 0) {

            throw new InvalidRequestException(
                    "Maximum price cannot be negative"
            );
        }

        // Sort direction validation
        if (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {

            throw new InvalidRequestException(
                    "Sort direction must be either asc or desc"
            );
        }

        // Sort field validation
        if (!ProductSortConfig.ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new InvalidRequestException(
                    "Invalid sort field: " + sortBy
            );
        }

        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Specification<Product> specification =
                ProductSpecification.hasStatus(ProductStatus.ACTIVE);

        if (request.search() != null &&
                !request.search().isBlank()) {

            specification = specification.and(
                    ProductSpecification.search(
                            request.search()
                    )
            );
        }

        if (request.categoryId() != null) {

            specification = specification.and(
                    ProductSpecification.hasCategory(
                            request.categoryId()
                    )
            );
        }

        if (request.brand() != null &&
                !request.brand().isBlank()) {

            specification = specification.and(
                    ProductSpecification.hasBrand(
                            request.brand()
                    )
            );
        }

        if (request.minPrice() != null) {

            specification = specification.and(
                    ProductSpecification
                            .priceGreaterThanOrEqualTo(
                                    request.minPrice()
                            )
            );
        }

        if (request.maxPrice() != null) {

            specification = specification.and(
                    ProductSpecification
                            .priceLessThanOrEqualTo(
                                    request.maxPrice()
                            )
            );
        }

        return productRepository
                .findAll(specification, pageable)
                .map(productMapper::toResponse);
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
