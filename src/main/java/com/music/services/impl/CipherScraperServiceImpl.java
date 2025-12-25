package com.music.services.impl;

import com.music.model.entity.Music;
import com.music.services.CipherScraperService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CipherScraperServiceImpl implements CipherScraperService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public Music scrapeMusic(String url, String slug) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .referrer("https://www.google.com/")
                    .get();

            String title = doc.select("h1.t1").text();

            String artist = doc.select("h2.t3 a").text();
            if (artist.isEmpty()) artist = doc.select("h2.t3").text();

            Element toneElement = doc.select("#cifra_tom a").first();
            String originalTone = (toneElement != null) ? toneElement.text().trim() : "C";

            Element preElement = doc.select("pre").first();
            if (preElement == null) {
                throw new RuntimeException("Cipher content not found (might be tabs only or incompatible format).");
            }

            String cipherContent = preElement.html();

            return Music.builder()
                    .nameMusic(title)
                    .artist(artist)
                    .slug(slug)
                    .originalTone(originalTone)
                    .cipherContent(cipherContent)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Error connecting to Cipher URL: " + e.getMessage(), e);
        }
    }
}
