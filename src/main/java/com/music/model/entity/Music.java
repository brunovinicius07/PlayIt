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
@Table(name = "tb_music", indexes = @Index(name = "idx_music_slug", columnList = "slug", unique = true))
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMusic;

    @NotBlank
    private String nameMusic;

    @NotBlank
    private String artist;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(length = 5, nullable = false)
    private String originalTone;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String cipherContent;

    @OneToMany(mappedBy = "music", fetch = FetchType.LAZY)
    private List<UserMusic> userMusics = new ArrayList<>();
}