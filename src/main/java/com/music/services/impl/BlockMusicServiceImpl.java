package com.music.services.impl;

import com.music.model.dto.request.BlockMusicRequestDto;
import com.music.model.dto.request.MusicToBlockRequest;
import com.music.model.dto.response.BlockMusicDetailResponse;
import com.music.model.dto.response.BlockMusicResponseDto;
import com.music.model.entity.BlockMusic;
import com.music.model.entity.BlockMusicItem;
import com.music.model.entity.UserMusic;
import com.music.model.exceptions.blockMusic.BlockMusicIsPresentException;
import com.music.model.exceptions.blockMusic.BlockMusicNotFoundException;
import com.music.model.mapper.BlockMusicMapper;
import com.music.model.mapper.MusicMapper;
import com.music.repositories.BlockMusicRepository;
import com.music.repositories.MusicRepository;
import com.music.repositories.UserMusicRepository;
import com.music.services.BlockMusicService;
import com.music.services.RepertoireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BlockMusicServiceImpl implements BlockMusicService {

    private final BlockMusicRepository blockMusicRepository;
    private final BlockMusicMapper blockMusicMapper;
    private final RepertoireService repertoireService;
    private final MusicRepository musicRepository;
    private final MusicMapper musicMapper;
    private final UserMusicRepository userMusicRepository;

    @Override
    @Transactional(readOnly = false)
    public BlockMusicResponseDto createBlockMusic(BlockMusicRequestDto blockMusicRequestDto) {
        existingBlockMusic(blockMusicRequestDto.getNameBlockMusic(),blockMusicRequestDto.getIdRepertoire());
        BlockMusic blockMusic = blockMusicMapper.toBlockMusic(blockMusicRequestDto);
        var repertoire = repertoireService.validateRepertoire(blockMusicRequestDto.getIdRepertoire());
        blockMusic.setRepertoire(repertoire);

        return blockMusicMapper.toBlockMusicResponseDto(blockMusicRepository.save(blockMusic));
    }

    @Override
    @Transactional(readOnly = true)
    public BlockMusicDetailResponse getBlockMusicByIdBlockMusic(Long idBlockMusic) {
        BlockMusic blockMusic = validateBlockMusic(idBlockMusic);
        return blockMusicMapper.toBlockMusicDetailResponse(blockMusic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockMusicResponseDto> getAllBlockMusic(Long idUser) {
        List<BlockMusic> blockMusicList = blockMusicRepository.findAllBlockMusicByUserIdUser(idUser);
        if (blockMusicList.isEmpty()) throw new BlockMusicNotFoundException();

        return blockMusicMapper.toListBlockMusicResponseDto(blockMusicList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockMusicDetailResponse> getAllBlockMusicDetail(Long idUser) {
        List<BlockMusic> blockMusicList = blockMusicRepository.findAllBlockMusicByUserIdUser(idUser);
        if (blockMusicList.isEmpty()) throw new BlockMusicNotFoundException();

        return blockMusicMapper.toListBlockMusicDetailResponse(blockMusicList);
    }

    @Override
    @Transactional(readOnly = false)
    public BlockMusicResponseDto updateBlockMusic(Long idBlockMusic, BlockMusicRequestDto blockMusicRequestDto) {
        existingBlockMusic(blockMusicRequestDto.getNameBlockMusic(),blockMusicRequestDto.getIdRepertoire());
        BlockMusic blockMusic = validateBlockMusic(idBlockMusic);
        blockMusic.setNameBlockMusic(blockMusicRequestDto.getNameBlockMusic() != null
                ? blockMusicRequestDto.getNameBlockMusic() : blockMusic.getNameBlockMusic());

        return blockMusicMapper.toBlockMusicResponseDto(blockMusicRepository.save(blockMusic));
    }

    @Override
    @Transactional(readOnly = false)
    public String deleteBlockMusic(Long idBlockMusic) {
        BlockMusic blockMusic = validateBlockMusic(idBlockMusic);
        blockMusicRepository.delete(blockMusic);
        return "Bloco com ID " + idBlockMusic + " excluído com sucesso!";
    }

    @Override
    @Transactional(readOnly = false)
    public List<BlockMusic> getBlockMusicsByIdBlockMusics(List<Long> idBlockMusics) {
        List<BlockMusic> blockMusics = new ArrayList<>();
        for (Long item : idBlockMusics) {
            Optional<BlockMusic> blockMusic = blockMusicRepository.findById(item);
            blockMusic.ifPresent(blockMusics::add);
        }
        return blockMusics;
    }

    @Override
    @Transactional(readOnly = false)
    public BlockMusicDetailResponse linkMusicToBLock(MusicToBlockRequest musicToBlockRequest) {
        UserMusic userMusic = userMusicRepository.findById(musicToBlockRequest.getIdUserMusic())
                .orElseThrow(() -> new RuntimeException("Música não encontrada na biblioteca (ID: " + musicToBlockRequest.getIdUserMusic() + ")"));

        BlockMusic blockMusic = validateBlockMusic(musicToBlockRequest.getIdBlockMusic());

        if (!blockMusic.getRepertoire().getUser().getIdUser().equals(userMusic.getUser().getIdUser())) {
            throw new RuntimeException("Erro: Tentativa de adicionar música de um usuário em bloco de outro.");
        }

        boolean exists = blockMusic.getItems().stream()
                .anyMatch(item -> item.getUserMusic().equals(userMusic));

        if (!exists) {
            BlockMusicItem newItem = BlockMusicItem.builder()
                    .blockMusic(blockMusic)
                    .userMusic(userMusic)
                    .build();
            
            blockMusic.addItem(newItem);
            blockMusic = blockMusicRepository.save(blockMusic);
        }

        return blockMusicMapper.toBlockMusicDetailResponse(blockMusic);
    }

    @Override
    @Transactional(readOnly = true)
    public void existingBlockMusic(String nameBlockMusic, Long idUser) {
        blockMusicRepository.findBlockMusicByNameBlockMusicAndRepertoireIdRepertoire(nameBlockMusic, idUser)
                .ifPresent(blockMusic -> {throw new BlockMusicIsPresentException();
                });
    }

    @Override
    @Transactional(readOnly = true)
    public BlockMusic validateBlockMusic(Long idBlockMusic) {
        return blockMusicRepository.findById(idBlockMusic).orElseThrow(BlockMusicNotFoundException::new);
    }

}
