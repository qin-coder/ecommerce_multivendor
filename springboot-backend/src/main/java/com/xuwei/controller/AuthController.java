package com.xuwei.controller;

import com.xuwei.domain.USER_ROLE;
import com.xuwei.model.VerificationCode;
import com.xuwei.request.LoginRequest;
import com.xuwei.response.ApiResponse;
import com.xuwei.response.AuthResponse;
import com.xuwei.request.SignupRequest;
import com.xuwei.service.AuthService;
import jakarta.mail.MessagingException;
import jdk.jshell.spi.ExecutionControl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sent/login-signup-otp")
    public ResponseEntity<ApiResponse> sentLoginOtp(
            @RequestBody VerificationCode req) throws MessagingException, ExecutionControl.UserException {

        authService.sendLoginOtp(req.getEmail());

        ApiResponse res = new ApiResponse();
        res.setMessage("otp sent");
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody SignupRequest signupRequest) {
        String token = authService.createUser(signupRequest);
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("User created successfully");
        authResponse.setRole(USER_ROLE.ROLE_CUSTOMER);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signing")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest loginRequest) throws Exception {

        AuthResponse authResponse = authService.signing(loginRequest);
        authResponse.setMessage("Login successful");
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }
}
