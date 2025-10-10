package com.xuwei.dto;

import com.xuwei.domain.AccountStatus;
import com.xuwei.domain.USER_ROLE;
import lombok.Data;
import java.util.List;

@Data
public class CartItemResponseDTO {
    private Long id;
    private String size;
    private int quantity;
    private Integer mrpPrice;
    private Integer sellingPrice;
    private Long userId;
    private ProductDTO product;

    @Data
    public static class ProductDTO {
        private Long id;
        private String title;
        private String description;
        private int mrpPrice;
        private int sellingPrice;
        private int discountPercent;
        private String color;
        private List<String> images;
        private int rating;
        private String sizes;
        private SellerDTO seller;
        private CategoryDTO category;
    }

    @Data
    public static class SellerDTO {
        private Long id;
        private String sellerName;
        private String phone;
        private String email;
        private BusinessDetailsDTO businessDetails;
        private BankDetailsDTO bankDetails;
        private AddressDTO pickupAddress;
        private String GSTIN;
        private USER_ROLE role;
        private boolean emailVerified;
        private AccountStatus accountStatus;
    }

    @Data
    public static class CategoryDTO {
        private Long id;
        private String name;
        private String categoryId;
        private CategorySimpleDTO parentCategory;
        private Integer level;
    }

    @Data
    public static class CategorySimpleDTO {
        private Long id;
        private String name;
        private String categoryId;
        private Integer level;
    }

    @Data
    public static class BusinessDetailsDTO {
        private String businessName;
        private String businessEmail;
        private String businessPhone;
        private String businessAddress;
        private String logo;
        private String banner;
    }

    @Data
    public static class BankDetailsDTO {
        private String accountNumber;
        private String accountHolderName;
        private String ifscCode;
    }

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