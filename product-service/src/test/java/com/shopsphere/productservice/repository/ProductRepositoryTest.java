package com.shopsphere.productservice.repository;

import com.shopsphere.productservice.entity.Category;
import com.shopsphere.productservice.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
}
