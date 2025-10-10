package com.xuwei.service;

import com.xuwei.dto.CartItemResponseDTO;
import com.xuwei.dto.CartResponseDTO;
import com.xuwei.exception.ProductException;
import com.xuwei.model.Cart;
import com.xuwei.model.CartItem;
import com.xuwei.model.User;
import com.xuwei.model.Product;

public interface CartService {
    Cart findUserCart(User user);
    CartResponseDTO findUserCartWithDetails(User user);
    CartItem addCartItem(User user, Product product, String size, int quantity) throws ProductException;
    CartItemResponseDTO addCartItemWithDetails(User user, Product product, String size, int quantity) throws ProductException;
}