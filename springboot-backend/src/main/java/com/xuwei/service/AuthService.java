package com.xuwei.service;

import com.xuwei.response.SignupRequest;

public interface AuthService {
    String createUser(SignupRequest signupRequest);
}
