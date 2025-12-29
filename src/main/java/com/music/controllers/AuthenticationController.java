package com.music.controllers;

import com.music.model.dto.request.AuthenticationRequest;
import com.music.model.dto.request.ForgotPasswordRequest;
import com.music.model.dto.request.RegisterRequest;
import com.music.model.dto.request.ResetPasswordRequest;
import com.music.model.dto.response.AuthenticationResponse;
import com.music.services.impl.AuthenticationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/music/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationServiceImpl service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest request) {
        var authenticationResponse = service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest request) {
        var authenticationResponse = service.login(request);
        return ResponseEntity.ok(authenticationResponse);
    }

    @PutMapping("/forgotPassword")
    public ResponseEntity<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        var message = service.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        var message = service.resetPassword(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String code) {
        var message = service.verifyEmail(email, code);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/resend-code")
    public ResponseEntity<String> resendVerificationCode(@RequestParam String email) {
        var message = service.resendVerificationCode(email);
        return ResponseEntity.ok(message);
    }

}
