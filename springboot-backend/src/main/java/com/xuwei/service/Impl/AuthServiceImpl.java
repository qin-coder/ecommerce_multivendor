package com.xuwei.service.Impl;


import com.xuwei.config.JwtProvider;
import com.xuwei.domain.USER_ROLE;
import com.xuwei.model.Cart;
import com.xuwei.model.User;
import com.xuwei.model.VerificationCode;
import com.xuwei.repository.CartRepository;
import com.xuwei.repository.UserRepository;
import com.xuwei.repository.VerificationCodeRepository;
import com.xuwei.response.SignupRequest;
import com.xuwei.service.AuthService;
import com.xuwei.service.EmailService;
import com.xuwei.utils.OtpUtils;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtProvider jwtProvider;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;


    @Override
    public void sendLoginOtp(String email) {
        String SIGNING_PREFIX = "signing_";

        if (email.startsWith(SIGNING_PREFIX)) {
            email = email.substring(SIGNING_PREFIX.length());
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
        }
        VerificationCode isExist = verificationCodeRepository.findByEmail(email);
        if (isExist != null) {
            verificationCodeRepository.delete(isExist);
        }
        String otp = OtpUtils.generateOTP();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(email);
        verificationCodeRepository.save(verificationCode);

        String subject = "Xuwei Store Login/Signup Otp";
        String text = "your login otp is - ";
        try {
            emailService.sendVerificationOtpEmail(email, otp, subject, text);
        } catch (MessagingException | MailSendException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send OTP email. Please try again later.");
        }
    }

    public String createUser(SignupRequest signupRequest) {

        VerificationCode verificationCode = verificationCodeRepository.findByEmail(signupRequest.getEmail());
        if (verificationCode == null || !verificationCode.getOtp().equals(signupRequest.getOtp())) {
            System.out.println("🔍 数据库中的 OTP: " + verificationCode.getOtp());
            System.out.println("🔍 用户提交的 OTP: " + signupRequest.getOtp());
            throw new RuntimeException("Invalid OTP");
        }
        User user =
                userRepository.findByEmail(signupRequest.getEmail());
        if (user == null) {
            User createUser = new User();
            createUser.setEmail(signupRequest.getEmail());
            createUser.setFullName(signupRequest.getFullName());
            createUser.setRole(USER_ROLE.ROLE_CUSTOMER);
            createUser.setPhone("123456789");
            createUser.setPassword(passwordEncoder.encode(signupRequest.getOtp()));
            user = userRepository.save(createUser);
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(USER_ROLE.ROLE_CUSTOMER.toString()));
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(signupRequest.getEmail(), null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtProvider.generateToken(authentication);
    }



}
