package com.music.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToneTransposer {

    private static final List<String> SCALE = Arrays.asList(
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    );

    private static String normalizeNote(String note) {
        return switch (note) {
            case "Db" -> "C#";
            case "Eb" -> "D#";
            case "Gb" -> "F#";
            case "Ab" -> "G#";
            case "Bb" -> "A#";
            case "Cb" -> "B"; // Exceção teórica
            case "E#" -> "F"; // Exceção teórica
            case "B#" -> "C"; // Exceção teórica
            case "Fb" -> "E"; // Exceção teórica
            default -> note;
        };
    }

    public static String transpose(String cipherContent, String fromTone, String toTone) {
        if (cipherContent == null || fromTone == null || toTone == null) return cipherContent;
        
        // Normaliza os tons de origem e destino para garantir que estão na escala
        String rootFrom = normalizeNote(fromTone);
        String rootTo = normalizeNote(toTone);
        
        if (rootFrom.equals(rootTo)) return cipherContent;

        int indexFrom = SCALE.indexOf(rootFrom);
        int indexTo = SCALE.indexOf(rootTo);

        if (indexFrom == -1 || indexTo == -1) return cipherContent; 

        int semitones = indexTo - indexFrom;

        // Regex aprimorado:
        // Procura por notas (A-G) seguidas opcionalmente de # ou b.
        // O lookahead (?=...) e lookbehind (?<=...) podem ser usados se quisermos garantir que está dentro de tags <b>
        // Mas como o CifraClub usa <b>C</b>, o regex simples funciona se aplicarmos sobre o texto todo, 
        // desde que tenhamos cuidado para não substituir texto comum.
        // O padrão do CifraClub geralmente isola os acordes em tags <b>.
        // Vamos tentar capturar o conteúdo dentro de <b>...</b> para ser mais seguro.
        
        // Estratégia: Encontrar tags <b> e processar apenas o conteúdo delas.
        // Regex para encontrar tags <b>: <b>(.*?)</b>
        
        StringBuffer result = new StringBuffer();
        Pattern tagPattern = Pattern.compile("<b>(.*?)</b>");
        Matcher tagMatcher = tagPattern.matcher(cipherContent);

        while (tagMatcher.find()) {
            String chordContent = tagMatcher.group(1);
            String transposedChord = transposeChordString(chordContent, semitones);
            tagMatcher.appendReplacement(result, "<b>" + transposedChord + "</b>");
        }
        tagMatcher.appendTail(result);

        return result.toString();
    }

    private static String transposeChordString(String chord, int semitones) {
        // Dentro da tag <b> pode ter algo como "C#m7" ou "G/B"
        // Vamos substituir cada nota encontrada.
        StringBuffer sb = new StringBuffer();
        // Regex para nota: [A-G] seguido opcionalmente de # ou b
        Pattern notePattern = Pattern.compile("[A-G](#|b)?");
        Matcher noteMatcher = notePattern.matcher(chord);

        while (noteMatcher.find()) {
            String originalNote = noteMatcher.group();
            String normalized = normalizeNote(originalNote);
            int oldIndex = SCALE.indexOf(normalized);

            if (oldIndex != -1) {
                int newIndex = (oldIndex + semitones) % 12;
                if (newIndex < 0) newIndex += 12;
                String newNote = SCALE.get(newIndex);
                noteMatcher.appendReplacement(sb, newNote);
            }
        }
        noteMatcher.appendTail(sb);
        return sb.toString();
    }
}
