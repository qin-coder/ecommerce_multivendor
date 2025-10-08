package com.xuwei.service.Impl;


import com.xuwei.config.JwtProvider;
import com.xuwei.domain.USER_ROLE;
import com.xuwei.model.Cart;
import com.xuwei.model.User;
import com.xuwei.repository.CartRepository;
import com.xuwei.repository.UserRepository;
import com.xuwei.response.SignupRequest;
import com.xuwei.service.AuthService;
import lombok.RequiredArgsConstructor;
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

    public String createUser(SignupRequest signupRequest) {
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
