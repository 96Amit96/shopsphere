package com.shopsphere.cartservice.service;

import com.shopsphere.cartservice.dto.request.CartItemRequest;
import com.shopsphere.cartservice.dto.response.CartResponse;

public interface CartService {

    CartResponse addItemToCart(CartItemRequest request);

    CartResponse getCart();

    CartResponse updateItemQuantity(Long productId , Integer quantity);

    void removeItemFromCart(Long productId);

    void clearCart();

}
