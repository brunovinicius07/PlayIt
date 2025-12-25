package com.music.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserMusicResponse {
    private Long idUserMusic;
    private String personalTone;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime addedAt;

    private MusicSummaryResponse music;
}
