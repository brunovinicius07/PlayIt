package com.music.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_user_music",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idUser", "idMusic"}))
public class UserMusic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUserMusic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.EAGER) // Traz os dados da música (Nome/Artista) automaticamente
    @JoinColumn(name = "idMusic", nullable = false)
    private Music music;

    // O CAMPO DE OURO: O último tom que o usuário escolheu.
    // Atualize este campo sempre que ele mudar o tom no front.
    @Column(length = 5)
    private String personalTone;

    private LocalDateTime addedAt;

    // Se deletar da biblioteca, remove dos repertórios também (Cascade)
    @OneToMany(mappedBy = "userMusic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlockMusicItem> linkedInBlocks = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.addedAt = LocalDateTime.now();
        // Se não escolheu tom, usa o original como padrão inicial
        if (this.personalTone == null && this.music != null) {
            this.personalTone = this.music.getOriginalTone();
        }
    }
}