package com.shopsphere.productservice.service.impl;

import com.shopsphere.productservice.dto.request.ProductCreateRequest;
import com.shopsphere.productservice.dto.response.ProductResponse;
import com.shopsphere.productservice.entity.Category;
import com.shopsphere.productservice.entity.Product;
import com.shopsphere.productservice.enums.ProductStatus;
import com.shopsphere.productservice.exception.DuplicateResourceException;
import com.shopsphere.productservice.exception.ResourceNotFoundException;
import com.shopsphere.productservice.mapper.ProductMapper;
import com.shopsphere.productservice.repository.CategoryRepository;
import com.shopsphere.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getProductById_shouldReturnActiveProduct() {

        Long productId = 10L;

        Product product = Mockito.mock(Product.class);
        Category category = Mockito.mock(Category.class);

        ProductResponse expectedResponse = new ProductResponse(
                10L,
                "SKU-TEST-001",
                "Test Product",
                "Product for unit testing",
                new BigDecimal("999.00"),
                1L,
                "Electronics",
                "TestBrand",
                ProductStatus.ACTIVE,
                null,
                null
        );

        when(productRepository.findByIdAndStatus(
                productId,
                ProductStatus.ACTIVE
        )).thenReturn(Optional.of(product));

        when(product.getId()).thenReturn(productId);
        when(product.getName()).thenReturn("Test Product");
        when(product.getCategory()).thenReturn(category);
        when(category.getName()).thenReturn("Electronics");

        when(productMapper.toResponse(product))
                .thenReturn(expectedResponse);

        ProductResponse actualResponse =
                productService.getProductById(productId);

        assertSame(expectedResponse, actualResponse);

        verify(productRepository).findByIdAndStatus(
                productId,
                ProductStatus.ACTIVE
        );

        verify(productMapper).toResponse(product);
    }


    @Test
    void getProductById_shouldThrowExceptionWhenProductDoesNotExist() {

        // Arrange
        Long productId = 99L;

        when(productRepository.findByIdAndStatus(
                productId,
                ProductStatus.ACTIVE
        )).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(productId)
        );

        verify(productRepository).findByIdAndStatus(
                productId,
                ProductStatus.ACTIVE
        );

        verifyNoInteractions(productMapper);
    }



    @Test
    void createProduct_shouldThrowExceptionWhenSkuAlreadyExists() {

        // Arrange
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-TEST-001",
                "Test Product",
                "Product for unit testing",
                new BigDecimal("999.00"),
                1L,
                "TestBrand"
        );

        when(productRepository.existsBySkuIgnoreCase("SKU-TEST-001"))
                .thenReturn(true);

        // Act and Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> productService.createProduct(request)
        );

        // Verify repository was checked for the duplicate SKU.
        verify(productRepository).existsBySkuIgnoreCase("SKU-TEST-001");

        // Category lookup and mapping must not happen after duplicate detection.
        verifyNoInteractions(categoryRepository, productMapper);
    }



    @Test
    void createProduct_shouldSaveAndReturnResponseWhenRequestIsValid() {

        // Arrange
        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-NEW-001",
                "New Product",
                "Product for successful creation test",
                new BigDecimal("1299.00"),
                1L,
                "TestBrand"
        );

        Category category = org.mockito.Mockito.mock(Category.class);
        Product product = org.mockito.Mockito.mock(Product.class);

        ProductResponse expectedResponse = new ProductResponse(
                10L,
                "SKU-NEW-001",
                "New Product",
                "Product for successful creation test",
                new BigDecimal("1299.00"),
                1L,
                "Electronics",
                "TestBrand",
                ProductStatus.ACTIVE,
                null,
                null
        );

        when(productRepository.existsBySkuIgnoreCase("SKU-NEW-001"))
                .thenReturn(false);

        when(categoryRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(category));

        when(productMapper.toEntity(request))
                .thenReturn(product);

        when(productRepository.save(product))
                .thenReturn(product);

        when(productMapper.toResponse(product))
                .thenReturn(expectedResponse);

        // Act
        ProductResponse actualResponse = productService.createProduct(request);

        // Assert
        assertSame(expectedResponse, actualResponse);

        verify(productRepository)
                .existsBySkuIgnoreCase("SKU-NEW-001");

        verify(categoryRepository)
                .findByIdAndActiveTrue(1L);

        verify(productMapper).toEntity(request);

        verify(product).setCategory(category);
        verify(product).setStatus(ProductStatus.ACTIVE);

        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }
}
