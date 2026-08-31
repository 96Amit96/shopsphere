package com.shopsphere.inventoryservice.entity;

import com.shopsphere.inventoryservice.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "inventories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_product",
                        columnNames = "product_id"
                )
        }
)
@Setter
@Getter
@NoArgsConstructor
public class Inventory extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Version
    private Long version;

    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
}
