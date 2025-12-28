package com.music.services;

import com.music.model.dto.request.AddMusicRequest;
import com.music.model.dto.request.UpdateToneRequest;
import com.music.model.dto.response.UserMusicDetailResponse;
import com.music.model.dto.response.UserMusicResponse;
import com.music.model.entity.User;

import java.util.List;

public interface MusicService {

    UserMusicDetailResponse addMusicFromCipherUrl(AddMusicRequest request, User user);

    UserMusicDetailResponse getUserMusicDetail(Long idUserMusic, Long userId);

    List<UserMusicResponse> getAllUserMusics(Long userId);

    UserMusicDetailResponse updatePersonalTone(Long idUserMusic, UpdateToneRequest request, Long userId);
}
