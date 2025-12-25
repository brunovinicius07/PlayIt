package com.music.services.impl;

import com.music.model.dto.request.UpdateUserRequest;
import com.music.model.dto.response.UserResponseDto;
import com.music.model.entity.User;
import com.music.model.exceptions.password.CurrentPasswordWrongException;
import com.music.model.exceptions.password.NewPasswordNoMatchException;
import com.music.model.exceptions.user.UserNotFoundException;
import com.music.model.mapper.UserMapper;
import com.music.repositories.MusicRepository;
import com.music.repositories.RepertoireRepository;
import com.music.repositories.UserRepository;
import com.music.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MusicRepository musicRepository;
    private final RepertoireRepository repertoireRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long idUser) {
        return userMapper.userToResponseDto(validateUser(idUser));
    }

    @Override
    @Transactional(readOnly = false)
    public UserResponseDto updateUser(Long idUser, UpdateUserRequest updateUserRequest) {

        User user = validateUser(idUser);

        user.setNameUser(updateUserRequest.getNameUser());
        user.setEmail(updateUserRequest.getEmail());

        if(updateUserRequest.isChangePassword()){
            boolean isCurrentPasswordMatches = passwordEncoder.matches(
                    updateUserRequest.getCurrentPassword(),
                    user.getPassword()
            );

            boolean isNewPassWordMatches = updateUserRequest.getNewPassword()
                    .equals(updateUserRequest.getConfirmNewPassword());

            if(isCurrentPasswordMatches && isNewPassWordMatches){
                user.setPassword(passwordEncoder.encode(updateUserRequest.getNewPassword()));
            }
            else if (!isCurrentPasswordMatches) throw new CurrentPasswordWrongException();
            else throw new NewPasswordNoMatchException();
        }

        return userMapper.userToResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = false)
    public String deleteUser(Long idUser) {
        User user = validateUser(idUser);

        // O CascadeType.ALL nas entidades User -> UserMusic e User -> Repertoire
        // garante que os dados relacionados sejam excluídos automaticamente.
        // Não devemos deletar do musicRepository pois as músicas são globais.

        userRepository.delete(user);
        return "Usuário com o id " + idUser + " apagado com sucesso!";
    }

    @Override
    @Transactional(readOnly = true)
    public User validateUser(Long idUser) {
        return userRepository.findById(idUser).orElseThrow(UserNotFoundException::new);
    }

}
