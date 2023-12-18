package de.habermehl.aventofcode.aoc2023.day15;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day15 {
    private static final Pattern COMMAND = Pattern.compile("^(\\w+)([-=])(\\d+)?$");
    private final String input;

    public Day15() throws IOException {
        this(Utils.getInput("aoc2023/day15/input"));
    }

    Day15(List<String> source) {
        input = source.stream().filter(Predicate.not(String::isEmpty)).collect(Collectors.joining());
    }

    public long getPart1() {
        return Stream.of(input.split(",")).mapToInt(Day15::getHash).sum();
    }

    public long getPart2() {
        final List<ArrayList<Integer>> boxes = getBoxes(input);
        int result = 0;
        for (int i = 0; i < boxes.size(); i++) {
            ArrayList<Integer> box = boxes.get(i);
            for (int j = 0; j < box.size(); j++) {
                result += (i + 1) * (j + 1) * box.get(j);
            }
        }
        return result;
    }

    private static List<ArrayList<Integer>> getBoxes(String input) {
        final List<LinkedHashMap<String, Integer>> boxes = IntStream.range(0, 256)
                .mapToObj(i -> new LinkedHashMap<String, Integer>())
                .toList();
        for (String s : input.split(",")) {
            Matcher matcher = COMMAND.matcher(s);
            if (!matcher.find()) {
                continue;
            }
            String label = matcher.group(1);
            String value = matcher.group(2);
            String focalLength = matcher.group(3);

            int boxNo = getHash(label);
            LinkedHashMap<String, Integer> box = boxes.get(boxNo);
            if (value.startsWith("-")) {
                box.remove(label);
            } else {
                box.put(label, Integer.valueOf(focalLength));
            }
        }
        return boxes.stream()
                .map(entries -> new ArrayList<>(entries.values()))
                .toList();
    }

    private static int getHash(String text) {
        int hash = 0;
        for (char c : text.toCharArray()) {
            hash += c;
            hash *= 17;
            hash %= 256;
        }
        return hash;
    }

    public static void main(String... args) throws IOException {
        Day15 day = new Day15();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }
}
