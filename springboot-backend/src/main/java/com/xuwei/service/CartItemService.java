package com.xuwei.service;

import com.xuwei.dto.CartItemResponseDTO;
import com.xuwei.exception.CartItemException;
import com.xuwei.exception.UserException;
import com.xuwei.model.CartItem;

public interface CartItemService {
    CartItemResponseDTO updateCartItem(Long userId, Long id, CartItem cartItem) throws CartItemException;
    void deleteCartItem(Long userId, Long cartItemId) throws CartItemException, UserException;
    CartItem findCartItemById(Long cartItemId) throws CartItemException;
    CartItemResponseDTO findCartItemDTOById(Long cartItemId) throws CartItemException;
}