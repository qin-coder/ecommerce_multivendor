package com.xuwei.request;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String fullName;
    private String otp;

}
