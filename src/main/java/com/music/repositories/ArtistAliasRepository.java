package com.music.repositories;

import com.music.model.entity.ArtistAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ArtistAliasRepository extends JpaRepository<ArtistAlias, Long> {
    Optional<ArtistAlias> findByAliasSlug(String aliasSlug);
}