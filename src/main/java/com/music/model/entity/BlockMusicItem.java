package com.music.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_block_music_item")
public class BlockMusicItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idBlockMusic", nullable = false)
    @JsonIgnore
    private BlockMusic blockMusic;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idUserMusic", nullable = false)
    private UserMusic userMusic;
}