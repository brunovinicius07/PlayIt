package com.music.controllers;

import com.music.model.dto.request.AddMusicRequest;
import com.music.model.dto.request.UpdateToneRequest;
import com.music.model.dto.response.UserMusicDetailResponse;
import com.music.model.dto.response.UserMusicResponse;
import com.music.model.entity.User;
import com.music.services.MusicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController()
@RequestMapping(value = "v1/music/musics")
public class MusicController {

    private final MusicService service;

    @PostMapping("/cipher")
    public ResponseEntity<UserMusicDetailResponse> addMusicFromCipherUrl(
            @RequestBody @Valid AddMusicRequest request,
            @AuthenticationPrincipal User user) {

        var response = service.addMusicFromCipherUrl(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/library")
    public ResponseEntity<List<UserMusicResponse>> getMyLibrary(@AuthenticationPrincipal User user) {
        var library = service.getAllUserMusics(user.getIdUser());
        return ResponseEntity.ok(library);
    }

    @GetMapping("/library/{idUserMusic}")
    public ResponseEntity<UserMusicDetailResponse> getMusicDetail(
            @PathVariable Long idUserMusic,
            @AuthenticationPrincipal User user) {

        var detail = service.getUserMusicDetail(idUserMusic, user.getIdUser());
        return ResponseEntity.ok(detail);
    }

    @PatchMapping("/library/{idUserMusic}/tone")
    public ResponseEntity<UserMusicDetailResponse> updateTone(
            @PathVariable Long idUserMusic,
            @RequestBody @Valid UpdateToneRequest request,
            @AuthenticationPrincipal User user) {

        var response = service.updatePersonalTone(idUserMusic, request, user.getIdUser());
        return ResponseEntity.ok(response);
    }
}
