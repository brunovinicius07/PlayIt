package com.music.services.impl;

import com.music.model.dto.request.BlockMusicRequestDto;
import com.music.model.dto.request.MusicToBlockRequest;
import com.music.model.dto.response.BlockMusicResponseDto;
import com.music.model.dto.response.MusicResponseDto;
import com.music.model.entity.*;
import com.music.model.exceptions.blockMusic.BlockMusicIsPresentException;
import com.music.model.exceptions.blockMusic.BlockMusicNotFoundException;
import com.music.model.exceptions.music.MusicNotFoundException;
import com.music.model.mapper.BlockMusicMapper;
import com.music.model.mapper.MusicMapper;
import com.music.repositories.BlockMusicRepository;
import com.music.repositories.MusicRepository;
import com.music.repositories.UserMusicRepository;
import com.music.services.RepertoireService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlockMusicServiceImplTest {

    @Mock
    private BlockMusicRepository blockMusicRepository;

    @Mock
    private BlockMusicMapper blockMusicMapper;

    @Mock
    private RepertoireService repertoireService;

    @Mock
    private MusicRepository musicRepository;

    @Mock
    private MusicMapper musicMapper;

    @Mock
    private UserMusicRepository userMusicRepository;

    @InjectMocks
    private BlockMusicServiceImpl blockMusicService;

    private BlockMusicRequestDto blockMusicRequestDto;
    private BlockMusic blockMusic;
    private Music music;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        blockMusicService = Mockito.spy(blockMusicService);

        blockMusicRequestDto = new BlockMusicRequestDto("Bloco Sertanejo", 1L, 1L);

        blockMusic = BlockMusic.builder()
                .idBlockMusic(1L)
                .nameBlockMusic("Bloco Sertanejo")
                .items(new ArrayList<>())
                .build();

        music = Music.builder()
                .idMusic(1L)
                .nameMusic("Música 1")
                .artist("Artista 1")
                .build();
    }

    @Test
    void shouldCreateBlockMusicSuccessfully() {
        when(blockMusicRepository.findBlockMusicByNameBlockMusicAndRepertoireIdRepertoire(
                blockMusicRequestDto.getNameBlockMusic(), blockMusicRequestDto.getIdRepertoire()))
                .thenReturn(Optional.empty());

        when(blockMusicMapper.toBlockMusic(blockMusicRequestDto)).thenReturn(blockMusic);
        when(repertoireService.validateRepertoire(blockMusicRequestDto.getIdRepertoire())).thenReturn(null);
        when(blockMusicRepository.save(blockMusic)).thenReturn(blockMusic);
        when(blockMusicMapper.toBlockMusicResponseDto(blockMusic)).thenReturn(new BlockMusicResponseDto());

        BlockMusicResponseDto response = blockMusicService.createBlockMusic(blockMusicRequestDto);

        assertNotNull(response);
        verify(blockMusicRepository).save(blockMusic);
    }

    @Test
    void shouldThrowBlockMusicIsPresentException_WhenBlockAlreadyExists() {
        when(blockMusicRepository.findBlockMusicByNameBlockMusicAndRepertoireIdRepertoire(
                anyString(), anyLong())).thenReturn(Optional.of(blockMusic));

        assertThrows(BlockMusicIsPresentException.class,
                () -> blockMusicService.createBlockMusic(blockMusicRequestDto));
    }

    @Test
    void shouldGetBlockMusicByIdSuccessfully() {
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(blockMusic));
        when(blockMusicMapper.toBlockMusicResponseDto(blockMusic)).thenReturn(new BlockMusicResponseDto());

        BlockMusicResponseDto response = blockMusicService.getBlockMusicByIdBlockMusic(1L);

        assertNotNull(response);
        verify(blockMusicRepository).findById(1L);
    }

    @Test
    void shouldThrowBlockMusicNotFound_WhenBlockDoesNotExist() {
        when(blockMusicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BlockMusicNotFoundException.class,
                () -> blockMusicService.getBlockMusicByIdBlockMusic(99L));
    }

    @Test
    void shouldReturnBlockMusicsByIdsSuccessfully() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        BlockMusic block1 = BlockMusic.builder().idBlockMusic(1L).build();
        BlockMusic block3 = BlockMusic.builder().idBlockMusic(3L).build();

        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(block1));
        when(blockMusicRepository.findById(2L)).thenReturn(Optional.empty());
        when(blockMusicRepository.findById(3L)).thenReturn(Optional.of(block3));

        List<BlockMusic> result = blockMusicService.getBlockMusicsByIdBlockMusics(ids);

        assertEquals(2, result.size());
        assertTrue(result.contains(block1));
        assertTrue(result.contains(block3));
        assertFalse(result.stream().anyMatch(b -> b.getIdBlockMusic().equals(2L)));

        verify(blockMusicRepository, times(3)).findById(anyLong());
    }

    @Test
    void shouldReturnAllBlockMusicSuccessfully() {
        List<BlockMusic> blockMusicList = Arrays.asList(
                BlockMusic.builder().idBlockMusic(1L).build(),
                BlockMusic.builder().idBlockMusic(2L).build());

        when(blockMusicRepository.findAllBlockMusicByUserIdUser(1L)).thenReturn(blockMusicList);
        when(blockMusicMapper.toListBlockMusicResponseDto(blockMusicList))
                .thenReturn(Arrays.asList(new BlockMusicResponseDto(), new BlockMusicResponseDto()));

        List<BlockMusicResponseDto> result = blockMusicService.getAllBlockMusic(1L);

        assertEquals(2, result.size());
        verify(blockMusicRepository).findAllBlockMusicByUserIdUser(1L);
        verify(blockMusicMapper).toListBlockMusicResponseDto(blockMusicList);
    }

    @Test
    void shouldThrowBlockMusicNotFound_WhenNoBlockExists() {
        when(blockMusicRepository.findAllBlockMusicByUserIdUser(99L)).thenReturn(Collections.emptyList());

        assertThrows(BlockMusicNotFoundException.class, () -> blockMusicService.getAllBlockMusic(99L));
        verify(blockMusicRepository).findAllBlockMusicByUserIdUser(99L);
    }

    @Test
    void shouldUpdateBlockMusicSuccessfully() {
        BlockMusicRequestDto updateDto = new BlockMusicRequestDto("Bloco MPB", 1L, 1L);

        when(blockMusicRepository.findBlockMusicByNameBlockMusicAndRepertoireIdRepertoire(
                updateDto.getNameBlockMusic(), updateDto.getIdRepertoire())).thenReturn(Optional.empty());
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(blockMusic));
        when(blockMusicRepository.save(blockMusic)).thenReturn(blockMusic);
        when(blockMusicMapper.toBlockMusicResponseDto(blockMusic)).thenReturn(new BlockMusicResponseDto());

        BlockMusicResponseDto response = blockMusicService.updateBlockMusic(1L, updateDto);

        assertNotNull(response);
        assertEquals("Bloco MPB", blockMusic.getNameBlockMusic());
        verify(blockMusicRepository).save(blockMusic);
    }

    @Test
    void shouldUpdateBlockMusic_WhenNameIsNotNull() {
        BlockMusicRequestDto requestDto = new BlockMusicRequestDto("NovoNome", 1L, 1L);
        BlockMusic existingBlock = BlockMusic.builder()
                .idBlockMusic(1L)
                .nameBlockMusic("Nome Original")
                .build();

        when(blockMusicRepository.findBlockMusicByNameBlockMusicAndRepertoireIdRepertoire(
                anyString(), anyLong())).thenReturn(Optional.empty());
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(existingBlock));
        when(blockMusicRepository.save(existingBlock)).thenReturn(existingBlock);
        when(blockMusicMapper.toBlockMusicResponseDto(existingBlock))
                .thenReturn(new BlockMusicResponseDto(1L, "NovoNome",
                        1L, List.of(), 1L));

        BlockMusicResponseDto response = blockMusicService.updateBlockMusic(1L, requestDto);

        assertNotNull(response);
        assertEquals("NovoNome", existingBlock.getNameBlockMusic());
        verify(blockMusicRepository).save(existingBlock);
    }

    @Test
    void shouldUpdateBlockMusic_WhenNameIsNull() {
        BlockMusicRequestDto requestDto = new BlockMusicRequestDto(null, 1L, 1L);
        BlockMusic existingBlock = BlockMusic.builder()
                .idBlockMusic(1L)
                .nameBlockMusic("Nome Original")
                .build();

        when(blockMusicRepository.findBlockMusicByNameBlockMusicAndRepertoireIdRepertoire(
                any(), anyLong())).thenReturn(Optional.empty());
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(existingBlock));
        when(blockMusicRepository.save(existingBlock)).thenReturn(existingBlock);
        when(blockMusicMapper.toBlockMusicResponseDto(existingBlock))
                .thenReturn(new BlockMusicResponseDto(1L, "Nome Original", 1L,
                        List.of(), 1L));

        BlockMusicResponseDto response = blockMusicService.updateBlockMusic(1L, requestDto);

        assertNotNull(response);
        assertEquals("Nome Original", existingBlock.getNameBlockMusic());
        verify(blockMusicRepository).save(existingBlock);
    }

    @Test
    void shouldDeleteBlockMusicSuccessfully() {
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(blockMusic));

        String response = blockMusicService.deleteBlockMusic(1L);

        assertEquals("Bloco com ID 1 excluído com sucesso!", response);
        verify(blockMusicRepository).delete(blockMusic);
    }

    @Test
    void shouldLinkMusicToBlockSuccessfully() {
        MusicToBlockRequest request = new MusicToBlockRequest(List.of(1L), 1L);

        // Configurar mocks para garantir que o usuário e userMusic sejam encontrados
        User user = new User();
        Repertoire repertoire = new Repertoire();
        repertoire.setUser(user);
        blockMusic.setRepertoire(repertoire);
        blockMusic.setItems(new ArrayList<>()); // Inicializar lista de items

        UserMusic userMusic = UserMusic.builder()
                .user(user)
                .music(music)
                .build();

        when(musicRepository.findById(1L)).thenReturn(Optional.of(music));
        when(blockMusicRepository.findById(1L)).thenReturn(Optional.of(blockMusic));
        when(userMusicRepository.findByUserAndMusic(user, music)).thenReturn(Optional.of(userMusic));
        when(blockMusicRepository.save(blockMusic)).thenReturn(blockMusic);

        MusicResponseDto response = blockMusicService.linkMusicToBLock(request);

        assertNotNull(response);
        assertEquals(music.getIdMusic(), response.getIdMusic());

        // Verificar se foi adicionado
        assertFalse(blockMusic.getItems().isEmpty());
        assertEquals(userMusic, blockMusic.getItems().get(0).getUserMusic());
    }

    @Test
    void shouldNotAddMusicAgainIfAlreadyPresentInBlock() {
        Long musicId = 1L;
        Long blockId = 10L;

        MusicToBlockRequest request = new MusicToBlockRequest(List.of(blockId), musicId);

        User user = new User();
        Repertoire repertoire = new Repertoire();
        repertoire.setUser(user);

        BlockMusic blockMusic = new BlockMusic();
        blockMusic.setIdBlockMusic(blockId);
        blockMusic.setRepertoire(repertoire);
        blockMusic.setItems(new ArrayList<>());

        UserMusic userMusic = UserMusic.builder().user(user).music(music).build();

        // Adiciona um item existente
        BlockMusicItem existingItem = BlockMusicItem.builder()
                .blockMusic(blockMusic)
                .userMusic(userMusic)
                .build();
        blockMusic.getItems().add(existingItem);

        // Mocking findAllBlockMusicByUserIdUser not needed here as we mock
        // getBlockMusicsByIdBlockMusics manually or use default implementation?
        // Wait, the service uses getBlockMusicsByIdBlockMusics which does findById.

        when(musicRepository.findById(musicId)).thenReturn(Optional.of(music));
        when(blockMusicRepository.findById(blockId)).thenReturn(Optional.of(blockMusic));
        when(userMusicRepository.findByUserAndMusic(user, music)).thenReturn(Optional.of(userMusic));

        MusicResponseDto result = blockMusicService.linkMusicToBLock(request);

        assertNotNull(result);
        assertEquals(1, blockMusic.getItems().size());
        verify(blockMusicRepository, never()).save(blockMusic); // Não deve salvar se já existe
    }

    @Test
    void shouldThrowMusicNotFound_WhenLinkingNonexistentMusic() {
        MusicToBlockRequest request = new MusicToBlockRequest(Collections.emptyList(), 99L);
        when(musicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MusicNotFoundException.class, () -> blockMusicService.linkMusicToBLock(request));
    }
}
