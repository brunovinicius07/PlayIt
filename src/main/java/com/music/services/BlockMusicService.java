package com.music.services;

import com.music.model.dto.request.BlockMusicRequestDto;
import com.music.model.dto.request.MusicToBlockRequest;
import com.music.model.dto.response.BlockMusicDetailResponse;
import com.music.model.dto.response.BlockMusicResponseDto;
import com.music.model.entity.BlockMusic;

import java.util.List;

public interface BlockMusicService {

    BlockMusicResponseDto createBlockMusic(BlockMusicRequestDto blockMusicRequestDto);

    List<BlockMusicResponseDto> getAllBlockMusic(Long idUser);

    List<BlockMusicDetailResponse> getAllBlockMusicDetail(Long idUser);

    BlockMusicDetailResponse getBlockMusicByIdBlockMusic(Long idBlockMusic);

    BlockMusicResponseDto updateBlockMusic(Long idBlockMusic, BlockMusicRequestDto blockMusicRequestDto);

    String deleteBlockMusic(Long idBlockMusic);

    List<BlockMusic> getBlockMusicsByIdBlockMusics(List<Long> idBlockMusics);

    BlockMusicDetailResponse linkMusicToBLock(MusicToBlockRequest musicToBlockRequest);

    void existingBlockMusic(String nameBlockMusic, Long idUser);

    BlockMusic validateBlockMusic(Long idBlockMusic);

}
