package com.xuwei.dto;

import com.xuwei.domain.USER_ROLE;
import lombok.Data;
import java.util.Set;
import java.util.List;

@Data
public class CartResponseDTO {
    private Long id;
    private UserDTO user;
    private Set<CartItemResponseDTO> cartItems;
    private double totalSellingPrice;
    private int totalItems;
    private int totalMrpPrice;
    private double discountedPrice;
    private String couponCode;

    @Data
    public static class UserDTO {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
        private USER_ROLE role;
        private Set<AddressDTO> address;

        @Data
        public static class AddressDTO {
            private Long id;
            private String name;
            private String locality;
            private String address;
            private String city;
            private String state;
            private String pinCode;
            private String phone;
        }
    }
}