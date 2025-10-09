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

    @PatchMapping("/verify/{otp}")
    public ResponseEntity<Seller> verifySellerEmail(@PathVariable String otp) throws Exception {


        VerificationCode verificationCode =
                verificationCodeRepository.findByOtp(otp);

        if (verificationCode == null || !verificationCode.getOtp().equals(otp)) {
            throw new Exception("wrong otp...");
        }

        Seller seller =
                sellerService.verifyEmail(verificationCode.getEmail(), otp);

        return new ResponseEntity<>(seller, HttpStatus.OK);
    }

    @PostMapping("/send/login-signup-otp")
    public ResponseEntity<ApiResponse> sentLoginOtp(@RequestBody VerificationCode req) throws Exception {
        Seller seller =
                sellerService.getSellerByEmail(req.getEmail());

        String otp = OtpUtils.generateOTP();
        VerificationCode verificationCode =
                verificationService.createVerificationCode(otp,
                        req.getEmail());

        String subject = "Xuwei Store Login Otp";
        String text = "your login otp is - ";
        emailService.sendVerificationOtpEmail(req.getEmail(),
                verificationCode.getOtp(), subject, text);

        ApiResponse res = new ApiResponse();
        res.setMessage("otp sent");
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PostMapping("/verify/login-otp")
    public ResponseEntity<AuthResponse> verifyLoginOtp(@RequestBody VerificationCode req) throws Exception {

        String otp = req.getOtp();
        String email = req.getEmail();
        VerificationCode verificationCode =
                verificationCodeRepository.findByEmail(email);

        if (verificationCode == null || !verificationCode.getOtp().equals(otp)) {
            throw new Exception("wrong otp...");
        }

        Authentication authentication = authenticate(req.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);
        AuthResponse authResponse = new AuthResponse();

        authResponse.setMessage("Login Success");
        authResponse.setJwt(token);
        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();


        String roleName = authorities.isEmpty() ? null :
                authorities.iterator().next().getAuthority();


        authResponse.setRole(USER_ROLE.valueOf(roleName));

        return new ResponseEntity<AuthResponse>(authResponse,
                HttpStatus.OK);
    }

    private Authentication authenticate(String username) {
        UserDetails userDetails =
                customerUserService.loadUserByUsername("seller_" + username);

        System.out.println("sign in userDetails - " + userDetails);

        if (userDetails == null) {
            System.out.println("sign in userDetails - null " + userDetails);
            throw new BadCredentialsException("Invalid username or " +
                    "password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails,
                null, userDetails.getAuthorities());
    }



}