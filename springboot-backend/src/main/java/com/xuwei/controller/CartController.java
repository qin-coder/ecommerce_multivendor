package com.xuwei.controller;

import com.xuwei.dto.CartItemResponseDTO;
import com.xuwei.dto.CartResponseDTO;
import com.xuwei.exception.CartItemException;
import com.xuwei.exception.ProductException;
import com.xuwei.exception.UserException;
import com.xuwei.model.*;
import com.xuwei.request.AddItemRequest;
import com.xuwei.response.ApiResponse;
import com.xuwei.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;
    private final CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<CartResponseDTO> findUserCartHandler(@RequestHeader("Authorization") String jwt) throws UserException {
        User user = userService.findUserByJwtToken(jwt);

        try {
            CartResponseDTO cart = cartService.findUserCartWithDetails(user);
            return new ResponseEntity<>(cart, HttpStatus.OK);
        } catch (Exception e) {
            CartResponseDTO simplifiedCart = new CartResponseDTO();
            simplifiedCart.setId(0L);
            simplifiedCart.setTotalItems(0);
            return new ResponseEntity<>(simplifiedCart, HttpStatus.OK);
        }
    }

    @PutMapping("/add")
    public ResponseEntity<CartItemResponseDTO> addItemToCart(@RequestBody AddItemRequest req,
                                                             @RequestHeader("Authorization") String jwt) throws UserException, ProductException {
        User user = userService.findUserByJwtToken(jwt);
        Product product = productService.getProductById(req.getProductId());

        CartItemResponseDTO item = cartService.addCartItemWithDetails(user, product, req.getSize(), req.getQuantity());
        return new ResponseEntity<>(item, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<ApiResponse> deleteCartItemHandler(
            @PathVariable Long cartItemId,
            @RequestHeader("Authorization") String jwt)
            throws CartItemException, UserException {
        User user = userService.findUserByJwtToken(jwt);
        cartItemService.deleteCartItem(user.getId(), cartItemId);

        ApiResponse res = new ApiResponse();
        res.setMessage("Item deleted successfully");
        return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<CartItemResponseDTO> updateCartItemHandler(
            @PathVariable Long cartItemId,
            @RequestBody CartItem cartItem,
            @RequestHeader("Authorization") String jwt)
            throws CartItemException, UserException {
        User user = userService.findUserByJwtToken(jwt);

        CartItemResponseDTO updatedCartItem = null;
        if (cartItem.getQuantity() > 0) {
            updatedCartItem = cartItemService.updateCartItem(user.getId(), cartItemId, cartItem);
        }

        return new ResponseEntity<>(updatedCartItem, HttpStatus.ACCEPTED);
    }
}