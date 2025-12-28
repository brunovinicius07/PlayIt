package com.music.services.impl;

import com.music.model.dto.request.UpdateUserRequest;
import com.music.model.dto.response.UserResponseDto;
import com.music.model.entity.User;
import com.music.model.exceptions.password.CurrentPasswordWrongException;
import com.music.model.exceptions.password.NewPasswordNoMatchException;
import com.music.model.exceptions.user.UserNotFoundException;
import com.music.model.mapper.UserMapper;
import com.music.repositories.*;
import com.music.role.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private MusicRepository musicRepository;
    @Mock
    private RepertoireRepository repertoireRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponseDto responseDto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .idUser(1L)
                .nameUser("User71")
                .email("user71@email.com")
                .password("senhaCriptografada")
                .role(UserRole.ADMIN)
                .myLibrary(List.of())
                .repertoires(List.of())
                .build();

        responseDto = new UserResponseDto(1L, "Novo Nome", "novo@email.com", UserRole.ADMIN);
    }

    @Test
    void shouldUpdateUserWithoutChangingPassword() {
        UpdateUserRequest request = new UpdateUserRequest(
                "Novo Nome",
                "novo@email.com",
                false,
                null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.userToResponseDto(any())).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(1L, request);

        assertNotNull(result);
        assertEquals("Novo Nome", user.getNameUser());
        assertEquals("novo@email.com", user.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository).save(user);
    }

    @Test
    void shouldUpdateUserAndChangePasswordSuccessfully() {
        UpdateUserRequest passwordRequest = new UpdateUserRequest(
                "User71",
                "user71@email.com",
                true,
                "senha atual",
                "senha nova",
                "senha nova");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha atual", "senhaCriptografada")).thenReturn(true);
        when(passwordEncoder.encode("senha nova")).thenReturn("senha nova criptografada");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.userToResponseDto(any())).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(1L, passwordRequest);

        assertNotNull(result);
        verify(passwordEncoder).encode("senha nova");
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowException_WhenCurrentPasswordIsWrong() {
        UpdateUserRequest invalidRequest = new UpdateUserRequest(
                "User71",
                "user71@email.com",
                true,
                "senha atual",
                "nova senha",
                "nova senha");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha atual", "senhaCriptografada")).thenReturn(false);

        assertThrows(CurrentPasswordWrongException.class,
                () -> userService.updateUser(1L, invalidRequest));

        verify(passwordEncoder).matches("senha atual", "senhaCriptografada");
    }

    @Test
    void shouldThrowException_WhenNewPasswordDoesNotMatch() {
        UpdateUserRequest invalidRequest = new UpdateUserRequest(
                "User71",
                "user71@email.com",
                true,
                "senha atual",
                "senha nova",
                "senha diferente");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha atual", "senhaCriptografada")).thenReturn(true);

        assertThrows(NewPasswordNoMatchException.class,
                () -> userService.updateUser(1L, invalidRequest));

        verify(passwordEncoder).matches("senha atual", "senhaCriptografada");
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String result = userService.deleteUser(1L);

        assertTrue(result.contains("apagado com sucesso"));

        verify(userRepository).delete(user);
    }

    @Test
    void shouldReturnUser_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.validateUser(1L);

        assertEquals(user, found);
    }

    @Test
    void shouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.validateUser(99L));
    }

    @Test
    void shouldThrowException_WhenUserNotFound_InGetUserById() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(99L));
    }
}
