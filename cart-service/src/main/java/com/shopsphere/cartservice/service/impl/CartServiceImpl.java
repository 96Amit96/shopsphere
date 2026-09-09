package com.shopsphere.cartservice.service.impl;

import com.shopsphere.cartservice.client.ProductClient;
import com.shopsphere.cartservice.client.UserClient;
import com.shopsphere.cartservice.dto.request.CartItemRequest;
import com.shopsphere.cartservice.dto.response.*;
import com.shopsphere.cartservice.entity.Cart;
import com.shopsphere.cartservice.entity.CartItem;
import com.shopsphere.cartservice.exception.ProductNotFoundException;
import com.shopsphere.cartservice.exception.ResourceNotFoundException;
import com.shopsphere.cartservice.exception.UserNotFoundException;
import com.shopsphere.cartservice.repository.CartItemRepository;
import com.shopsphere.cartservice.repository.CartRepository;
import com.shopsphere.cartservice.service.CartService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public CartResponse addItemToCart(CartItemRequest request) {

        log.info("Adding item to the cart: {}", request);

        ApiResponse<CurrentUserResponse> currentUser = userClient.getCurrentUser();

        log.info("Current user :: {}", currentUser);

        Long userId = currentUser.data().id();

        log.info("UserId {}", userId);

        ApiResponse<ProductResponse> product   = productClient.getProductById(request.productId());

        log.info("Product :: {}", product);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(
                cart.getId(),
                request.productId()
        ).orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(
                    cartItem.getQuantity() + request.quantity()
            );
        } else {
            cartItem = new CartItem();

            cartItem.setProductId(product.data().id());
            cartItem.setProductName(product.data().name());
            cartItem.setUnitPrice(product.data().price());
            cartItem.setQuantity(request.quantity());

            cart.addItem(cartItem);

        }

        cartItemRepository.save(cartItem);

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse getCart() {

        ApiResponse<CurrentUserResponse> currentUser  = userClient.getCurrentUser();

        log.info("Current user :: {}", currentUser);

        Long userId = currentUser.data().id();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                            new ResourceNotFoundException("Cart not found for user :: " + userId)
                        );

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long productId, Integer quantity) {

        ApiResponse<CurrentUserResponse> currentUser = userClient.getCurrentUser();

        log.info("Current user :: {}", currentUser);

        Long userId = currentUser.data().id();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found for user :: " + userId
                        )
                );

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(
                cart.getId(),
                productId
        )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found in cart :: "+ productId
                        )
                );

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        log.info(
                "Cart item quantity updated: userId={}, productId={}, quantity={}",
                userId,
                productId,
                quantity
        );

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long productId) {

        ApiResponse<CurrentUserResponse> currentUser = userClient.getCurrentUser() ;

        log.info("Current user :: {}", currentUser);

        Long userId = currentUser.data().id();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found for user :: " + userId
                        )
                );

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found in cart :: "+ productId
                        )
                );

        cart.removeItem(cartItem);

        log.info(
                "Cart item removed: userId={}, productId={}",
                userId,
                productId
        );


    }

    @Override
    @Transactional
    public void clearCart() {

        ApiResponse<CurrentUserResponse> currentUser = userClient.getCurrentUser();

        log.info("Current user :: {}", currentUser);

        Long userId = currentUser.data().id();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found for user :: " + userId
                        )
                );

        cart.getItems().clear();

        log.info(
                "Cart cleared successfully: userId={}",
                userId
        );
    }


    private CartResponse mapToCartResponse(Cart cart) {

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(item -> {
                    BigDecimal totalPrice =
                            item.getUnitPrice()
                                    .multiply(
                                            BigDecimal.valueOf(
                                                    item.getQuantity()
                                            )
                                    );
                    return new CartItemResponse(
                            item.getId(),
                            item.getProductId(),
                            item.getProductName(),
                            item.getUnitPrice(),
                            item.getQuantity(),
                            totalPrice
                    );
                })
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::totalPrice)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                items,
                totalAmount
        );
    }


}
