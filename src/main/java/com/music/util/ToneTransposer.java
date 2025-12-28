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
            case "Cb" -> "B";
            case "E#" -> "F";
            case "B#" -> "C";
            case "Fb" -> "E";
            default -> note;
        };
    }

    public static String transpose(String cipherContent, String fromTone, String toTone) {
        if (cipherContent == null || fromTone == null || toTone == null) return cipherContent;
        
        String rootFrom = normalizeNote(fromTone);
        String rootTo = normalizeNote(toTone);
        
        if (rootFrom.equals(rootTo)) return cipherContent;

        int indexFrom = SCALE.indexOf(rootFrom);
        int indexTo = SCALE.indexOf(rootTo);

        if (indexFrom == -1 || indexTo == -1) return cipherContent; 

        int semitones = indexTo - indexFrom;
        
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
        StringBuffer sb = new StringBuffer();
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
