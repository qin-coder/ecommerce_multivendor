package com.xuwei.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xuwei.domain.USER_ROLE;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private USER_ROLE role = USER_ROLE.ROLE_CUSTOMER;
    private Set<Address> address = new HashSet<>();
    private Set<Coupon> usedCoupons = new HashSet<>();
;
}
