package com.xuwei.converter;

import com.xuwei.dto.CartItemResponseDTO;
import com.xuwei.dto.CartResponseDTO;
import com.xuwei.model.Address;
import com.xuwei.model.Cart;
import com.xuwei.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartConverter {

    private final CartItemConverter cartItemConverter;
    private final UserConverter userConverter;

    public CartResponseDTO convertToCartResponseDTO(Cart cart, Set<CartItemResponseDTO> cartItemDTOs) {
        CartResponseDTO dto = new CartResponseDTO();
        dto.setId(cart.getId());
        dto.setTotalSellingPrice(cart.getTotalSellingPrice());
        dto.setTotalItems(cart.getTotalItems());
        dto.setTotalMrpPrice(cart.getTotalMrpPrice());
        dto.setDiscountedPrice(cart.getDiscountedPrice());
        dto.setCouponCode(cart.getCouponCode());
        dto.setCartItems(cartItemDTOs);

        // 转换用户信息
        if (cart.getUser() != null) {
            dto.setUser(userConverter.convertToCartUserDTO(cart.getUser()));
        }

        return dto;
    }
}