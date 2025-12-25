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
import com.music.services.MusicService;
import com.music.util.ToneTransposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MusicServiceImpl implements MusicService {

    private final MusicMapper musicMapper;
    private final MusicRepository musicRepository;
    private final UserMusicRepository userMusicRepository;
    private final CipherScraperService cipherScraperService;

    @Override
    @Transactional
    public UserMusicDetailResponse addMusicFromCipherUrl(AddMusicRequest request, User user) {
        String url = request.getUrl();
        String slug = extractSlugFromUrl(url);

        Optional<Music> existingMusic = musicRepository.findBySlug(slug);

        Music music;
        if (existingMusic.isPresent()) {
            music = existingMusic.get();
        } else {
            music = cipherScraperService.scrapeMusic(url, slug);
            music = musicRepository.save(music);
        }

        Optional<UserMusic> existingUserMusic = userMusicRepository.findByUserAndMusic(user, music);

        if (existingUserMusic.isPresent()) {
            return buildTransposedResponse(existingUserMusic.get());
        }

        UserMusic newUserMusic = UserMusic.builder()
                .user(user)
                .music(music)
                .personalTone(music.getOriginalTone())
                .build();

        UserMusic savedUserMusic = userMusicRepository.save(newUserMusic);

        return buildTransposedResponse(savedUserMusic);
    }

    @Override
    @Transactional(readOnly = true)
    public UserMusicDetailResponse getUserMusicDetail(Long idUserMusic, Long userId) {
        UserMusic userMusic = validateUserMusicOwnership(idUserMusic, userId);
        return buildTransposedResponse(userMusic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserMusicResponse> getAllUserMusics(Long userId) {
        List<UserMusic> library = userMusicRepository.findAllByUser_IdUser(userId);
        return library.stream()
                .map(musicMapper::toUserMusicResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserMusicDetailResponse updatePersonalTone(Long idUserMusic, UpdateToneRequest request, Long userId) {
        UserMusic userMusic = validateUserMusicOwnership(idUserMusic, userId);
        
        userMusic.setPersonalTone(request.getNewTone());
        UserMusic updatedUserMusic = userMusicRepository.save(userMusic);
        
        return buildTransposedResponse(updatedUserMusic);
    }

    private UserMusic validateUserMusicOwnership(Long idUserMusic, Long userId) {
        UserMusic userMusic = userMusicRepository.findById(idUserMusic)
                .orElseThrow(() -> new RuntimeException("Music not found in library."));

        if (!userMusic.getUser().getIdUser().equals(userId)) {
            throw new RuntimeException("Access denied: This music does not belong to your library.");
        }
        return userMusic;
    }

    private UserMusicDetailResponse buildTransposedResponse(UserMusic userMusic) {
        UserMusicDetailResponse response = musicMapper.toUserMusicDetailResponse(userMusic);

        String originalTone = userMusic.getMusic().getOriginalTone();
        String personalTone = userMusic.getPersonalTone();
        String originalCipher = userMusic.getMusic().getCipherContent();

        String transposedCipher = ToneTransposer.transpose(originalCipher, originalTone, personalTone);

        response.getMusic().setCipherContent(transposedCipher);

        return response;
    }

    private String extractSlugFromUrl(String url) {
        Pattern pattern = Pattern.compile("cifraclub\\.com\\.br/([^/]+/[^/]+)/?");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid URL. Please ensure it is a valid CifraClub URL.");
    }
}
