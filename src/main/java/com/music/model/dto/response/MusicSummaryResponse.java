package com.music.model.dto.response;

import lombok.Data;

@Data
public class MusicSummaryResponse {
    private Long idMusic;
    private String nameMusic;
    private String artist;
    private String slug;
    private String originalTone;
}
