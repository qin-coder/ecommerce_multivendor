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

/**
 * Authentication service implementation handling user registration,
 * login, and OTP-based authentication
 *
 * This service provides OTP-based authentication flow including:
 * - Sending OTP via email for login/signup
 * - User registration with OTP verification
 * - User login with OTP validation
 * - JWT token generation upon successful authentication
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Constants for configuration values
    private static final String SIGNING_PREFIX = "signing_";
    private static final String EMAIL_SUBJECT = "Xuwei Store Login/Signup Otp";
    private static final String EMAIL_TEXT = "your login otp is - ";

    // Dependency injections
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtProvider jwtProvider;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final CustomerUserServiceImpl customerUserService;

    /**
     * Sends login OTP to the specified email address
     * Handles both new user signup and existing user login
     *
     * @param email the email address to send OTP to
     * @throws RuntimeException if email sending fails or user not found for login
     */
    @Override
    public void sendLoginOtp(String email) {
        String processedEmail = processEmailInput(email);
        validateUserExistsForLogin(processedEmail);

        cleanupExistingOtp(processedEmail);
        String otp = generateAndSaveOtp(processedEmail);
        sendOtpEmail(processedEmail, otp);
    }

    /**
     * Creates a new user after validating the OTP
     * If user already exists, returns authentication token directly
     *
     * @param signupRequest contains user details and OTP for verification
     * @return JWT token for authenticated session
     * @throws RuntimeException if OTP is invalid
     */
    @Override
    public String createUser(SignupRequest signupRequest) {
        // Validate OTP before proceeding with user creation
        validateOtp(signupRequest.getEmail(), signupRequest.getOtp());

        // Find existing user or create new one
        User user = findOrCreateUser(signupRequest);

        // Create shopping cart for new users only
        if (user.getId() == null) {
            createUserCart(user);
        }

        // Generate and return authentication token
        return generateAuthToken(signupRequest.getEmail(), USER_ROLE.ROLE_CUSTOMER);
    }

    /**
     * Authenticates user using OTP and returns authentication response
     *
     * @param req login request containing email and OTP
     * @return authentication response with JWT token and user role
     * @throws BadCredentialsException if authentication fails
     */
    @Override
    public AuthResponse signing(LoginRequest req) {
        // Authenticate user credentials and OTP
        Authentication authentication = authenticate(req.getEmail(), req.getOtp());

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String token = jwtProvider.generateToken(authentication);

        // Build and return authentication response
        return buildAuthResponse(token, authentication);
    }

    // ============ PRIVATE HELPER METHODS ============

    /**
     * Processes email input by removing signing prefix if present
     *
     * @param email raw email input that may contain prefix
     * @return processed email without prefix
     */
    private String processEmailInput(String email) {
        return email.startsWith(SIGNING_PREFIX) ?
                email.substring(SIGNING_PREFIX.length()) : email;
    }

    /**
     * Validates that user exists when processing login request
     * Only validates for login requests (those with signing prefix)
     *
     * @param email the email to check
     * @throws RuntimeException if user not found for login request
     */
    private void validateUserExistsForLogin(String email) {
        // For login requests, verify user exists in database
        if (email.startsWith(SIGNING_PREFIX)) {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
        }
    }

    /**
     * Cleans up any existing OTP for the email before generating new one
     * Prevents multiple active OTPs for same email
     *
     * @param email email address to clean up OTPs for
     */
    private void cleanupExistingOtp(String email) {
        VerificationCode existingCode = verificationCodeRepository.findByEmail(email);
        if (existingCode != null) {
            verificationCodeRepository.delete(existingCode);
        }
    }

    /**
     * Generates new OTP and saves it to database
     *
     * @param email email address to associate with OTP
     * @return the generated OTP string
     */
    private String generateAndSaveOtp(String email) {
        String otp = OtpUtils.generateOTP();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(email);
        verificationCodeRepository.save(verificationCode);

        return otp;
    }

    /**
     * Sends OTP email to user
     *
     * @param email recipient email address
     * @param otp the OTP to send
     * @throws RuntimeException if email sending fails
     */
    private void sendOtpEmail(String email, String otp) {
        try {
            emailService.sendVerificationOtpEmail(email, otp, EMAIL_SUBJECT, EMAIL_TEXT);
        } catch (MessagingException | MailSendException e) {
            // Log and rethrow with user-friendly message
            throw new RuntimeException("Failed to send OTP email. Please try again later.", e);
        }
    }

    /**
     * Validates OTP against stored value in database
     *
     * @param email email address associated with OTP
     * @param otp the OTP to validate
     * @throws RuntimeException if OTP is invalid or expired
     */
    private void validateOtp(String email, String otp) {
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email);
        if (verificationCode == null || !verificationCode.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
    }

    /**
     * Finds existing user or creates new user from signup request
     *
     * @param request signup request containing user details
     * @return user entity (existing or newly created)
     */
    private User findOrCreateUser(SignupRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            user = createNewUser(request);
            user = userRepository.save(user);
        }
        return user;
    }

    /**
     * Creates new user entity from signup request
     *
     * @param request signup request containing user details
     * @return new user entity (not yet saved)
     */
    private User createNewUser(SignupRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(USER_ROLE.ROLE_CUSTOMER);
        user.setPhone("123456789");
        user.setPassword(passwordEncoder.encode(request.getOtp()));
        return user;
    }

    /**
     * Creates shopping cart for new user
     *
     * @param user the user to create cart for
     */
    private void createUserCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);
    }

    /**
     * Generates authentication token for user
     *
     * @param email user's email address
     * @param role user's role for authorization
     * @return JWT token string
     */
    private String generateAuthToken(String email, USER_ROLE role) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role.toString()));
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return jwtProvider.generateToken(auth);
    }

    /**
     * Builds authentication response with token and user details
     *
     * @param token JWT token
     * @param authentication Spring Security authentication object
     * @return authentication response DTO
     */
    private AuthResponse buildAuthResponse(String token, Authentication authentication) {
        String roleName = authentication.getAuthorities().iterator().next().getAuthority();

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Login successful");
        authResponse.setRole(USER_ROLE.valueOf(roleName));
        return authResponse;
    }

    /**
     * Authenticates user using email and OTP
     *
     * @param email user's email address
     * @param otp one-time password for verification
     * @return Spring Security authentication object
     * @throws BadCredentialsException if authentication fails
     */
    private Authentication authenticate(String email, String otp) {
        // Load user details from database
        UserDetails userDetails = customerUserService.loadUserByUsername(email);
        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username");
        }

        // Validate OTP
        validateOtp(email, otp);

        // Return authentication token with user authorities
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}