package de.habermehl.aventofcode.aoc2023.day09;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.ToLongBiFunction;
import java.util.stream.Stream;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day9 {

    private final List<List<Long>> inputLines;

    public Day9() throws IOException {
        this(Utils.getInput("aoc2023/day09/input"));
    }

    Day9(List<String> inputLines) {
        this.inputLines = inputLines.stream()
                .map(line -> Stream.of(line.split("\\s")).map(Long::valueOf).toList())
                .toList();
    }

    public long getPart1() {
        return inputLines.stream()
                .mapToLong(value -> getNextValue(value, (previousValue, list) -> previousValue + list.get(list.size() - 1)))
                .sum();
    }

    public long getPart2() {
        return inputLines.stream()
                .mapToLong(value -> getNextValue(value, (previousValue, list) -> list.get(0) - previousValue))
                .sum();
    }

    private long getNextValue(List<Long> source, ToLongBiFunction<Long, List<Long>> itemFunction) {
        LinkedList<List<Long>> pyramid = new LinkedList<>();
        pyramid.add(source);
        while (pyramid.getLast().stream().anyMatch(n -> n != 0L)) {
            pyramid.add(getDiffs(pyramid.getLast()));
        }
        long nextValue = 0;
        Iterator<List<Long>> listIterator = pyramid.descendingIterator();
        do {
            List<Long> diffList = listIterator.next();
            nextValue = itemFunction.applyAsLong(nextValue, diffList);
        } while (listIterator.hasNext());
        return nextValue;
    }

    private List<Long> getDiffs(List<Long> source) {
        List<Long> result = new ArrayList<>(source.size() - 1);
        for (int i = 1; i < source.size(); i++) {
            result.add(source.get(i) - source.get(i - 1));
        }
        return result;
    }

    public static void main(String... args) throws IOException {
        Day9 day = new Day9();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }
}
