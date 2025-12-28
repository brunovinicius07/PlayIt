package com.music.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlugUtilsTest {

    @Test
    void shouldNormalizeArtistName_WithAmpersand() {
        String input = "Jorge & Mateus";
        String expected = "jorge-e-mateus";
        String result = SlugUtils.toSlug(input);
        assertEquals(expected, result);
    }

    @Test
    void shouldNormalizeArtistName_WithSeparatorAnd() {
        String input = "Rick e Renner";
        String expected = "rick-e-renner"; // toSlug mantem o 'e' se estiver normal
        String result = SlugUtils.toSlug(input);
        assertEquals(expected, result);
    }

    @Test
    void testGenerateArtistCandidates_BrunoMarrone() {
        String input = "Bruno Marrone";
        List<String> candidates = SlugUtils.generateArtistCandidates(input);

        // Deve gera tanto o "bruno-marrone" (padrão) quanto "bruno-e-marrone" (com e)
        assertTrue(candidates.contains("bruno-marrone"));
        assertTrue(candidates.contains("bruno-e-marrone"));
    }

    @Test
    void testGenerateArtistCandidates_BrunoAndMarrone() {
        String input = "Bruno & Marrone";
        List<String> candidates = SlugUtils.generateArtistCandidates(input);

        // Deve gerar "bruno-e-marrone" (padrão do &) e "bruno-marrone" (sem e)
        assertTrue(candidates.contains("bruno-e-marrone"));
        assertTrue(candidates.contains("bruno-marrone"));
    }

    @Test
    void shouldGenerateCandidates_WhenStandardDiffersFromClean() {
        // "Jorge e Mateus" -> toSlug = "jorge-e-mateus"
        // cleanSlug removes "-e-" -> "jorge-mateus"
        String input = "Jorge e Mateus";

        List<String> candidates = SlugUtils.generateArtistCandidates(input);

        assertEquals(2, candidates.size());
        assertTrue(candidates.contains("jorge-e-mateus"));
        assertTrue(candidates.contains("jorge-mateus"));
    }

    @Test
    void shouldGenerateCandidates_WhenSepatorIsDe() {
        // "Matheus de Kauan" (exemplo hipotético)
        String input = "Matheus de Kauan";

        List<String> candidates = SlugUtils.generateArtistCandidates(input);

        // standard: matheus-de-kauan
        // clean: matheus-kauan
        // extra: matheus-e-kauan (pela nova lógica de inserir 'e')
        assertEquals(3, candidates.size());
        assertTrue(candidates.contains("matheus-de-kauan"));
        assertTrue(candidates.contains("matheus-kauan"));
        assertTrue(candidates.contains("matheus-e-kauan"));
    }

    @Test
    void testGenerateArtistCandidates_SimoneAndSimaria() {
        // Teste para o caso especial (alias conhecido)
        String input = "Simone & Simaria"; // gera simone-e-simaria -> mapeado
        List<String> candidates = SlugUtils.generateArtistCandidates(input);
        assertTrue(candidates.contains("simone-simaria-as-coleguinhas"));

        String input2 = "Simone Simaria"; // gera simone-simaria -> mapeado
        List<String> candidates2 = SlugUtils.generateArtistCandidates(input2);
        assertTrue(candidates2.contains("simone-simaria-as-coleguinhas"));
    }

    @Test
    void shouldGenerateOnlyOneCandidate_WhenNoSeparatorsToCheck() {
        String input = "Metallica";

        List<String> candidates = SlugUtils.generateArtistCandidates(input);

        assertEquals(1, candidates.size());
        assertTrue(candidates.contains("metallica"));
    }
}
