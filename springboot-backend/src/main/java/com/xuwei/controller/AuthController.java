package com.xuwei.controller;

import com.xuwei.domain.USER_ROLE;
import com.xuwei.repository.UserRepository;
import com.xuwei.response.AuthResponse;
import com.xuwei.response.SignupRequest;
import com.xuwei.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final AuthService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody SignupRequest signupRequest) {
        String token = userService.createUser(signupRequest);
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("User created successfully");
        authResponse.setRole(USER_ROLE.ROLE_CUSTOMER);
        return ResponseEntity.ok(authResponse);
    }
}
