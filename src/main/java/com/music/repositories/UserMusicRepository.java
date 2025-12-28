package com.music.repositories;

import com.music.model.entity.User;
import com.music.model.entity.Music;
import com.music.model.entity.UserMusic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMusicRepository extends JpaRepository<UserMusic, Long> {
    Optional<UserMusic> findByUserAndMusic(User user, Music music);
    List<UserMusic> findAllByUser_IdUser(Long idUser);
}
