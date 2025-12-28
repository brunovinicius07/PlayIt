package com.music.services;

import com.music.model.entity.Music;

public interface CipherScraperService {
    Music findAndScrapeMusic(String artistInput, String musicInput);

    Music scrapeMusic(String url, String slug);
}
