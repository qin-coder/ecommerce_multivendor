package com.xuwei.service.Impl;

import com.xuwei.config.JwtProvider;
import com.xuwei.domain.USER_ROLE;
import com.xuwei.model.Cart;
import com.xuwei.model.User;
import com.xuwei.model.VerificationCode;
import com.xuwei.repository.CartRepository;
import com.xuwei.repository.UserRepository;
import com.xuwei.repository.VerificationCodeRepository;
import com.xuwei.request.LoginRequest;
import com.xuwei.request.SignupRequest;
import com.xuwei.response.AuthResponse;
import com.xuwei.service.AuthService;
import com.xuwei.service.EmailService;
import com.xuwei.utils.OtpUtils;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String SIGNING_PREFIX = "signing_";
    private static final String EMAIL_SUBJECT = "Xuwei Store Login/Signup Otp";
    private static final String EMAIL_TEXT = "your login otp is - ";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtProvider jwtProvider;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final CustomerUserServiceImpl customerUserService;

    @Override
    public void sendLoginOtp(String email) {
        String processedEmail = processEmailInput(email);
        validateUserExistsForLogin(processedEmail);

        cleanupExistingOtp(processedEmail);
        String otp = generateAndSaveOtp(processedEmail);
        sendOtpEmail(processedEmail, otp);
    }

    @Override
    public String createUser(SignupRequest signupRequest) {
        validateOtp(signupRequest.getEmail(), signupRequest.getOtp());

        User user = findOrCreateUser(signupRequest);
        if (user.getId() == null) {
            createUserCart(user);
        }

        return generateAuthToken(signupRequest.getEmail(), USER_ROLE.ROLE_CUSTOMER);
    }

    @Override
    public AuthResponse signing(LoginRequest req) {
        Authentication authentication = authenticate(req.getEmail(), req.getOtp());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);
        return buildAuthResponse(token, authentication);
    }

    private String processEmailInput(String email) {
        return email.startsWith(SIGNING_PREFIX) ? email.substring(SIGNING_PREFIX.length()) : email;
    }

    private void validateUserExistsForLogin(String email) {
        if (email.startsWith(SIGNING_PREFIX)) {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
        }
    }

    private void cleanupExistingOtp(String email) {
        VerificationCode existingCode = verificationCodeRepository.findByEmail(email);
        if (existingCode != null) {
            verificationCodeRepository.delete(existingCode);
        }
    }

    private String generateAndSaveOtp(String email) {
        String otp = OtpUtils.generateOTP();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(email);
        verificationCodeRepository.save(verificationCode);
        return otp;
    }

    private void sendOtpEmail(String email, String otp) {
        try {
            emailService.sendVerificationOtpEmail(email, otp, EMAIL_SUBJECT, EMAIL_TEXT);
        } catch (MessagingException | MailSendException e) {
            throw new RuntimeException("Failed to send OTP email. Please try again later.", e);
        }
    }

    private void validateOtp(String email, String otp) {
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email);
        if (verificationCode == null || !verificationCode.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
    }

    private User findOrCreateUser(SignupRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            user = createNewUser(request);
            user = userRepository.save(user);
        }
        return user;
    }

    private User createNewUser(SignupRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(USER_ROLE.ROLE_CUSTOMER);
        user.setPhone("123456789"); // 建议在SignupRequest中添加phone字段
        user.setPassword(passwordEncoder.encode(request.getOtp()));
        return user;
    }

    private void createUserCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);
    }

    private String generateAuthToken(String email, USER_ROLE role) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.toString()));
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return jwtProvider.generateToken(auth);
    }

    private AuthResponse buildAuthResponse(String token, Authentication authentication) {
        String roleName = authentication.getAuthorities().iterator().next().getAuthority();

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Login successful");
        authResponse.setRole(USER_ROLE.valueOf(roleName));
        return authResponse;
    }

    private Authentication authenticate(String email, String otp) {
        UserDetails userDetails = customerUserService.loadUserByUsername(email);
        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username");
        }

        validateOtp(email, otp);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}