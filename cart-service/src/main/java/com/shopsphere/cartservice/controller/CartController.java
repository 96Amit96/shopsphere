package com.shopsphere.cartservice.controller;

import com.shopsphere.cartservice.dto.request.CartItemRequest;
import com.shopsphere.cartservice.dto.request.UpdateCartItemRequest;
import com.shopsphere.cartservice.dto.response.ApiResponse;
import com.shopsphere.cartservice.dto.response.CartResponse;
import com.shopsphere.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(
            @Valid @RequestBody CartItemRequest request
            ) {

        CartResponse response = cartService.addItemToCart(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Product added to cart successfully",
                        response
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        CartResponse response = cartService.getCart();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart fetched successfully", response
                )
        );
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        CartResponse response =
                cartService.updateItemQuantity(
                        productId,
                        request.quantity()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart item quantity updated successfully",
                        response
                )
        );
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeItemFromCart(
            @PathVariable Long productId) {

        cartService.removeItemFromCart(productId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart item removed successfully",
                        null
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart() {

        cartService.clearCart();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart cleared successfully",
                        null
                )
        );
    }
}
