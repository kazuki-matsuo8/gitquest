package com.gitquest.backend.service;

import com.gitquest.backend.dto.auth.AuthResponse;
import com.gitquest.backend.dto.auth.LoginRequest;
import com.gitquest.backend.dto.auth.RegisterRequest;
import com.gitquest.backend.entity.User;
import com.gitquest.backend.repository.UserRepository;
import com.gitquest.backend.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("このメールアドレスはすでに使用されています");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("このユーザー名はすでに使用されています");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail());
        return AuthResponse.of(token, saved.getId(), saved.getUsername(), saved.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("メールアドレスまたはパスワードが正しくありません"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("メールアドレスまたはパスワードが正しくありません");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.of(token, user.getId(), user.getUsername(), user.getEmail());
    }
}
