package com.music.controllers;

import com.music.model.dto.request.UpdateUserRequest;
import com.music.model.dto.response.UserResponseDto;
import com.music.model.entity.User;
import com.music.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController()
@RequestMapping(value = "v1/music/user")
public class UserController {

    private final UserService userService;

    @GetMapping("get/{idUser}")
    public ResponseEntity<UserResponseDto> getUserById(@AuthenticationPrincipal User user){
        var userResponse = userService.getUserById(user.getIdUser());
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("put/{idUser}")
    public ResponseEntity<UserResponseDto> updateUser(@AuthenticationPrincipal User user,
                                                      @RequestBody @Valid UpdateUserRequest updateUserRequest){
        var userResponse = userService.updateUser(user.getIdUser(), updateUserRequest);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("delete/{idUser}")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal User user){
        String message = userService.deleteUser(user.getIdUser());
        return ResponseEntity.ok(message);
    }

}
