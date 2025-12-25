package com.music.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateToneRequest {

    @NotBlank(message = "The tone cannot be empty.")
    // Regex opcional para validar tons (ex: C, C#, Db, etc)
    @Pattern(regexp = "^[A-G](#|b)?$", message = "Invalid tone format. Use C, C#, Db, etc.")
    private String newTone;
}
