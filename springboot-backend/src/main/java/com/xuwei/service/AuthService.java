package com.xuwei.service;

import com.xuwei.request.LoginRequest;
import com.xuwei.response.AuthResponse;
import com.xuwei.request.SignupRequest;

public interface AuthService {
    void sendLoginOtp(String email);
    String createUser(SignupRequest signupRequest);
    AuthResponse signing(LoginRequest req);


}
