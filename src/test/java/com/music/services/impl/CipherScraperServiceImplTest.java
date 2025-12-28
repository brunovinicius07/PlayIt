package com.music.services.impl;

import com.music.model.entity.Music;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.music.model.exceptions.music.MusicNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CipherScraperServiceImplTest {

    private CipherScraperServiceImpl cipherScraperService;

    private Document document;

    @Mock
    private com.music.repositories.ArtistAliasRepository aliasRepository;

    @BeforeEach
    void setUp() {
        cipherScraperService = spy(new CipherScraperServiceImpl(aliasRepository));
        document = mock(Document.class);
    }

    @Test
    void testScrapeMusic_Success() throws IOException {
        String url = "https://www.cifraclub.com.br/artist/song/";
        String slug = "artist/song";

        // Mock Document structure
        Element titleElement = mock(Element.class);
        when(titleElement.text()).thenReturn("Song Title");

        Element artistElement = mock(Element.class);
        when(artistElement.text()).thenReturn("Artist Name");

        Element toneElement = mock(Element.class);
        when(toneElement.text()).thenReturn("G");

        Element preElement = mock(Element.class);
        when(preElement.html()).thenReturn("C G D");

        Elements titleElements = new Elements(titleElement);
        Elements artistElements = new Elements(artistElement);
        Elements toneElements = new Elements(toneElement);
        Elements preElements = new Elements(preElement);

        // Mock Jsoup selectors
        when(document.select("h1.t1")).thenReturn(titleElements);
        when(document.select("h2.t3 a")).thenReturn(artistElements);
        when(document.select("#cifra_tom a")).thenReturn(toneElements);
        when(document.select("pre")).thenReturn(preElements);

        // Mock getDocument to return our mocked document
        doReturn(document).when(cipherScraperService).getDocument(url);

        Music result = cipherScraperService.scrapeMusic(url, slug);

        assertNotNull(result);
        assertEquals("Song Title", result.getNameMusic());
        assertEquals("Artist Name", result.getArtist());
        assertEquals("G", result.getOriginalTone());
        assertEquals(slug, result.getSlug());
        assertEquals("C G D", result.getCipherContent());
    }

    @Test
    void testScrapeMusic_ArtistFallback() throws IOException {
        String url = "https://www.cifraclub.com.br/artist/song/";
        String slug = "artist/song";

        // Mock Document structure
        Element titleElement = mock(Element.class);
        when(titleElement.text()).thenReturn("Song Title");

        // Primary artist selector empty
        when(document.select("h2.t3 a")).thenReturn(new Elements());

        // Fallback artist selector
        Element fallbackArtistElement = mock(Element.class);
        when(fallbackArtistElement.text()).thenReturn("Fallback Artist");
        when(document.select("h2.t3")).thenReturn(new Elements(fallbackArtistElement));

        Element toneElement = mock(Element.class);
        when(toneElement.text()).thenReturn("G");

        Element preElement = mock(Element.class);
        when(preElement.html()).thenReturn("C G D");

        when(document.select("h1.t1")).thenReturn(new Elements(titleElement));
        // Selector for tone and pre also need to be mocked
        when(document.select("#cifra_tom a")).thenReturn(new Elements(toneElement));
        when(document.select("pre")).thenReturn(new Elements(preElement));

        doReturn(document).when(cipherScraperService).getDocument(url);

        Music result = cipherScraperService.scrapeMusic(url, slug);

        assertEquals("Fallback Artist", result.getArtist());
    }

    @Test
    void testScrapeMusic_DefaultTone() throws IOException {
        String url = "https://www.cifraclub.com.br/artist/song/";
        String slug = "artist/song";

        Element titleElement = mock(Element.class);
        when(titleElement.text()).thenReturn("Song Title");
        when(document.select("h1.t1")).thenReturn(new Elements(titleElement));

        Element artistElement = mock(Element.class);
        when(artistElement.text()).thenReturn("Artist");
        when(document.select("h2.t3 a")).thenReturn(new Elements(artistElement));

        // Tone is null/empty
        when(document.select("#cifra_tom a")).thenReturn(new Elements());

        Element preElement = mock(Element.class);
        when(preElement.html()).thenReturn("C G D");
        when(document.select("pre")).thenReturn(new Elements(preElement));

        doReturn(document).when(cipherScraperService).getDocument(url);

        Music result = cipherScraperService.scrapeMusic(url, slug);

        assertEquals("C", result.getOriginalTone());
    }

    @Test
    void testScrapeMusic_NoCipherContent_ThrowsException() throws IOException {
        String url = "https://www.cifraclub.com.br/artist/song/";
        String slug = "artist/song";

        Element titleEl = mock(Element.class);
        when(titleEl.text()).thenReturn("Title");
        when(document.select("h1.t1")).thenReturn(new Elements(titleEl));

        Element artistEl = mock(Element.class);
        when(artistEl.text()).thenReturn("Artist");
        when(document.select("h2.t3 a")).thenReturn(new Elements(artistEl));

        Element toneEl = mock(Element.class);
        when(toneEl.text()).thenReturn("C");
        when(document.select("#cifra_tom a")).thenReturn(new Elements(toneEl));

        // Pre not found
        when(document.select("pre")).thenReturn(new Elements());

        doReturn(document).when(cipherScraperService).getDocument(url);

        assertThrows(RuntimeException.class, () -> {
            cipherScraperService.scrapeMusic(url, slug);
        });
    }

    @Test
    void testScrapeMusic_ConnectionError_ThrowsException() throws IOException {
        String url = "https://www.cifraclub.com.br/artist/song/";
        String slug = "artist/song";

        doThrow(new IOException("Connection timed out")).when(cipherScraperService).getDocument(url);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            cipherScraperService.scrapeMusic(url, slug);
        });

        assertTrue(thrown.getMessage().contains("Erro ao conectar na URL"));
    }

    @Test
    void testFindAndScrapeMusic_ThrowsMusicNotFoundException() throws IOException {
        String artistInput = "NonExistentArtist";
        String musicInput = "NonExistentSong";

        doThrow(new RuntimeException("Page not found")).when(cipherScraperService).getDocument(anyString());

        assertThrows(MusicNotFoundException.class, () -> {
            cipherScraperService.findAndScrapeMusic(artistInput, musicInput);
        });
    }
}
