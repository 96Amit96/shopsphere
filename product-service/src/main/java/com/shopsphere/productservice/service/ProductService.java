package com.shopsphere.productservice.service;

import com.shopsphere.productservice.dto.request.ProductCreateRequest;
import com.shopsphere.productservice.dto.request.ProductStatusUpdateRequest;
import com.shopsphere.productservice.dto.request.ProductUpdateRequest;
import com.shopsphere.productservice.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    ProductResponse updateProduct(Long id , ProductUpdateRequest request);

    ProductResponse updateProductStatus(Long id , ProductStatusUpdateRequest request);
}
