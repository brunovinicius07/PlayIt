package com.music.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockMusicDetailResponse {

    private Long idBlockMusic;

    private String nameBlockMusic;

    private Long idRepertoire;

    private List<UserMusicResponse> musics = new ArrayList<>();

    private Long idUser;
}
