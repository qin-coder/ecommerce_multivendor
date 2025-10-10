package com.xuwei.repository;

import com.xuwei.model.Cart;
import com.xuwei.model.CartItem;
import com.xuwei.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    CartItem findByCartAndProductAndSize(Cart cart, Product product, String size);
    List<CartItem> findByCart(Cart cart);
}
