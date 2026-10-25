package com.shopsphere.productservice.repository;

import com.shopsphere.productservice.entity.Category;
import com.shopsphere.productservice.entity.Product;
import com.shopsphere.productservice.enums.ProductStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void saveProduct_shouldPersistAndRetrieveProduct() {
        Category category = new Category();
        category.setName("Repository Test Electronics");
        category.setDescription("Category for repository testing");
        category.setActive(true);

        Category savedCategory = categoryRepository.saveAndFlush(category);

        Product product = new Product();
        product.setSku("SKU-REPO-001");
        product.setName("Repository Test Product");
        product.setDescription("Testing JPA persistence");
        product.setPrice(new BigDecimal("999.00"));
        product.setBrand("TestBrand");
        product.setCategory(savedCategory);

        Product savedProduct = productRepository.saveAndFlush(product);

        assertNotNull(savedProduct.getId());
        assertNotNull(savedProduct.getCreatedAt());
        assertNotNull(savedProduct.getUpdatedAt());

        Product foundProduct = productRepository.findById(savedProduct.getId()).orElseThrow();

        assertEquals("SKU-REPO-001",foundProduct.getSku());
        assertEquals("Repository Test Product", foundProduct.getName());
        assertEquals(new BigDecimal("999.00"), foundProduct.getPrice());
        assertEquals(savedCategory.getId(),
                foundProduct.getCategory().getId());
        assertEquals("Repository Test Electronics",
                foundProduct.getCategory().getName());
    }

    @Test
    void existsBySkuIgnoreCase_shouldIgnoreCase() {
        Category category = new Category();
        category.setName("SKU Search Test Category");
        category.setDescription("Category for SKU search testing");
        category.setActive(true);

        Category savedCategory = categoryRepository.saveAndFlush(category);

        Product product = new Product();
        product.setSku("SKU-REPO-002");
        product.setName("SKU Search Test Product");
        product.setDescription("Testing case-insensitive SKU search");
        product.setPrice(new BigDecimal("499.00"));
        product.setBrand("TestBrand");
        product.setCategory(savedCategory);

        productRepository.saveAndFlush(product);

        assertTrue(productRepository.existsBySkuIgnoreCase("sku-repo-002"));
        assertFalse(productRepository.existsBySkuIgnoreCase("SKU-UNKNOWN"));
    }

    @Test
    void findBySku_shouldReturnProductWhenSkuExists() {

        Product savedProduct = saveProduct(
                "SKU-REPO-003", ProductStatus.ACTIVE);

        var result = productRepository.findBySku("SKU-REPO-003");

        assertTrue(result.isPresent());
        assertEquals(savedProduct.getId(), result.orElseThrow().getId());
        assertEquals("SKU-REPO-003", result.orElseThrow().getSku());

        assertFalse(productRepository.findBySku("SKU-UNKNOWN").isPresent());
    }

    @Test
    void findByIdAndStatus_shouldReturnProductOnlyWhenStatusMatches() {

        Product savedProduct = saveProduct(
                "SKU-REPO-004", ProductStatus.ACTIVE);

        var activeResult = productRepository.findByIdAndStatus(
                savedProduct.getId(), ProductStatus.ACTIVE);

        var inactiveResult = productRepository.findByIdAndStatus(
                savedProduct.getId(), ProductStatus.INACTIVE);

        assertTrue(activeResult.isPresent());
        assertEquals(savedProduct.getId(), activeResult.orElseThrow().getId());

        assertFalse(inactiveResult.isPresent());
    }

    @Test
    void findAllByStatus_shouldReturnOnlyProductsWithRequestedStatus() {

        saveProduct("SKU-REPO-005", ProductStatus.ACTIVE);
        saveProduct("SKU-REPO-006", ProductStatus.INACTIVE);

        Page<Product> activeProducts = productRepository.findAllByStatus(
                ProductStatus.ACTIVE, PageRequest.of(0, 10));

        assertEquals(1, activeProducts.getTotalElements());
        assertEquals(1, activeProducts.getContent().size());
        assertEquals(
                "SKU-REPO-005",
                activeProducts.getContent().get(0).getSku());
    }

    private Product saveProduct(String sku, ProductStatus status) {

        Category category = new Category();
        category.setName("Category for " + sku);
        category.setDescription("Test category");
        category.setActive(true);

        Category savedCategory = categoryRepository.saveAndFlush(category);

        Product product = new Product();
        product.setSku(sku);
        product.setName("Product " + sku);
        product.setDescription("Product for repository testing");
        product.setPrice(new BigDecimal("100.00"));
        product.setBrand("TestBrand");
        product.setCategory(savedCategory);
        product.setStatus(status);

        return productRepository.saveAndFlush(product);
    }

}
