package com.shopsphere.productservice.controller;

import com.shopsphere.productservice.config.SecurityConfig;
import com.shopsphere.productservice.dto.request.ProductCreateRequest;
import com.shopsphere.productservice.dto.response.ProductResponse;
import com.shopsphere.productservice.enums.ProductStatus;
import com.shopsphere.productservice.security.jwt.JwtService;
import com.shopsphere.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
public class ProductSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "USER")
    void userShouldNotCreateProduct() throws Exception {

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                 "sku": "SKU-SEC-001",
                                 "name": "Security Test Product",
                                 "description": "Testing product authorization",
                                 "price": 100.00,
                                 "categoryId": 1,
                                 "brand": "TestBrand"
                                }
                                
                                """
                )
        ).andExpect(status().isForbidden());

        verifyNoInteractions(productService);

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminShouldCreateProduct() throws Exception {

        ProductResponse response = new ProductResponse(
                20L,
                "SKU-SEC-002",
                "Admin Test Product",
                "Created by admin",
                new BigDecimal("499.00"),
                1L,
                "Electronics",
                "TestBrand",
                ProductStatus.ACTIVE,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        when(productService.createProduct(any(ProductCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "sku": "SKU-SEC-002",
                                    "name": "Admin Test Product",
                                    "description": "Created by admin",
                                    "price": 499.00,
                                    "categoryId": 1,
                                    "brand": "TestBrand"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(20))
                .andExpect(jsonPath("$.data.sku").value("SKU-SEC-002"));

        verify(productService)
                .createProduct(any(ProductCreateRequest.class));
    }
}
