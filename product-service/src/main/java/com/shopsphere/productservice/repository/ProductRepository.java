package com.shopsphere.productservice.repository;

import com.shopsphere.productservice.entity.Product;
import com.shopsphere.productservice.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    Optional<Product> findBySku(String sku);

    Optional<Product> findByIdAndStatus(Long id , ProductStatus status);

    List<Product> findAllByStatus(ProductStatus status);
}
