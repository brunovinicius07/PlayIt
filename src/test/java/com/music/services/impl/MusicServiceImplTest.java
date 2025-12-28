package com.music.services.impl;

import com.music.model.dto.request.AddMusicRequest;
import com.music.model.dto.request.UpdateToneRequest;
import com.music.model.dto.response.UserMusicDetailResponse;
import com.music.model.dto.response.UserMusicResponse;
import com.music.model.entity.Music;
import com.music.model.entity.User;
import com.music.model.entity.UserMusic;
import com.music.model.mapper.MusicMapper;
import com.music.repositories.MusicRepository;
import com.music.repositories.UserMusicRepository;
import com.music.services.CipherScraperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicServiceImplTest {

    @Mock
    private MusicMapper musicMapper;

    @Mock
    private MusicRepository musicRepository;

    @Mock
    private UserMusicRepository userMusicRepository;

    @Mock
    private CipherScraperService cipherScraperService;

    @InjectMocks
    private MusicServiceImpl musicService;

    private User user;
    private Music music;
    private UserMusic userMusic;
    private AddMusicRequest addRequest;

    @BeforeEach
    void setup() {
        user = new User();
        user.setIdUser(1L);

        music = Music.builder()
                .idMusic(100L)
                .nameMusic("Oceano")
                .artist("Djavan")
                .slug("djavan/oceano")
                .originalTone("D")
                .cipherContent("<pre>Cifra original</pre>")
                .build();

        userMusic = UserMusic.builder()
                .idUserMusic(10L)
                .user(user)
                .music(music)
                .personalTone("D")
                .build();

        addRequest = new AddMusicRequest();
        addRequest.setUrl("https://www.cifraclub.com.br/djavan/oceano/");
    }

    @Test
    void shouldAddMusicFromCipherUrl_WhenMusicIsNew() {
        String slug = "djavan/oceano";

        when(musicRepository.findBySlug(slug)).thenReturn(Optional.empty());
        when(cipherScraperService.scrapeMusic(addRequest.getUrl(), slug)).thenReturn(music);
        when(musicRepository.save(music)).thenReturn(music);

        when(userMusicRepository.findByUserAndMusic(user, music)).thenReturn(Optional.empty());
        when(userMusicRepository.save(any(UserMusic.class))).thenReturn(userMusic);

        UserMusicDetailResponse responseDto = new UserMusicDetailResponse();
        responseDto.setMusic(new com.music.model.dto.response.MusicDetailResponse());
        when(musicMapper.toUserMusicDetailResponse(any(UserMusic.class))).thenReturn(responseDto);

        // Execução
        UserMusicDetailResponse result = musicService.addMusicFromCipherUrl(addRequest, user);

        // Validações
        assertNotNull(result);
        verify(cipherScraperService).scrapeMusic(addRequest.getUrl(), slug);
        verify(musicRepository).save(music);
        verify(userMusicRepository).save(any(UserMusic.class));
    }

    @Test
    void shouldAddMusicFromCipherUrl_WhenMusicExists_AndUserAlreadyHasIt() {
        String slug = "djavan/oceano";

        when(musicRepository.findBySlug(slug)).thenReturn(Optional.of(music));
        when(userMusicRepository.findByUserAndMusic(user, music)).thenReturn(Optional.of(userMusic));

        UserMusicDetailResponse responseDto = new UserMusicDetailResponse();
        responseDto.setMusic(new com.music.model.dto.response.MusicDetailResponse());
        when(musicMapper.toUserMusicDetailResponse(userMusic)).thenReturn(responseDto);

        UserMusicDetailResponse result = musicService.addMusicFromCipherUrl(addRequest, user);

        assertNotNull(result);
        verify(cipherScraperService, never()).scrapeMusic(anyString(), anyString());
        verify(userMusicRepository, never()).save(any(UserMusic.class));
    }

    @Test
    void shouldUpdatePersonalTone_Successfully() {
        UpdateToneRequest toneRequest = new UpdateToneRequest();
        toneRequest.setNewTone("G");

        when(userMusicRepository.findById(10L)).thenReturn(Optional.of(userMusic));

        when(userMusicRepository.save(any(UserMusic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserMusicDetailResponse responseDto = new UserMusicDetailResponse();
        responseDto.setMusic(new com.music.model.dto.response.MusicDetailResponse());
        when(musicMapper.toUserMusicDetailResponse(any(UserMusic.class))).thenReturn(responseDto);

        UserMusicDetailResponse result = musicService.updatePersonalTone(10L, toneRequest, user.getIdUser());

        assertNotNull(result);
        assertEquals("G", userMusic.getPersonalTone());
        verify(userMusicRepository).save(userMusic);
    }

    @Test
    void shouldThrowException_WhenUpdatingTone_IfMusicDoesNotBelongToUser() {
        UpdateToneRequest toneRequest = new UpdateToneRequest();
        toneRequest.setNewTone("G");

        User outroUser = new User();
        outroUser.setIdUser(99L);
        userMusic.setUser(outroUser);

        when(userMusicRepository.findById(10L)).thenReturn(Optional.of(userMusic));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            musicService.updatePersonalTone(10L, toneRequest, user.getIdUser());
        });

        assertEquals("Access denied: This music does not belong to your library.", exception.getMessage());
        verify(userMusicRepository, never()).save(any());
    }

    @Test
    void shouldGetAllUserMusics() {
        when(userMusicRepository.findAllByUser_IdUser(user.getIdUser())).thenReturn(List.of(userMusic));
        when(musicMapper.toUserMusicResponse(userMusic)).thenReturn(new UserMusicResponse());

        List<UserMusicResponse> result = musicService.getAllUserMusics(user.getIdUser());

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowException_WhenUrlIsInvalid() {
        AddMusicRequest invalidRequest = new AddMusicRequest();
        invalidRequest.setUrl("https://not-cifraclub.com/test");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            musicService.addMusicFromCipherUrl(invalidRequest, user);
        });

        assertEquals("Invalid URL. Please ensure it is a valid CifraClub URL.", thrown.getMessage());
    }

    @Test
    void shouldAddMusicFromCipherUrl_WhenMusicExists_ButNewToUser() {

        String slug = "djavan/oceano";

        when(musicRepository.findBySlug(slug)).thenReturn(Optional.of(music));
        when(userMusicRepository.findByUserAndMusic(user, music)).thenReturn(Optional.empty());

        when(userMusicRepository.save(any(UserMusic.class))).thenReturn(userMusic);

        UserMusicDetailResponse responseDto = new UserMusicDetailResponse();
        responseDto.setMusic(new com.music.model.dto.response.MusicDetailResponse());
        when(musicMapper.toUserMusicDetailResponse(any(UserMusic.class))).thenReturn(responseDto);

        UserMusicDetailResponse result = musicService.addMusicFromCipherUrl(addRequest, user);

        assertNotNull(result);
        verify(cipherScraperService, never()).scrapeMusic(anyString(), anyString());
        verify(userMusicRepository).save(any(UserMusic.class));
        verify(musicRepository, never()).save(music);
    }

    @Test
    void shouldThrowException_WhenUserMusicNotFound_InDetail() {
        when(userMusicRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            musicService.getUserMusicDetail(999L, user.getIdUser());
        });

        assertEquals("Music not found in library.", thrown.getMessage());
    }

    @Test
    void shouldThrowException_WhenUserMusicAccessDenied_InDetail() {
        User otherUser = new User();
        otherUser.setIdUser(888L);
        userMusic.setUser(otherUser);

        when(userMusicRepository.findById(10L)).thenReturn(Optional.of(userMusic));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            musicService.getUserMusicDetail(10L, user.getIdUser());
        });

        assertEquals("Access denied: This music does not belong to your library.", thrown.getMessage());
    }

    @Test
    void shouldGetUserMusicDetail_Success() {
        when(userMusicRepository.findById(10L)).thenReturn(Optional.of(userMusic));
        UserMusicDetailResponse responseDto = new UserMusicDetailResponse();
        responseDto.setMusic(new com.music.model.dto.response.MusicDetailResponse());
        when(musicMapper.toUserMusicDetailResponse(userMusic)).thenReturn(responseDto);

        UserMusicDetailResponse result = musicService.getUserMusicDetail(10L, user.getIdUser());

        assertNotNull(result);
    }
}