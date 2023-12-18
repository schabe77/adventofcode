package de.habermehl.aventofcode.aoc2023.day13;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day13 {

    private final List<List<String>> maps;

    public Day13() throws IOException {
        this(Utils.getInput("aoc2023/day13/input"));
    }

    Day13(List<String> inputLines) {
        maps = Stream.of(String.join("\n", inputLines).split("\n{2,}"))
                .map(map -> List.of(map.split("\\n")))
                .toList();
    }

    public long getPart1() {
        return maps.stream()
                .mapToLong(map -> getPerfectReflectionRow(map, 0) * 100 + getPerfectReflectionRow(swap(map), 0))
                .sum();
    }

    public long getPart2() {
        return maps.stream()
                .mapToLong(map -> getPerfectReflectionRow(map, 1) * 100 + getPerfectReflectionRow(swap(map), 1))
                .sum();
    }

    private long getPerfectReflectionRow(List<String> mapLines, int expectedDiffs) {
        for (int i = 1; i < mapLines.size(); i++) {
            int expectedRows = Math.min(i, mapLines.size() - i);
            String previousRows = String.join("", mapLines.subList(i - expectedRows, i));
            String followingRows = String.join("", Lists.reverse(mapLines.subList(i, i + expectedRows)));
            if (diff(previousRows, followingRows) == expectedDiffs) {
                return i;
            }
        }
        return 0;
    }

    private static long diff(String a, String b) {
        return IntStream.range(0, a.length()).map(i -> a.charAt(i) == b.charAt(i) ? 0 : 1).sum();
    }

    public List<String> swap(List<String> source) {
        char[][] sbs = new char[source.get(0).length()][source.size()];
        for (int y = 0; y < source.size(); y++) {
            String line = source.get(y);
            for (int x = 0; x < line.length(); x++) {
                sbs[x][y] = line.charAt(x);
            }
        }
        return Stream.of(sbs).map(String::new).toList();
    }

    public static void main(String... args) throws IOException {
        Day13 day = new Day13();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }
}
