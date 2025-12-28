package com.music.model.dto.response;

import lombok.Data;

@Data
public class MusicResponse {
    private Long idMusic;
    private String nameMusic;
    private String artist;
    private String slug;
    private String originalTone;
    private String cipherContent;
}
