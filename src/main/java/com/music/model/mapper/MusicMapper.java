package com.music.model.mapper;

import com.music.model.dto.response.MusicDetailResponse;
import com.music.model.dto.response.MusicSummaryResponse;
import com.music.model.dto.response.UserMusicDetailResponse;
import com.music.model.dto.response.UserMusicResponse;
import com.music.model.entity.Music;
import com.music.model.entity.UserMusic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface MusicMapper {

    // Mapeamentos para Resumo (Listas)
    @Named("toSummary")
    MusicSummaryResponse toMusicSummaryResponse(Music music);

    @Mapping(target = "music", source = "music", qualifiedByName = "toSummary")
    UserMusicResponse toUserMusicResponse(UserMusic userMusic);

    // Mapeamentos para Detalhe (Tela de Ensaio/Visualização)
    @Named("toDetail")
    MusicDetailResponse toMusicDetailResponse(Music music);

    @Mapping(target = "music", source = "music", qualifiedByName = "toDetail")
    UserMusicDetailResponse toUserMusicDetailResponse(UserMusic userMusic);
}
