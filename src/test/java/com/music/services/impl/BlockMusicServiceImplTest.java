package com.music.services.impl;

import com.music.model.dto.request.BlockMusicRequestDto;
import com.music.model.dto.request.MusicToBlockRequest;
import com.music.model.dto.response.BlockMusicDetailResponse;
import com.music.model.dto.response.BlockMusicResponseDto;
import com.music.model.entity.*;
import com.music.model.exceptions.blockMusic.BlockMusicNotFoundException;
import com.music.model.mapper.BlockMusicMapper;
import com.music.model.exceptions.blockMusic.BlockMusicIsPresentException;
import com.music.repositories.BlockMusicRepository;
import com.music.repositories.UserMusicRepository;
import com.music.services.RepertoireService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockMusicServiceImplTest {

    @Mock
    private BlockMusicRepository blockMusicRepository;

    @Mock
    private BlockMusicMapper blockMusicMapper;

    @Mock
    private RepertoireService repertoireService;

    @Mock
    private UserMusicRepository userMusicRepository;

    @InjectMocks
    private BlockMusicServiceImpl blockMusicService;

    private BlockMusic blockMusic;
    private BlockMusicRequestDto requestDto;
    private BlockMusicResponseDto responseDto;
    private BlockMusicDetailResponse detailResponseDto;
    private UserMusic userMusic;
    private User user;
    private Repertoire repertoire;

    @BeforeEach
    void setup() {
        user = new User();
        user.setIdUser(1L);

        repertoire = new Repertoire();
        repertoire.setIdRepertoire(1L);
        repertoire.setUser(user);

        blockMusic = new BlockMusic();
        blockMusic.setIdBlockMusic(1L);
        blockMusic.setNameBlockMusic("Sertanejo");
        blockMusic.setRepertoire(repertoire);
        blockMusic.setItems(new ArrayList<>());

        requestDto = new BlockMusicRequestDto();
        requestDto.setNameBlockMusic("Sertanejo");
        requestDto.setIdRepertoire(1L);

        responseDto = new BlockMusicResponseDto();
        responseDto.setIdBlockMusic(1L);
        responseDto.setNameBlockMusic("Sertanejo");

        detailResponseDto = new BlockMusicDetailResponse();
        detailResponseDto.setIdBlockMusic(1L);
        detailResponseDto.setNameBlockMusic("Sertanejo");

        userMusic = new UserMusic();
        userMusic.setIdUserMusic(10L);
        userMusic.setUser(user);
        userMusic.setMusic(new Music());
    }

    @Test
    void createBlockMusic_Success() {
        when(blockMusicMapper.toBlockMusic(requestDto)).thenReturn(blockMusic);
        when(repertoireService.validateRepertoire(1L)).thenReturn(repertoire);
        when(blockMusicRepository.save(blockMusic)).thenReturn(blockMusic);
        when(blockMusicMapper.toBlockMusicResponseDto(blockMusic)).thenReturn(responseDto);

        BlockMusicResponseDto result = blockMusicService.createBlockMusic(requestDto);

        assertNotNull(result);
        assertEquals(responseDto.getNameBlockMusic(), result.getNameBlockMusic());
        verify(blockMusicRepository).save(blockMusic);
    }

    @Test
    void getBlockMusicById_Success() {
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(blockMusic));
        when(blockMusicMapper.toBlockMusicDetailResponse(blockMusic)).thenReturn(detailResponseDto);

        BlockMusicDetailResponse result = blockMusicService.getBlockMusicByIdBlockMusic(1L);

        assertNotNull(result);
    }

    @Test
    void getAllBlockMusic_Success() {
        when(blockMusicRepository.findAllBlockMusicByUserIdUser(1L)).thenReturn(List.of(blockMusic));
        when(blockMusicMapper.toListBlockMusicResponseDto(anyList())).thenReturn(List.of(responseDto));

        List<BlockMusicResponseDto> result = blockMusicService.getAllBlockMusic(1L);

        assertFalse(result.isEmpty());
    }

    @Test
    void getAllBlockMusicDetail_Success() {
        when(blockMusicRepository.findAllBlockMusicByUserIdUser(1L)).thenReturn(List.of(blockMusic));
        when(blockMusicMapper.toListBlockMusicDetailResponse(anyList())).thenReturn(List.of(detailResponseDto));

        List<BlockMusicDetailResponse> result = blockMusicService.getAllBlockMusicDetail(1L);

        assertFalse(result.isEmpty());
    }

    @Test
    void updateBlockMusic_Success() {
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(blockMusic));
        when(blockMusicRepository.save(blockMusic)).thenReturn(blockMusic);
        when(blockMusicMapper.toBlockMusicResponseDto(blockMusic)).thenReturn(responseDto);

        BlockMusicResponseDto result = blockMusicService.updateBlockMusic(1L, requestDto);

        assertNotNull(result);
    }

    @Test
    void deleteBlockMusic_Success() {
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(blockMusic));

        String result = blockMusicService.deleteBlockMusic(1L);

        assertEquals("Bloco com ID 1 excluído com sucesso!", result);
        verify(blockMusicRepository).delete(blockMusic);
    }

    @Test
    void linkMusicToBlock_Success() {
        MusicToBlockRequest linkRequest = new MusicToBlockRequest();
        linkRequest.setIdUserMusic(10L);
        linkRequest.setIdBlockMusic(1L);

        when(userMusicRepository.findById(10L)).thenReturn(Optional.of(userMusic));
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(blockMusic));
        when(blockMusicRepository.save(any(BlockMusic.class))).thenReturn(blockMusic);
        when(blockMusicMapper.toBlockMusicDetailResponse(any(BlockMusic.class))).thenReturn(detailResponseDto);

        BlockMusicDetailResponse result = blockMusicService.linkMusicToBLock(linkRequest);

        assertNotNull(result);
        assertNotNull(result);
        verify(blockMusicRepository).save(blockMusic);

        // Verifica se o item foi adicionado
        assertFalse(blockMusic.getItems().isEmpty());
        assertEquals(userMusic, blockMusic.getItems().get(0).getUserMusic());
    }

    @Test
    void linkMusicToBlock_UserMismatch_ThrowsException() {
        // Cenário: Usuário da música diferente do usuário do bloco
        User otherUser = new User();
        otherUser.setIdUser(99L);
        userMusic.setUser(otherUser);

        MusicToBlockRequest linkRequest = new MusicToBlockRequest();
        linkRequest.setIdUserMusic(10L);
        linkRequest.setIdBlockMusic(1L);

        when(userMusicRepository.findById(10L)).thenReturn(Optional.of(userMusic));
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(blockMusic));

        assertThrows(RuntimeException.class, () -> blockMusicService.linkMusicToBLock(linkRequest));
    }

    @Test
    void testExistingBlockMusic_ThrowsException() {
        when(blockMusicRepository.findBlockMusicByNameBlockMusicAndRepertoireIdRepertoire(anyString(), anyLong()))
                .thenReturn(Optional.of(blockMusic));

        assertThrows(BlockMusicIsPresentException.class, () -> blockMusicService.createBlockMusic(requestDto));
    }

    @Test
    void testGetAllBlockMusic_Empty_ThrowsException() {
        when(blockMusicRepository.findAllBlockMusicByUserIdUser(anyLong())).thenReturn(List.of());

        assertThrows(BlockMusicNotFoundException.class, () -> blockMusicService.getAllBlockMusic(1L));
    }

    @Test
    void testGetAllBlockMusicDetail_Empty_ThrowsException() {
        when(blockMusicRepository.findAllBlockMusicByUserIdUser(anyLong())).thenReturn(List.of());

        assertThrows(BlockMusicNotFoundException.class, () -> blockMusicService.getAllBlockMusicDetail(1L));
    }

    @Test
    void linkMusicToBlock_AlreadyExists_ReturnsSavedBlock() {
        // Setup existing item
        BlockMusicItem existingItem = new BlockMusicItem();
        existingItem.setUserMusic(userMusic);
        blockMusic.setItems(new ArrayList<>(List.of(existingItem)));

        MusicToBlockRequest linkRequest = new MusicToBlockRequest();
        linkRequest.setIdUserMusic(10L);
        linkRequest.setIdBlockMusic(1L);

        when(userMusicRepository.findById(10L)).thenReturn(Optional.of(userMusic));
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(blockMusic));
        when(blockMusicMapper.toBlockMusicDetailResponse(any(BlockMusic.class))).thenReturn(detailResponseDto);

        BlockMusicDetailResponse result = blockMusicService.linkMusicToBLock(linkRequest);

        assertNotNull(result);
        assertNotNull(result);
        verify(blockMusicRepository, never()).save(blockMusic);
    }
}
