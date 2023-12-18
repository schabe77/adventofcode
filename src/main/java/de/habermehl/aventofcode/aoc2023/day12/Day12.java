package de.habermehl.aventofcode.aoc2023.day12;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day12 {
    private final List<Map.Entry<String, List<Integer>>> input;

    public Day12() throws IOException {
        this(Utils.getInput("aoc2023/day12/input"));
    }

    Day12(List<String> inputLines) {
        input = getInputMap(inputLines);
    }

    public long getPart1() {
        return input.stream()
                .mapToLong(entry -> getValidOptionsCached(entry.getKey(), entry.getValue(), new HashMap<>()))
                .sum();
    }

    public long getPart2() {
        Map<String, Long> cache = new HashMap<>();
        return input.stream()
                .map(entry -> addFactor(entry, 5))
                .mapToLong(entry -> getValidOptionsCached(entry.getKey(), entry.getValue(), cache))
                .sum();
    }

    private long getValidOptionsCached(String input, List<Integer> targetAmounts, Map<String, Long> cache) {
        String key = input + ":" + targetAmounts;
        Long l = cache.get(key);
        if (l == null) {
            l = getValidOptions3(input, targetAmounts, cache);
            cache.put(key, l);
        }
        return l;
    }

    private long getValidOptions3(String input, List<Integer> targetAmounts, Map<String, Long> cache) {
        if (input.isEmpty()) {
            return targetAmounts.isEmpty() ? 1L : 0L;
        }
        char current = input.charAt(0);
        if (current == '#') {
            if (targetAmounts.isEmpty()) {
                return 0;
            }
            final int brokenSprings = targetAmounts.get(0);
            if (input.length() < brokenSprings || input.chars().limit(brokenSprings).anyMatch(i -> i == '.')) {
                // input not long enough to contain expected amount
                // or there are intact spring within the broken spring amount
                return 0;
            }
            String nextInput = input.substring(brokenSprings);
            if (nextInput.startsWith("#")) {
                // additionally to the already contained broken springs there is one more than the group allows
                return 0;
            }
            if (nextInput.startsWith("?")) {
                // # as next character is not allowed -> skip one more character - can only be a dot
                return getValidOptionsCached(noLeadingDots(nextInput.substring(1)), targetAmounts.subList(1, targetAmounts.size()), cache);
            }
            // can only be a dot - remove all leading dots
            return getValidOptionsCached(noLeadingDots(nextInput), targetAmounts.subList(1, targetAmounts.size()), cache);
        }
        String remainingText = input.substring(1);
        if (current == '.') {
            return getValidOptionsCached(noLeadingDots(remainingText), targetAmounts, cache);
        } else if (current == '?') {
            return getValidOptionsCached("#" + remainingText, targetAmounts, cache) + getValidOptionsCached("." + remainingText, targetAmounts, cache);
        }
        return 0;
    }

    private static List<Map.Entry<String, List<Integer>>> getInputMap(List<String> inputLines) {
        List<Map.Entry<String, List<Integer>>> result = new ArrayList<>();
        for (String inputLine : inputLines) {
            String[] parts = inputLine.split("\\s+");
            if (parts.length == 2) {
                result.add(Map.entry(parts[0], Stream.of(parts[1].split(",")).map(Integer::valueOf).toList()));
            }
        }
        return result;
    }

    public static Map.Entry<String, List<Integer>> addFactor(Map.Entry<String, List<Integer>> source, int factor) {
        StringBuilder resultString = new StringBuilder();
        List<Integer> resultAmounts = new ArrayList<>();
        for (int i = 0; i < factor; i++) {
            resultString.append(source.getKey()).append('?');
            resultAmounts.addAll(source.getValue());
        }
        return Map.entry(resultString.substring(0, resultString.length() - 1), resultAmounts);
    }

    private static String noLeadingDots(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != '.') {
                return text.substring(i);
            }
        }
        return "";
    }

    public static void main(String... args) throws IOException {
        Day12 day = new Day12();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }
}
