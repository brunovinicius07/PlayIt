package com.music.controllers;

import com.music.model.dto.request.RepertoireRequestDto;
import com.music.model.dto.response.RepertoireResponseDto;
import com.music.model.entity.User;
import com.music.services.RepertoireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController()
@RequestMapping(value = "v1/music/repertoire")
public class RepertoireController {

    private final RepertoireService repertoireService;

    @PostMapping("/post")
    ResponseEntity<RepertoireResponseDto> registerRepertoire(
            @RequestBody @Valid RepertoireRequestDto repertoireRequestDto){
        var repertoireResponse = repertoireService.createRepertoire(repertoireRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(repertoireResponse);
    }

    @PostMapping("/{idUser}")
    ResponseEntity<List<RepertoireResponseDto>> getAllRepertoireByIdUser(@AuthenticationPrincipal User user){
        var repertoireResponse = repertoireService.getAllRepertoireByIdUser(user.getIdUser());
        return ResponseEntity.ok(repertoireResponse);
    }

    @GetMapping("/{idRepertoire}")
    ResponseEntity<RepertoireResponseDto> getRepertoireByIdRepertoire(@PathVariable Long idRepertoire){
        var repertoireResponse = repertoireService.getRepertoireByIdRepertoire(idRepertoire);
        return ResponseEntity.ok(repertoireResponse);
    }

    @PutMapping("/put/{idRepertoire}")
    ResponseEntity<RepertoireResponseDto> updateRepertoire(@PathVariable Long idRepertoire, @RequestBody
                                                           @Valid RepertoireRequestDto repertoireRequestDto){
        var repertoireResponse = repertoireService.updateRepertoire(idRepertoire,repertoireRequestDto);
        return ResponseEntity.ok(repertoireResponse);
    }

    @DeleteMapping("/delete/{idRepertoire}")
    ResponseEntity<String> deleteRepertoire(@PathVariable Long idRepertoire){
        String message = repertoireService.deleteRepertoire(idRepertoire);
        return ResponseEntity.ok(message);
    }
}
