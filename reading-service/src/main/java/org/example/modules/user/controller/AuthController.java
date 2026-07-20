package org.example.modules.user.controller;

import org.example.common.response.ResponseResult;
import org.example.modules.user.dto.AuthResponse;
import org.example.modules.user.dto.LoginRequest;
import org.example.modules.user.dto.RegisterRequest;
import org.example.modules.user.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseResult<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseResult.success(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseResult<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseResult.success(authService.register(request));
    }
}
