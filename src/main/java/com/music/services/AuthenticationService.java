package com.music.services;

import com.music.model.dto.request.AuthenticationRequest;
import com.music.model.dto.request.ForgotPasswordRequest;
import com.music.model.dto.request.RegisterRequest;
import com.music.model.dto.request.ResetPasswordRequest;
import com.music.model.dto.response.AuthenticationResponse;
import com.music.model.entity.User;
import jakarta.validation.Valid;

public interface AuthenticationService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse login(AuthenticationRequest request);

    String forgotPassword(ForgotPasswordRequest request);

    String resetPassword(ResetPasswordRequest request);

    String verifyEmail(String email, String code);

    String resendVerificationCode(String email);

    void existingUser(String email);

    User validateUserById(Long IdUser);
}
