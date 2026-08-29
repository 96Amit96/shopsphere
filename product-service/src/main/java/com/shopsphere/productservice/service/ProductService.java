package com.shopsphere.productservice.service;

import com.shopsphere.productservice.dto.request.ProductCreateRequest;
import com.shopsphere.productservice.dto.request.ProductStatusUpdateRequest;
import com.shopsphere.productservice.dto.request.ProductUpdateRequest;
import com.shopsphere.productservice.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request);

    ProductResponse getProductById(Long id);

   // List<ProductResponse> getAllProducts();

    Page<ProductResponse> getAllProducts(
            int page,
            int size,
            String sortBy,
            String direction
    );

    ProductResponse updateProduct(Long id , ProductUpdateRequest request);

    ProductResponse updateProductStatus(Long id , ProductStatusUpdateRequest request);
}
