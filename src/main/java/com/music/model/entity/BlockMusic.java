package com.music.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_block_musical")
public class BlockMusic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBlockMusic;

    @Size(max = 25)
    @NotBlank
    @JsonIgnore
    private String nameBlockMusic;

    @ManyToOne
    @JoinColumn(name = "idRepertoire")
    private Repertoire repertoire;

    @OneToMany(mappedBy = "blockMusic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlockMusicItem> items = new ArrayList<>();

    public void addItem(BlockMusicItem item) {
        items.add(item);
        item.setBlockMusic(this);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", nullable = false)
    private User user;
}
