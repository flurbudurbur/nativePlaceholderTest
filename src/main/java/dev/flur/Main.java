package dev.flur;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static List<List<String>> compileParts(String input) {
        List<List<String>> result = new ArrayList<>();
        StringBuilder currentSegment = new StringBuilder();
        boolean insideBrackets = false;

        for (char c : input.toCharArray()) {
            if (c == '[' || c == ']' || (c == '_' && !insideBrackets)) {
                if (!currentSegment.isEmpty()) {
                    result.add(c == ']' ? Arrays.asList(currentSegment.toString().split("/")) : List.of(currentSegment.toString()));
                    currentSegment.setLength(0);
                }
                insideBrackets = (c == '[');
            } else {
                currentSegment.append(c);
            }
        }

        if (!currentSegment.isEmpty()) {
            result.add(List.of(currentSegment.toString()));
        }

        return result;
    }

    public static List<String> compileVariants(List<List<String>> parsed, int i) {
        if (i >= parsed.size()) {
            return Collections.singletonList("");
        }

        List<String> result = new ArrayList<>();
        List<String> currentSegment = parsed.get(i);
        List<String> nextVariants = compileVariants(parsed, i + 1);

        for (String segment : currentSegment) {
            for (String variant : nextVariants) {
                result.add(segment + (variant.isEmpty() ? "" : "_" + variant));
            }
        }

        return result;
    }

    public static String getRegex(String identifier) {
        String regex = "\\[|]|/|<.*?>";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(identifier);

        return matcher.replaceAll(matchResult -> {
            String match = matchResult.group();
            return switch (match) {
                case "[" -> "(";
                case "]" -> ")";
                case "/" -> "|";
                case "<amount>", "<position>" -> "\\\\d+";
                default -> "";
            };
        });
    }

    public static String inverseRegex(String identifier, String trueIdentifier) {
        Pattern placeholderPattern = Pattern.compile("<(.*?)>");
        Matcher placeholderMatcher = placeholderPattern.matcher(trueIdentifier);

        while (placeholderMatcher.find()) {
            String placeholder = placeholderMatcher.group(1);
            identifier = identifier.replaceFirst("\\d+", "<" + placeholder + ">");
        }

        return identifier;
    }

    public static String[] getSelectedVariantParts(String identifier) {
        String trueIdentifier = "next_level_[cost/interest_rate/offline_interest_rate/capacity/required_items]_test_[foo/bar]_<amount>";
        String originalIdentifier = identifier;
        identifier = inverseRegex(identifier, trueIdentifier);
        List<List<String>> parts = compileParts(trueIdentifier), originalParts = compileParts(originalIdentifier);
        List<String> selectedParts = new ArrayList<>(), variants = compileVariants(parts, 0);

        List<String> reParts = getRecompiledParts(parts, originalParts);
        for (String part : reParts) {
            if (identifier.contains(part) &&
                    (identifier.startsWith(part + "_") || identifier.endsWith("_" + part) || identifier.contains("_" + part + "_"))) {
                selectedParts.add(part);
            }
        }

        String potentialIdentifier = String.join("_", selectedParts);

        System.out.println("sParts: " + selectedParts);
        System.out.println("oParts: " + originalParts);

        // verify if the placeholders exist
        if (variants.contains(potentialIdentifier)) {
            // replace the "<placeholders>" with the original parts
            for (int i = 0; i < selectedParts.size(); i++) {
                selectedParts.set(i, reParts.get(i));
            }
//            System.out.println(selectedParts);
            return selectedParts.toArray(new String[0]);
        }
        return new String[]{"Invalid identifier"};
    }

    public static List<String> getRecompiledParts(List<List<String>> parts, List<List<String>> oParts) {
        List<String> reParts = new ArrayList<>();
        List<List<String>> oPartsCopy = new ArrayList<>(oParts);
        for (List<String> part : parts) {
            if (part.size() > 1) {
                String current = "", first = oPartsCopy.get(0).toString().replaceAll("[\\[\\]/]", "");
                for (String p : part) {
                    if (p.startsWith(first) && !p.equals(current)) {
                        current = p;
                        if (current.equalsIgnoreCase(p)) {
                            reParts.add(p);
                            int l = p.split("_").length;
                            while (l > 0) {
                                oPartsCopy.remove(0);
                                l--;
                            }
                        }
                    }
                }
            } else {
                reParts.add(part.get(0));
                oPartsCopy.remove(0);
            }
        }
        return reParts;
    }

    public static void main(String[] args) {
        String identifier = "next_level_offline_interest_rate_test_bar_129873";
        String[] selectedVariantParts = getSelectedVariantParts(identifier);

        System.out.println("output: " + Arrays.toString(selectedVariantParts));
    }
}