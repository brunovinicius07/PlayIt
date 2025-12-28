package com.music.model.mapper;

import com.music.model.dto.request.BlockMusicRequestDto;
import com.music.model.dto.response.BlockMusicDetailResponse;
import com.music.model.dto.response.BlockMusicResponseDto;
import com.music.model.dto.response.UserMusicResponse;
import com.music.model.entity.BlockMusic;
import com.music.model.entity.BlockMusicItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {MusicMapper.class})
public abstract class BlockMusicMapper {

    @Autowired
    protected MusicMapper musicMapper;

    @Mapping(source = "idUser", target = "user.idUser")
    @Mapping(source = "idRepertoire", target = "repertoire.idRepertoire")
    @Mapping(source = "nameBlockMusic", target = "nameBlockMusic")
    public abstract BlockMusic toBlockMusic(BlockMusicRequestDto blockMusicRequestDto);

    @Mapping(source = "idBlockMusic", target = "idBlockMusic")
    @Mapping(source = "nameBlockMusic", target = "nameBlockMusic")
    @Mapping(source = "repertoire.idRepertoire", target = "idRepertoire")
    @Mapping(source = "user.idUser", target = "idUser")
    @Mapping(target = "idMusics", expression = "java(mapItemsToMusicIds(blockMusic.getItems()))")
    public abstract BlockMusicResponseDto toBlockMusicResponseDto(BlockMusic blockMusic);

    public abstract List<BlockMusicResponseDto> toListBlockMusicResponseDto(List<BlockMusic> blockMusicList);

    // Novos Mapeamentos para Detalhes
    @Mapping(source = "idBlockMusic", target = "idBlockMusic")
    @Mapping(source = "nameBlockMusic", target = "nameBlockMusic")
    @Mapping(source = "repertoire.idRepertoire", target = "idRepertoire")
    @Mapping(source = "user.idUser", target = "idUser")
    @Mapping(target = "musics", expression = "java(mapItemsToUserMusicResponse(blockMusic.getItems()))")
    public abstract BlockMusicDetailResponse toBlockMusicDetailResponse(BlockMusic blockMusic);

    public abstract List<BlockMusicDetailResponse> toListBlockMusicDetailResponse(List<BlockMusic> blockMusicList);

    protected List<Long> mapItemsToMusicIds(List<BlockMusicItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(item -> item.getUserMusic().getMusic().getIdMusic())
                .collect(Collectors.toList());
    }

    protected List<UserMusicResponse> mapItemsToUserMusicResponse(List<BlockMusicItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(item -> musicMapper.toUserMusicResponse(item.getUserMusic()))
                .collect(Collectors.toList());
    }
}
