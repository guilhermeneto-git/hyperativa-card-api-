package com.hyperativa.card.service;

import com.hyperativa.card.dto.AuthResponse;
import com.hyperativa.card.dto.LoginRequest;
import com.hyperativa.card.model.User;
import com.hyperativa.card.repository.UserRepository;
import com.hyperativa.card.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }


    public AuthResponse login(LoginRequest request) {
        // Find user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Validate if user is active
        if (!user.getEnabled()) {
            throw new RuntimeException("User disabled");
        }

        // Generate token
        String token = tokenProvider.generateToken(user.getUsername(), user.getRole());

        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}

