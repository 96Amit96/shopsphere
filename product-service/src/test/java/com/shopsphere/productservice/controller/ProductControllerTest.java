package com.shopsphere.productservice.controller;

import com.shopsphere.productservice.dto.response.ProductResponse;
import com.shopsphere.productservice.enums.ProductStatus;
import com.shopsphere.productservice.security.jwt.JwtService;
import com.shopsphere.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getProductById_shouldReturnProductResponse() throws Exception {

        ProductResponse response = new ProductResponse(
                10L,
                "SKU-TEST-001",
                "Test Product",
                "Product for controller testing",
                new BigDecimal("999.00"),
                1L,
                "Electronics",
                "TestBrand",
                ProductStatus.ACTIVE,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        when(productService.getProductById(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        org.springframework.http.MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Product retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.sku").value("SKU-TEST-001"))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(productService).getProductById(10L);
    }
}
