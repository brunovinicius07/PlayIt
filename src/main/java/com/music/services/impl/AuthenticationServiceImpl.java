package com.music.services.impl;

import com.music.infra.kafka.KafkaProducerService;
import com.music.infra.security.TokenService;
import com.music.model.dto.request.AuthenticationRequest;
import com.music.model.dto.request.ForgotPasswordRequest;
import com.music.model.dto.request.RegisterRequest;
import com.music.model.dto.request.ResetPasswordRequest;
import com.music.model.dto.response.AuthenticationResponse;
import com.music.model.entity.User;
import com.music.model.enums.EmailType;
import com.music.model.event.EmailRequestEvent;
import com.music.model.exceptions.login.EmailPresentException;
import com.music.model.exceptions.login.VerifyCredential;
import com.music.model.exceptions.password.NewPasswordNoMatchException;
import com.music.model.exceptions.password.PasswordNoMatchException;
import com.music.model.exceptions.token.TokenNotFoundOrExpiredException;
import com.music.model.exceptions.user.EmailNotFoundException;
import com.music.model.exceptions.user.UserNotFoundException;
import com.music.model.mapper.UserMapper;
import com.music.repositories.UserRepository;
import com.music.role.UserRole;
import com.music.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final KafkaProducerService producer;

    @Value("${api.security.token.timeExpirationToChangePassword}")
    private long expirationTime;

    @Override
    @Transactional(readOnly = false)
    public AuthenticationResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmNewPassword())) {
            throw new PasswordNoMatchException();
        }

        User user;
        var optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isPresent()) {
            User existing = optionalUser.get();
            if (existing.isEnabled()) {
                throw new EmailPresentException();
            }
            user = existing;
            user.setNameUser(request.getNameUser());
            user.setPassword(this.passwordEncoder.encode(request.getPassword()));
            user.setRole(UserRole.ADMIN);
        } else {
            user = this.userMapper.registerDtoToUser(request);
            user.setPassword(this.passwordEncoder.encode(user.getPassword()));
            user.setRole(UserRole.ADMIN);
        }

        String code = String.valueOf(new SecureRandom().nextInt(900000) + 100000);
        user.setVerificationCode(code);
        user.setVerificationCodeExpiration(
                java.time.LocalDateTime.now().plusMinutes(expirationTime));
        user.setEnabled(false);

        userRepository.save(user);

        EmailRequestEvent event = new EmailRequestEvent(
                user.getEmail(),
                "Código de Verificação",
                EmailType.VERIFICATION_CODE,
                Map.of("name", user.getNameUser(),
                        "code", code,
                        "expiration", expirationTime + " minutos"));
        producer.sendEmailEvent(event);

        String token = tokenService.generateToken(user);
        AuthenticationResponse authenticationResponse = userMapper.userToAuthenticationResponse(user);
        authenticationResponse.setToken(token);

        return authenticationResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse login(AuthenticationRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            User user = (User) authentication.getPrincipal();

            String token = tokenService.generateToken(user);
            AuthenticationResponse authResponse = userMapper.userToAuthenticationResponse(user);
            authResponse.setToken(token);

            return authResponse;
        } catch (BadCredentialsException e) {
            throw new VerifyCredential();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String forgotPassword(ForgotPasswordRequest request) {
        String message = "Se o e-mail: " + request.getEmail()
                + " estiver cadastrado, enviaremos instruções para redefinir sua senha.";

        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = tokenService.generateResetToken(user);

            EmailRequestEvent event = new EmailRequestEvent(
                    user.getEmail(),
                    "Redefinição de Senha",
                    EmailType.FORGOT_PASSWORD,
                    Map.of("name", user.getNameUser(),
                            "token", token,
                            "expiration", "5 minutos"));
            producer.sendEmailEvent(event);
        });

        return message;
    }

    @Override
    @Transactional(readOnly = false)
    public String resetPassword(ResetPasswordRequest request) {
        if (!tokenService.isSimpleTokenValid(request.getToken())) {
            throw new TokenNotFoundOrExpiredException();
        }

        String email = tokenService.extractUsername(request.getToken());
        User user = userRepository.findByEmail(email).orElseThrow(EmailNotFoundException::new);

        if (request.getNewPassword().equals(request.getConfirmNewPassword())) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
        } else
            throw new NewPasswordNoMatchException();

        return "Senha redefinida com sucesso, faça login!";
    }

    @Override
    @Transactional(readOnly = false)
    public String verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email).orElseThrow(EmailNotFoundException::new);

        if (user.getVerificationCode() != null && user.getVerificationCode().equals(code)) {
            if (user.getVerificationCodeExpiration() != null
                    && java.time.LocalDateTime.now().isAfter(user.getVerificationCodeExpiration())) {
                throw new VerifyCredential("Código de verificação expirado.");
            }
            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiration(null);
            userRepository.save(user);
            return "E-mail verificado com sucesso!";
        } else {
            throw new VerifyCredential("Código de verificação inválido.");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public String resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(EmailNotFoundException::new);

        if (user.isEnabled()) {
            return "Este usuário já está ativado.";
        }

        String code = String.valueOf(new SecureRandom().nextInt(900000) + 100000);
        user.setVerificationCode(code);
        user.setVerificationCodeExpiration(
                java.time.LocalDateTime.now().plusMinutes(expirationTime));

        userRepository.save(user);

        EmailRequestEvent event = new EmailRequestEvent(
                user.getEmail(),
                "Reenvio de Código de Verificação",
                EmailType.VERIFICATION_CODE,
                Map.of("name", user.getNameUser(),
                        "code", code,
                        "expiration", expirationTime + " minutos"));
        producer.sendEmailEvent(event);

        return "Novo código enviado com sucesso!";
    }

    @Override
    @Transactional(readOnly = true)
    public void existingUser(String email) {
        userRepository.findByEmail(email).ifPresent(verify -> {
            throw new EmailPresentException();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public User validateUserById(Long IdUser) {
        return userRepository.findById(IdUser).orElseThrow(UserNotFoundException::new);
    }

}
