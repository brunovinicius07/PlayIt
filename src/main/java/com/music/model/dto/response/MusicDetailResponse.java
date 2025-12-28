package com.music.model.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MusicDetailResponse extends MusicSummaryResponse {
    private String cipherContent;
}
