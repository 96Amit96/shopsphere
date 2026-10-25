package com.shopsphere.productservice.integration;

import com.shopsphere.productservice.dto.request.ProductCreateRequest;
import com.shopsphere.productservice.dto.response.ProductResponse;
import com.shopsphere.productservice.entity.Category;
import com.shopsphere.productservice.entity.Product;
import com.shopsphere.productservice.repository.CategoryRepository;
import com.shopsphere.productservice.repository.ProductRepository;
import com.shopsphere.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:product-integration-db;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false"
        }
)
@Transactional
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void createProduct_shouldPersistAndRetrieveProduct(){
        Category category = new Category();
        category.setName("Integration Test Electronics");
        category.setDescription("Category for integration testing");
        category.setActive(true);

        Category savedCategory = categoryRepository.saveAndFlush(category);

        ProductCreateRequest request = new ProductCreateRequest(
                "SKU-INT-001",
                "Integration Test Product",
                "Product created through the real service",
                new BigDecimal("1499.00"),
                savedCategory.getId(),
                "TestBrand"
        );

        ProductResponse created = productService.createProduct(request);

        assertNotNull(created.id());
        assertEquals("SKU-INT-001", created.sku());
        assertEquals("Integration Test Product", created.name());

        Product persisted = productRepository.findById(created.id())
                .orElseThrow();

        assertEquals("SKU-INT-001", persisted.getSku());
        assertEquals(new BigDecimal("1499.00"), persisted.getPrice());
        assertEquals(
                savedCategory.getId(),
                persisted.getCategory().getId()
        );

        ProductResponse retrieved =
                productService.getProductById(created.id());

        assertEquals(created.id(), retrieved.id());
        assertEquals("SKU-INT-001", retrieved.sku());
        assertEquals("Integration Test Product", retrieved.name());
        assertEquals("TestBrand", retrieved.brand());
    }
}
