package com.music.model.mapper;

import com.music.model.dto.request.BlockMusicRequestDto;
import com.music.model.dto.response.BlockMusicResponseDto;
import com.music.model.entity.BlockMusic;
import com.music.model.entity.BlockMusicItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BlockMusicMapper {

    @Mapping(source = "idUser", target = "user.idUser")
    @Mapping(source = "idRepertoire", target = "repertoire.idRepertoire")
    @Mapping(source = "nameBlockMusic", target = "nameBlockMusic")
    BlockMusic toBlockMusic(BlockMusicRequestDto blockMusicRequestDto);

    @Mapping(source = "idBlockMusic", target = "idBlockMusic")
    @Mapping(source = "nameBlockMusic", target = "nameBlockMusic")
    @Mapping(source = "repertoire.idRepertoire", target = "idRepertoire")
    @Mapping(source = "user.idUser", target = "idUser")
    @Mapping(target = "idMusics", expression = "java(mapItemsToMusicIds(blockMusic.getItems()))")
    BlockMusicResponseDto toBlockMusicResponseDto(BlockMusic blockMusic);

    List<BlockMusicResponseDto> toListBlockMusicResponseDto(List<BlockMusic> blockMusicList);

    default List<Long> mapItemsToMusicIds(List<BlockMusicItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }

        return items.stream()
                .map(item -> item.getUserMusic().getMusic().getIdMusic())
                .collect(Collectors.toList());
    }
}
