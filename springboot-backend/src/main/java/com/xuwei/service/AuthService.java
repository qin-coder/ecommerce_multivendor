package com.xuwei.service;

import com.xuwei.response.AuthResponse;
import com.xuwei.response.SignupRequest;

public interface AuthService {
    void sendLoginOtp(String email);
    String createUser(SignupRequest signupRequest);


}
