package com.jady.appplatform.api.controller;

import com.jady.appplatform.api.dto.LoginRequest;
import com.jady.appplatform.api.dto.RegisterRequest;
import com.jady.appplatform.api.response.ApiResponse;
import com.jady.appplatform.common.exception.UnauthorizedException;
import com.jady.appplatform.domain.entity.User;
import com.jady.appplatform.repository.UserRepository;
import com.jady.appplatform.security.JwtUtil;

import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest req) {
        User user = new User(
                req.email,
                passwordEncoder.encode(req.password),
                "USER"
        );
        userRepository.save(user);
        return ApiResponse.success(null);
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginRequest req) {
        final String invalid = "Invalid credentials";

        var userOpt = userRepository.findByEmail(req.email);
        if(userOpt.isEmpty()) {
            throw new UnauthorizedException(invalid);
        }

        var user = userOpt.get();

        // 兼容“历史脏数据”（password 非 bcrypt 格式导致 matches 抛异常）
        try {
            boolean ok = passwordEncoder.matches(req.password, user.getPassword());
            if (!ok) {
                throw new UnauthorizedException(invalid);
            }
        } catch (Exception e) {
            throw new UnauthorizedException(invalid);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return ApiResponse.success(token);
    }
}
