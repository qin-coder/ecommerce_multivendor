package com.xuwei.converter;

import com.xuwei.dto.CartItemResponseDTO;
import com.xuwei.model.*;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CartItemConverter {

    public CartItemResponseDTO convertToCartItemResponseDTO(CartItem cartItem) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setId(cartItem.getId());
        dto.setSize(cartItem.getSize());
        dto.setQuantity(cartItem.getQuantity());
        dto.setMrpPrice(cartItem.getMrpPrice());
        dto.setSellingPrice(cartItem.getSellingPrice());
        dto.setUserId(cartItem.getUserId());

        if (cartItem.getProduct() != null) {
            dto.setProduct(convertToProductDTO(cartItem.getProduct()));
        }

        return dto;
    }

    private CartItemResponseDTO.ProductDTO convertToProductDTO(Product product) {
        CartItemResponseDTO.ProductDTO productDTO = new CartItemResponseDTO.ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setTitle(product.getTitle());
        productDTO.setDescription(product.getDescription());
        productDTO.setMrpPrice(product.getMrpPrice());
        productDTO.setSellingPrice(product.getSellingPrice());
        productDTO.setDiscountPercent(product.getDiscountPercent());
        productDTO.setColor(product.getColor());
        productDTO.setImages(product.getImages());
        productDTO.setRating(product.getRating());
        productDTO.setSizes(product.getSizes());

        if (product.getSeller() != null) {
            productDTO.setSeller(convertToSellerDTO(product.getSeller()));
        }

        if (product.getCategory() != null) {
            productDTO.setCategory(convertToCategoryDTO(product.getCategory()));
        }

        return productDTO;
    }

    private CartItemResponseDTO.SellerDTO convertToSellerDTO(Seller seller) {
        CartItemResponseDTO.SellerDTO sellerDTO = new CartItemResponseDTO.SellerDTO();
        sellerDTO.setId(seller.getId());
        sellerDTO.setSellerName(seller.getSellerName());
        sellerDTO.setPhone(seller.getPhone());
        sellerDTO.setEmail(seller.getEmail());
        sellerDTO.setGSTIN(seller.getGSTIN());
        sellerDTO.setRole(seller.getRole());
        sellerDTO.setEmailVerified(seller.isEmailVerified());
        sellerDTO.setAccountStatus(seller.getAccountStatus());

        if (seller.getBusinessDetails() != null) {
            CartItemResponseDTO.BusinessDetailsDTO businessDetailsDTO = new CartItemResponseDTO.BusinessDetailsDTO();
            businessDetailsDTO.setBusinessName(seller.getBusinessDetails().getBusinessName());
            businessDetailsDTO.setBusinessEmail(seller.getBusinessDetails().getBusinessEmail());
            businessDetailsDTO.setBusinessPhone(seller.getBusinessDetails().getBusinessPhone());
            businessDetailsDTO.setBusinessAddress(seller.getBusinessDetails().getBusinessAddress());
            businessDetailsDTO.setLogo(seller.getBusinessDetails().getLogo());
            businessDetailsDTO.setBanner(seller.getBusinessDetails().getBanner());
            sellerDTO.setBusinessDetails(businessDetailsDTO);
        }

        if (seller.getBankDetails() != null) {
            CartItemResponseDTO.BankDetailsDTO bankDetailsDTO = new CartItemResponseDTO.BankDetailsDTO();
            bankDetailsDTO.setAccountNumber(seller.getBankDetails().getAccountNumber());
            bankDetailsDTO.setAccountHolderName(seller.getBankDetails().getAccountHolderName());
            bankDetailsDTO.setIfscCode(seller.getBankDetails().getIfscCode());
            sellerDTO.setBankDetails(bankDetailsDTO);
        }

        if (seller.getPickupAddress() != null) {
            sellerDTO.setPickupAddress(convertToAddressDTO(seller.getPickupAddress()));
        }

        return sellerDTO;
    }

    private CartItemResponseDTO.CategoryDTO convertToCategoryDTO(Category category) {
        CartItemResponseDTO.CategoryDTO categoryDTO = new CartItemResponseDTO.CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());
        categoryDTO.setCategoryId(category.getCategoryId());
        categoryDTO.setLevel(category.getLevel());

        if (category.getParentCategory() != null) {
            CartItemResponseDTO.CategorySimpleDTO parentCategoryDTO = new CartItemResponseDTO.CategorySimpleDTO();
            parentCategoryDTO.setId(category.getParentCategory().getId());
            parentCategoryDTO.setName(category.getParentCategory().getName());
            parentCategoryDTO.setCategoryId(category.getParentCategory().getCategoryId());
            parentCategoryDTO.setLevel(category.getParentCategory().getLevel());
            categoryDTO.setParentCategory(parentCategoryDTO);
        }

        return categoryDTO;
    }

    private CartItemResponseDTO.AddressDTO convertToAddressDTO(Address address) {
        CartItemResponseDTO.AddressDTO addressDTO = new CartItemResponseDTO.AddressDTO();
        addressDTO.setId(address.getId());
        addressDTO.setName(address.getName());
        addressDTO.setLocality(address.getLocality());
        addressDTO.setAddress(address.getAddress());
        addressDTO.setCity(address.getCity());
        addressDTO.setState(address.getState());
        addressDTO.setPinCode(address.getPinCode());
        addressDTO.setPhone(address.getPhone());
        return addressDTO;
    }
    public Set<CartItemResponseDTO> findCartItemsByCart(Cart cart) {

        return Set.of();
    }
}