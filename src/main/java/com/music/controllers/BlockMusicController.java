package com.music.controllers;

import com.music.model.dto.request.BlockMusicRequestDto;
import com.music.model.dto.request.MusicToBlockRequest;
import com.music.model.dto.response.BlockMusicDetailResponse;
import com.music.model.dto.response.BlockMusicResponseDto;
import com.music.model.entity.User;
import com.music.services.BlockMusicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController()
@RequestMapping(value = "v1/music/block_music")
public class BlockMusicController {

    private final BlockMusicService blockMusicService;

    @PostMapping("/post")
    public ResponseEntity<BlockMusicResponseDto> registerBlockMusic(
            @RequestBody @Valid BlockMusicRequestDto blockMusicRequestDto) {
        var blockMusicResponse = blockMusicService.createBlockMusic(blockMusicRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(blockMusicResponse);
    }

    @GetMapping("getAll/{idUser}")
    public ResponseEntity<List<BlockMusicResponseDto>> getAllBlockMusic(@AuthenticationPrincipal User user) {
        var blockMusicResponse = blockMusicService.getAllBlockMusic(user.getIdUser());
        return ResponseEntity.ok(blockMusicResponse);
    }

    @GetMapping("getAll/detail/{idUser}")
    public ResponseEntity<List<BlockMusicDetailResponse>> getAllBlockMusicDetail(@AuthenticationPrincipal User user) {
        var blockMusicResponse = blockMusicService.getAllBlockMusicDetail(user.getIdUser());
        return ResponseEntity.ok(blockMusicResponse);
    }

    @GetMapping("getById/{idBlockMusic}")
    public ResponseEntity<BlockMusicDetailResponse> getBlockMusicByIdBlockMusic(@PathVariable Long idBlockMusic) {
        var blockMusicResponse = blockMusicService.getBlockMusicByIdBlockMusic(idBlockMusic);
        return ResponseEntity.ok(blockMusicResponse);
    }

    @PutMapping("/put/{idBlockMusic}")
    public ResponseEntity<BlockMusicResponseDto> updateBlockMusic(@PathVariable Long idBlockMusic,
                                                                  @RequestBody @Valid
                                                              BlockMusicRequestDto blockMusicRequestDto) {
        var blockMusicResponse = blockMusicService.updateBlockMusic(idBlockMusic,blockMusicRequestDto);
        return ResponseEntity.ok(blockMusicResponse);
    }

    @PutMapping("/link-music-to-block")
    public ResponseEntity<BlockMusicDetailResponse> linkMusicToBLock(@RequestBody
                                                             MusicToBlockRequest musicToBlockRequest){

        var blockMusicResponse = blockMusicService.linkMusicToBLock(musicToBlockRequest);
        return ResponseEntity.ok(blockMusicResponse);
    }

    @DeleteMapping("/delete/{idBlockMusic}")
    public ResponseEntity<String> deleteBlockMusic(@PathVariable Long idBlockMusic) {
        String message = blockMusicService.deleteBlockMusic(idBlockMusic);
        return ResponseEntity.ok(message);
    }
}
