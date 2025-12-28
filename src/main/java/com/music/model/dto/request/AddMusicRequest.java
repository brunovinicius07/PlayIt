package com.music.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddMusicRequest {

    @NotBlank(message = "A URL não pode estar em branco.")
    @Pattern(regexp = "^(https?://)?(www\\.)?cifraclub\\.com\\.br/.+/.+", message = "URL inválida.")
    private String url;
}
