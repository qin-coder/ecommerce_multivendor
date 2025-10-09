package com.xuwei.response;

import com.xuwei.domain.USER_ROLE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String jwt;

    private boolean status;

    private String message;

    private USER_ROLE role;
}