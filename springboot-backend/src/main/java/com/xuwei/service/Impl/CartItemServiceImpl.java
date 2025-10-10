package com.xuwei.service.Impl;

import com.xuwei.converter.CartItemConverter;
import com.xuwei.dto.CartItemResponseDTO;
import com.xuwei.exception.CartItemException;
import com.xuwei.exception.UserException;
import com.xuwei.model.*;
import com.xuwei.repository.CartItemRepository;
import com.xuwei.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartItemConverter cartItemConverter;

    @Override
    @Transactional
    public CartItemResponseDTO updateCartItem(Long userId, Long id, CartItem cartItem) throws CartItemException {
        CartItem item = findCartItemById(id);
        User cartItemUser = item.getCart().getUser();
        if (cartItemUser.getId().equals(userId)) {
            item.setQuantity(cartItem.getQuantity());
            item.setMrpPrice(item.getQuantity() * item.getProduct().getMrpPrice());
            item.setSellingPrice(item.getQuantity() * item.getProduct().getSellingPrice());
            CartItem updatedItem = cartItemRepository.save(item);
            return cartItemConverter.convertToCartItemResponseDTO(updatedItem);
        }
        throw new CartItemException("you can't update this item");
    }

    @Override
    public void deleteCartItem(Long userId, Long cartItemId) throws CartItemException, UserException {
        CartItem cartItem = findCartItemById(cartItemId);
        User cartItemUser = cartItem.getCart().getUser();
        if (cartItemUser.getId().equals(userId)) {
            cartItemRepository.deleteById(cartItem.getId());
            return;
        }
        throw new UserException("you can't delete this item");
    }

    @Override
    public CartItem findCartItemById(Long cartItemId) throws CartItemException {
        Optional<CartItem> opt = cartItemRepository.findById(cartItemId);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new CartItemException("cartItem not found with id : " + cartItemId);
    }

    @Override
    public CartItemResponseDTO findCartItemDTOById(Long cartItemId) throws CartItemException {
        CartItem cartItem = findCartItemById(cartItemId);
        return cartItemConverter.convertToCartItemResponseDTO(cartItem);
    }
}