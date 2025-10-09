package com.xuwei.controller;

import com.xuwei.config.JwtProvider;
import com.xuwei.domain.AccountStatus;
import com.xuwei.domain.USER_ROLE;
import com.xuwei.model.Seller;
import com.xuwei.model.VerificationCode;
import com.xuwei.repository.VerificationCodeRepository;
import com.xuwei.response.ApiResponse;
import com.xuwei.response.AuthResponse;
import com.xuwei.service.EmailService;
import com.xuwei.service.Impl.CustomerUserServiceImpl;
import com.xuwei.service.SellerService;
import com.xuwei.service.VerificationService;
import com.xuwei.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * REST Controller for handling seller-related operations
 * Provides endpoints for seller registration, authentication,
 * profile management, and account operations
 */
@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationService verificationService;
    private final JwtProvider jwtProvider;
    private final CustomerUserServiceImpl customerUserService;


    @PostMapping
    public ResponseEntity<Seller> createSeller(@RequestBody Seller seller) throws Exception {
        Seller savedSeller = sellerService.createSeller(seller);

        String otp = OtpUtils.generateOTP();
        VerificationCode verificationCode =
                verificationService.createVerificationCode(otp,
                        seller.getEmail());

        String subject = "Xuwei Store Email Verification Code";
        String text = "Welcome to Xuwei Store, verify your account " +
                "using this link ";
        String frontend_url = "http://localhost:3000/verify-seller/";
        emailService.sendVerificationOtpEmail(seller.getEmail(),
                verificationCode.getOtp(), subject,
                text + frontend_url);
        return new ResponseEntity<>(savedSeller, HttpStatus.CREATED);
    }




}