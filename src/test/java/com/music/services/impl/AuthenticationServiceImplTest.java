package com.music.services.impl;

import com.music.infra.kafka.KafkaProducerService;
import com.music.infra.security.TokenService;
import com.music.model.dto.request.AuthenticationRequest;
import com.music.model.dto.request.ForgotPasswordRequest;
import com.music.model.dto.request.RegisterRequest;
import com.music.model.dto.request.ResetPasswordRequest;
import com.music.model.dto.response.AuthenticationResponse;
import com.music.model.entity.User;
import com.music.model.entity.User;
import com.music.model.event.EmailRequestEvent;
import com.music.model.enums.EmailType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

        @Mock
        private UserMapper userMapper;
        @Mock
        private UserRepository userRepository;
        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private TokenService tokenService;
        @Mock
        private AuthenticationManager authenticationManager;
        @Mock
        private KafkaProducerService producer;

        @InjectMocks
        private AuthenticationServiceImpl authenticationService;

        private RegisterRequest registerRequest;
        private AuthenticationRequest authRequest;
        private ForgotPasswordRequest forgotPasswordRequest;
        private ResetPasswordRequest resetPasswordRequest;
        private User user;

        @BeforeEach
        void setup() {
                MockitoAnnotations.openMocks(this);

                registerRequest = new RegisterRequest(
                                "User71",
                                "user71@email.com",
                                "12345678",
                                "12345678");

                authRequest = new AuthenticationRequest("user71@email.com", "12345678");
                forgotPasswordRequest = new ForgotPasswordRequest("user71@email.com");
                resetPasswordRequest = new ResetPasswordRequest("token123", "abc", "abc");

                user = User.builder()
                                .idUser(1L)
                                .nameUser("User71")
                                .email("user71@email.com")
                                .password("encoded")
                                .role(UserRole.ADMIN)
                                .build();
        }

        @Test
        void shouldRegisterUserSuccessfully() {
                when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
                when(userMapper.registerDtoToUser(registerRequest)).thenReturn(user);
                when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
                when(tokenService.generateToken(user)).thenReturn("token123");

                when(userMapper.userToAuthenticationResponse(user))
                                .thenReturn(new AuthenticationResponse(null, 1L, "User71", UserRole.ADMIN));

                AuthenticationResponse response = authenticationService.register(registerRequest);

                assertNotNull(response);
                assertEquals("token123", response.getToken());
                assertEquals(UserRole.ADMIN, response.getRole());
                verify(userRepository).save(user);
                verify(producer).sendEmailEvent(argThat(event -> event.getType() == EmailType.VERIFICATION_CODE &&
                                event.getTo().equals("user71@email.com")));
        }

        @Test
        void shouldThrowEmailPresentException_WhenEmailAlreadyExistsAndEnabled() {
                user.setEnabled(true);
                when(userRepository.findByEmail(registerRequest.getEmail()))
                                .thenReturn(Optional.of(user));

                assertThrows(EmailPresentException.class, () -> authenticationService.register(registerRequest));

                verify(userRepository, never()).save(any());
        }

        @Test
        void shouldOverwriteUser_WhenEmailExistsButNotEnabled() {
                user.setEnabled(false);
                when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));
                when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPass");
                when(tokenService.generateToken(user)).thenReturn("newToken");

                when(userMapper.userToAuthenticationResponse(user))
                                .thenReturn(new AuthenticationResponse(null, 1L, "User71", UserRole.ADMIN));

                AuthenticationResponse response = authenticationService.register(registerRequest);

                assertNotNull(response);
                assertEquals("newToken", response.getToken());
                verify(userRepository).save(user); // Should save the existing user
                verify(producer).sendEmailEvent(any(EmailRequestEvent.class));
        }

        @Test
        void shouldThrowPasswordNoMatchException_WhenPasswordsDoNotMatchOnRegister() {
                RegisterRequest req = new RegisterRequest(
                                "User71",
                                "user71@email.com",
                                "abc",
                                "xyz");

                assertThrows(PasswordNoMatchException.class, () -> authenticationService.register(req));
        }

        @Test
        void shouldLoginSuccessfully() {
                Authentication authentication = mock(Authentication.class);
                when(authenticationManager.authenticate(any())).thenReturn(authentication);
                when(authentication.getPrincipal()).thenReturn(user);
                when(tokenService.generateToken(user)).thenReturn("tokenABC");

                when(userMapper.userToAuthenticationResponse(user))
                                .thenReturn(new AuthenticationResponse(null, 1L, "User71", UserRole.ADMIN));

                AuthenticationResponse response = authenticationService.login(authRequest);

                assertEquals("tokenABC", response.getToken());
        }

        @Test
        void shouldThrowVerifyCredential_WhenBadCredentials() {
                when(authenticationManager.authenticate(any()))
                                .thenThrow(new BadCredentialsException("Invalid"));

                assertThrows(VerifyCredential.class, () -> authenticationService.login(authRequest));
        }

        @Test
        void shouldSendForgotPasswordEvent_WhenUserExists() {
                when(userRepository.findByEmail(forgotPasswordRequest.getEmail()))
                                .thenReturn(Optional.of(user));
                when(tokenService.generateResetToken(eq(user)))
                                .thenReturn("tok123");

                String msg = authenticationService.forgotPassword(forgotPasswordRequest);

                assertTrue(msg.contains("Se o e-mail:"));
                verify(producer).sendEmailEvent(argThat(event -> event.getType() == EmailType.FORGOT_PASSWORD &&
                                event.getTo().equals("user71@email.com")));
        }

        @Test
        void shouldNotSendEvent_WhenUserDoesNotExist() {
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

                String msg = authenticationService.forgotPassword(forgotPasswordRequest);

                verify(producer, never()).sendEmailEvent(any());
                assertTrue(msg.contains("Se o e-mail:"));
        }

        @Test
        void shouldThrowException_WhenTokenInvalid() {
                when(tokenService.isSimpleTokenValid("token123")).thenReturn(false);

                assertThrows(TokenNotFoundOrExpiredException.class,
                                () -> authenticationService.resetPassword(resetPasswordRequest));
        }

        @Test
        void shouldThrowEmailNotFound_WhenUserDoesNotExist() {
                when(tokenService.isSimpleTokenValid(anyString())).thenReturn(true);
                when(tokenService.extractUsername(anyString())).thenReturn("email@email.com");
                when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

                assertThrows(EmailNotFoundException.class,
                                () -> authenticationService.resetPassword(resetPasswordRequest));
        }

        @Test
        void shouldThrowException_WhenPasswordsDoNotMatch() {
                ResetPasswordRequest req = new ResetPasswordRequest("tok", "abc", "xyz");

                when(tokenService.isSimpleTokenValid(anyString())).thenReturn(true);
                when(tokenService.extractUsername(anyString())).thenReturn(user.getEmail());
                when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

                assertThrows(NewPasswordNoMatchException.class, () -> authenticationService.resetPassword(req));
        }

        @Test
        void shouldResetPasswordSuccessfully() {
                when(tokenService.isSimpleTokenValid(anyString())).thenReturn(true);
                when(tokenService.extractUsername(anyString())).thenReturn(user.getEmail());
                when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
                when(passwordEncoder.encode(anyString())).thenReturn("encodedXYZ");

                String response = authenticationService.resetPassword(resetPasswordRequest);

                verify(userRepository).save(user);
                assertEquals("Senha redefinida com sucesso, faça login!", response);
        }

        @Test
        void shouldReturnUser_WhenUserExists() {
                when(userRepository.findById(1L)).thenReturn(Optional.of(user));

                assertEquals(user, authenticationService.validateUserById(1L));
        }

        @Test
        void shouldThrow_WhenUserDoesNotExist() {
                when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

                assertThrows(UserNotFoundException.class, () -> authenticationService.validateUserById(99L));
        }

        @Test
        void shouldVerifyEmailSuccessfully() {
                user.setVerificationCode("123456");
                user.setVerificationCodeExpiration(java.time.LocalDateTime.now().plusMinutes(5));
                user.setEnabled(false);
                when(userRepository.findByEmail("user71@email.com")).thenReturn(Optional.of(user));

                String result = authenticationService.verifyEmail("user71@email.com", "123456");

                assertEquals("E-mail verificado com sucesso!", result);
                assertTrue(user.isEnabled());
                assertNull(user.getVerificationCode());
                verify(userRepository).save(user);
        }

        @Test
        void shouldThrowVerifyCredential_WhenCodeIsInvalid() {
                user.setVerificationCode("123456");
                when(userRepository.findByEmail("user71@email.com")).thenReturn(Optional.of(user));

                assertThrows(VerifyCredential.class,
                                () -> authenticationService.verifyEmail("user71@email.com", "654321"));
        }

        @Test
        void shouldThrowVerifyCredential_WhenCodeIsExpired() {
                user.setVerificationCode("123456");
                user.setVerificationCodeExpiration(java.time.LocalDateTime.now().minusMinutes(1));
                when(userRepository.findByEmail("user71@email.com")).thenReturn(Optional.of(user));

                VerifyCredential exception = assertThrows(VerifyCredential.class,
                                () -> authenticationService.verifyEmail("user71@email.com", "123456"));
                assertEquals("401", exception.getErrorCode());
        }
}
