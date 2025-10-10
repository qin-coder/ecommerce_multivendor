package com.xuwei.service.Impl;

import com.xuwei.converter.CartConverter;
import com.xuwei.converter.CartItemConverter;
import com.xuwei.dto.CartItemResponseDTO;
import com.xuwei.dto.CartResponseDTO;
import com.xuwei.exception.ProductException;
import com.xuwei.model.*;
import com.xuwei.repository.CartItemRepository;
import com.xuwei.repository.CartRepository;
import com.xuwei.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemConverter cartItemConverter;
    private final CartConverter cartConverter;

    @Override
    public Cart findUserCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId());
        updateCartTotals(cart);
        return cart;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO findUserCartWithDetails(User user) {
        Cart cart = cartRepository.findByUserId(user.getId());

        if (cart == null) {

            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
        }

        updateCartTotals(cart);


        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        Set<CartItemResponseDTO> cartItemDTOs = cartItems.stream()
                .map(cartItemConverter::convertToCartItemResponseDTO)
                .collect(Collectors.toSet());

        return cartConverter.convertToCartResponseDTO(cart, cartItemDTOs);
    }


    public static int calculateDiscountPercentage(double mrpPrice, double sellingPrice) {
        if (mrpPrice <= 0) {
            return 0;
        }
        double discount = mrpPrice - sellingPrice;
        double discountPercentage = (discount / mrpPrice) * 100;
        return (int) discountPercentage;
    }

    @Override
    @Transactional
    public CartItem addCartItem(User user, Product product, String size, int quantity) throws ProductException {
        Cart cart = cartRepository.findByUserId(user.getId());

        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
        }

        CartItem isPresent = cartItemRepository.findByCartAndProductAndSize(cart, product, size);

        if (isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUserId(user.getId());

            int totalPrice = quantity * product.getSellingPrice();
            cartItem.setSellingPrice(totalPrice);
            cartItem.setMrpPrice(quantity * product.getMrpPrice());
            cartItem.setSize(size);
            cartItem.setCart(cart);

            CartItem savedItem = cartItemRepository.save(cartItem);
            updateCartTotals(cart);
            return savedItem;
        } else {
            isPresent.setQuantity(isPresent.getQuantity() + quantity);
            isPresent.setMrpPrice(isPresent.getQuantity() * product.getMrpPrice());
            isPresent.setSellingPrice(isPresent.getQuantity() * product.getSellingPrice());

            CartItem updatedItem = cartItemRepository.save(isPresent);
            updateCartTotals(cart);
            return updatedItem;
        }
    }

    @Override
    @Transactional
    public CartItemResponseDTO addCartItemWithDetails(User user, Product product, String size, int quantity) throws ProductException {
        CartItem cartItem = addCartItem(user, product, size, quantity);
        return cartItemConverter.convertToCartItemResponseDTO(cartItem);
    }

    private void updateCartTotals(Cart cart) {
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        int totalPrice = 0;
        int totalDiscountedPrice = 0;
        int totalItemCount = 0;

        for (CartItem cartItem : cartItems) {
            totalPrice += cartItem.getMrpPrice();
            totalDiscountedPrice += cartItem.getSellingPrice();
            totalItemCount += cartItem.getQuantity();
        }

        cart.setTotalMrpPrice(totalPrice);
        cart.setTotalItems(cartItems.size());
        cart.setTotalSellingPrice(totalDiscountedPrice);
        cart.setDiscountedPrice(calculateDiscountPercentage(totalPrice, totalDiscountedPrice));
        cart.setTotalItems(totalItemCount);

        cartRepository.save(cart);
    }
}