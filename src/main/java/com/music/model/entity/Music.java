package com.music.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_music",
        indexes = @Index(name = "idx_music_slug", columnList = "slug", unique = true))
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMusic;

    @NotBlank
    private String nameMusic;

    @NotBlank
    private String artist;

    // Identificador único (ex: "julliany-souza/quem-e-esse")
    @Column(unique = true, nullable = false)
    private String slug;

    // Tom Original que veio do site (ex: "G") - Referência imutável
    @Column(length = 5, nullable = false)
    private String originalTone;

    // A Cifra completa (HTML/Texto) raspada do site
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String cipherContent;

    // Relacionamento reverso (apenas para mapeamento JPA)
    @OneToMany(mappedBy = "music", fetch = FetchType.LAZY)
    private List<UserMusic> userMusics = new ArrayList<>();
}