package com.music.services;

import com.music.model.entity.Music;

public interface CipherScraperService {
    Music scrapeMusic(String url, String slug);
}
