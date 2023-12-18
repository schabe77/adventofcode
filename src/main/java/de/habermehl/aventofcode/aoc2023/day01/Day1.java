package de.habermehl.aventofcode.aoc2023.day01;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day1 {
    private static final Map<String, Integer> ONLY_DIGITS = IntStream.rangeClosed(1, 9)
            .boxed()
            .collect(Collectors.toMap(String::valueOf, Function.identity()));
    private static final Map<String, Integer> ONLY_WORDS = Map.ofEntries(
            Map.entry("one", 1),
            Map.entry("two", 2),
            Map.entry("three", 3),
            Map.entry("four", 4),
            Map.entry("five", 5),
            Map.entry("six", 6),
            Map.entry("seven", 7),
            Map.entry("eight", 8),
            Map.entry("nine", 9));

    private final List<String> inputLines;

    public Day1() throws IOException {
        this(Utils.getInput("aoc2023/day01/input"));
    }

    Day1(List<String> inputLines) {
        this.inputLines = inputLines;
    }

    public int getPart1() {
        return getResult(ONLY_DIGITS);
    }

    public int getPart2() {
        Map<String, Integer> mappings = new HashMap<>(ONLY_DIGITS);
        mappings.putAll(ONLY_WORDS);
        return getResult(mappings);
    }

    private int getResult(Map<String, Integer> mappings) {
        Pattern startPattern = Pattern.compile("(" + String.join("|", mappings.keySet()) + ")");
        Pattern endPattern = Pattern.compile(".*(" + String.join("|", mappings.keySet()) + ")");
        int sum = 0;
        for (String inputLine : inputLines) {
            Matcher startMatcher = startPattern.matcher(inputLine);
            Matcher endMatcher = endPattern.matcher(inputLine);
            if (startMatcher.find() && endMatcher.find()) {
                sum += mappings.get(startMatcher.group(1)) * 10 + mappings.get(endMatcher.group(1));
            }
        }
        return sum;
    }

    public static void main(String... args) throws IOException {
        Day1 day = new Day1();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }
}
