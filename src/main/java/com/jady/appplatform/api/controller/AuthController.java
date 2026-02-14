package com.jady.appplatform.api.controller;

import com.jady.appplatform.api.dto.LoginRequest;
import com.jady.appplatform.api.dto.RegisterRequest;
import com.jady.appplatform.api.dto.RegisterEmployerRequest;
import com.jady.appplatform.api.response.ApiResponse;
import com.jady.appplatform.common.exception.UnauthorizedException;
import com.jady.appplatform.domain.entity.User;
import com.jady.appplatform.domain.enums.UserRole;
import com.jady.appplatform.repository.UserRepository;
import com.jady.appplatform.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final String employerInviteCode;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            @Value("${app.employer.inviteCode}") String employerInviteCode
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.employerInviteCode = employerInviteCode;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest req) {
        User user = new User(
                req.email,
                passwordEncoder.encode(req.password),
                UserRole.USER
        );
        userRepository.save(user);
        return ApiResponse.success(null);
    }

    @PostMapping("/register-employer")
    public ApiResponse<Void> registerEmployer(@Valid @RequestBody RegisterEmployerRequest req) {
        if (!employerInviteCode.equals(req.inviteCode)) {
            // 语义更标准的是 403 Forbidden；你现在项目只有 UnauthorizedException(401)，先用它跑通流程
            throw new UnauthorizedException("Invalid invite code");
        }

        User user = new User(
                req.email,
                passwordEncoder.encode(req.password),
                UserRole.EMPLOYER
        );
        userRepository.save(user);
        return ApiResponse.success(null);
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginRequest req) {
        final String invalid = "Invalid credentials";

        var userOpt = userRepository.findByEmail(req.email);
        if (userOpt.isEmpty()) {
            throw new UnauthorizedException(invalid);
        }

        var user = userOpt.get();

        try {
            boolean ok = passwordEncoder.matches(req.password, user.getPassword());
            if (!ok) throw new UnauthorizedException(invalid);
        } catch (Exception e) {
            throw new UnauthorizedException(invalid);
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
        return ApiResponse.success(token);
    }
}