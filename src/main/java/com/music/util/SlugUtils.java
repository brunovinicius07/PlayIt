package com.music.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import java.util.HashMap;
import java.util.Map;

public class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private static final Map<String, String> SPECIAL_CASES = new HashMap<>();

    static {
        SPECIAL_CASES.put("simone-simaria", "simone-simaria-as-coleguinhas");
        SPECIAL_CASES.put("simone-e-simaria", "simone-simaria-as-coleguinhas");
        SPECIAL_CASES.put("simone-&-simaria", "simone-simaria-as-coleguinhas");
    }

    private SlugUtils() {
    }

    public static String toSlug(String input) {
        if (input == null)
            return "";

        String nowhitespace = input.trim().toLowerCase();
        nowhitespace = nowhitespace.replaceAll("\\s+&\\s+", " e ");

        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        slug = NONLATIN.matcher(slug).replaceAll("");
        slug = slug.replaceAll("-+", "-");

        if (slug.startsWith("-"))
            slug = slug.substring(1);
        if (slug.endsWith("-"))
            slug = slug.substring(0, slug.length() - 1);

        return slug;
    }

    public static List<String> generateArtistCandidates(String artistName) {
        List<String> candidates = new ArrayList<>();

        String standardSlug = toSlug(artistName);
        candidates.add(standardSlug);

        String cleanSlug = standardSlug
                .replace("-e-", "-")
                .replace("-de-", "-")
                .replace("-da-", "-")
                .replace("-do-", "-");

        if (!cleanSlug.equals(standardSlug)) {
            candidates.add(cleanSlug);
        }

        if (cleanSlug.split("-").length == 2) {
            String withE = cleanSlug.replace("-", "-e-");
            if (!candidates.contains(withE)) {
                candidates.add(withE);
            }
        }

        if (SPECIAL_CASES.containsKey(standardSlug)) {
            String special = SPECIAL_CASES.get(standardSlug);
            if (!candidates.contains(special))
                candidates.add(special);
        }
        if (SPECIAL_CASES.containsKey(cleanSlug)) {
            String special = SPECIAL_CASES.get(cleanSlug);
            if (!candidates.contains(special))
                candidates.add(special);
        }

        return candidates;
    }
}