package com.music.services.impl;

import com.music.model.entity.ArtistAlias;
import com.music.model.entity.Music;
import com.music.model.exceptions.music.MusicNotFoundException;
import com.music.repositories.ArtistAliasRepository;
import com.music.services.CipherScraperService;
import com.music.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CipherScraperServiceImpl implements CipherScraperService {

    private final ArtistAliasRepository aliasRepository;

    private static final String BASE_URL = "https://www.cifraclub.com.br/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public Music findAndScrapeMusic(String artistInput, String musicInput) {
        String musicSlug = SlugUtils.toSlug(musicInput);
        String initialArtistSlug = SlugUtils.toSlug(artistInput);

        Optional<ArtistAlias> knownAlias = aliasRepository.findByAliasSlug(initialArtistSlug);

        if (knownAlias.isPresent()) {
            String correctArtistSlug = knownAlias.get().getCorrectSlug();
            String url = BASE_URL + correctArtistSlug + "/" + musicSlug + "/";
            String fullSlug = correctArtistSlug + "/" + musicSlug;
            return scrapeMusic(url, fullSlug);
        }

        List<String> candidates = SlugUtils.generateArtistCandidates(artistInput);
        RuntimeException lastError = null;

        for (String artistCandidate : candidates) {
            String url = BASE_URL + artistCandidate + "/" + musicSlug + "/";
            String candidateSlug = artistCandidate + "/" + musicSlug;

            try {
                Music music = scrapeMusic(url, candidateSlug);

                if (!artistCandidate.equals(initialArtistSlug)) {
                    saveNewAlias(initialArtistSlug, artistCandidate);
                }
                return music;

            } catch (RuntimeException e) {
                lastError = e;
            }
        }

        throw new MusicNotFoundException();
    }

    @Override
    public Music scrapeMusic(String url, String slug) {
        try {
            Document doc = getDocument(url);

            String title = doc.select("h1.t1").text();

            String artist = doc.select("h2.t3 a").text();
            if (artist.isEmpty())
                artist = doc.select("h2.t3").text();

            Element toneElement = doc.select("#cifra_tom a").first();
            String originalTone = (toneElement != null) ? toneElement.text().trim() : "C";

            Element preElement = doc.select("pre").first();
            if (preElement == null) {
                throw new RuntimeException("Conteúdo da cifra não encontrado.");
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
            throw new RuntimeException("Erro ao conectar na URL: " + url, e);
        }
    }

    protected Document getDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .referrer("https://www.google.com/")
                .get();
    }

    private void saveNewAlias(String wrongSlug, String rightSlug) {
        try {
            aliasRepository.save(ArtistAlias.builder()
                    .aliasSlug(wrongSlug)
                    .correctSlug(rightSlug)
                    .build());
        } catch (Exception e) {
            System.err.println("Aviso: Erro ao salvar alias: " + e.getMessage());
        }
    }
}