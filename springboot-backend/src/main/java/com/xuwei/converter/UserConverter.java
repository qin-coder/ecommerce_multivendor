package com.xuwei.converter;

import com.xuwei.dto.CartResponseDTO;
import com.xuwei.model.Address;
import com.xuwei.model.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserConverter {

    public CartResponseDTO.UserDTO convertToCartUserDTO(User user) {
        CartResponseDTO.UserDTO userDTO = new CartResponseDTO.UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFullName(user.getFullName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setRole(user.getRole());

        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            Set<CartResponseDTO.UserDTO.AddressDTO> addressDTOs = user.getAddress().stream()
                    .map(this::convertToUserAddressDTO)
                    .collect(Collectors.toSet());
            userDTO.setAddress(addressDTOs);
        }

        return userDTO;
    }

    private CartResponseDTO.UserDTO.AddressDTO convertToUserAddressDTO(Address address) {
        CartResponseDTO.UserDTO.AddressDTO addressDTO = new CartResponseDTO.UserDTO.AddressDTO();
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
}