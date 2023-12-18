package de.habermehl.aventofcode.aoc2023.day02;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day2 {
    private static final Pattern GAME_PATTERN = Pattern.compile("^Game (\\d+): (.*)$");
    private static final Pattern VALUES_PATTERN = Pattern.compile("(\\d+) (\\w+)");
    private final List<String> inputLines;

    public Day2() throws IOException {
        this(Utils.getInput("aoc2023/day02/input"));
    }

    Day2(List<String> inputLines) {
        this.inputLines = inputLines;
    }

    public int getPart1() {
        Map<String, Integer> maxValues = Map.of("red", 12, "green", 13, "blue", 14);
        int possibleSum = 0;
        for (String inputLine : inputLines) {
            Matcher matcher = GAME_PATTERN.matcher(inputLine);
            if (matcher.find()) {
                boolean possible = true;
                int game = Integer.parseInt(matcher.group(1));
                for (Entry<String, Integer> entry : colorAmounts(matcher.group(2)).entrySet()) {
                    if (maxValues.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                        possible = false;
                    }
                }
                if (possible) {
                    possibleSum += game;
                }
            }
        }
        return possibleSum;
    }

    public int getPart2() {
        int sum = 0;
        for (String inputLine : inputLines) {
            Matcher matcher = GAME_PATTERN.matcher(inputLine);
            if (matcher.find()) {
                int power = colorAmounts(matcher.group(2)).values().stream()
                        .mapToInt(Integer::valueOf)
                        .reduce(1, (a, b) -> a * b);
                sum += power;
            }
        }
        return sum;
    }

    private Map<String, Integer> colorAmounts(String line) {
        final Map<String, Integer> result = new HashMap<>();
        Matcher matcher = VALUES_PATTERN.matcher(line);
        while (matcher.find()) {
            int cubeAmount = Integer.parseInt(matcher.group(1));
            result.compute(matcher.group(2), (key, oldValue) -> oldValue == null || oldValue <= cubeAmount ? cubeAmount : oldValue);
        }
        return result;
    }

    public static void main(String... args) throws IOException {
        Day2 day = new Day2();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }
}
