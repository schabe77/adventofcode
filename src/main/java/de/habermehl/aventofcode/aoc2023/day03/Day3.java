package de.habermehl.aventofcode.aoc2023.day03;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day3 {
    private final List<String> inputLines;

    public Day3() throws IOException {
        this(Utils.getInput("aoc2023/day03/input"));
    }

    Day3(List<String> inputLines) {
        this.inputLines = inputLines;
    }

    public int getPart1() {
        Set<Point> symbolPoints = getSymbolPoints(inputLines).keySet();
        int sum = 0;
        for (Position numberPosition : getNumberPositions(inputLines)) {
            if (numberPosition.getSurroundingPoints().stream()
                    .anyMatch(symbolPoints::contains)) {
                sum += numberPosition.number();
            }
        }
        return sum;
    }

    public int getPart2() {
        Set<Point> gearPoints = getSymbolPoints(inputLines).entrySet().stream()
                .filter(entry -> "*".equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Multimap<Point, Integer> gearNumbers = ArrayListMultimap.create();
        for (Position numberPosition : getNumberPositions(inputLines)) {
            for (Point surroundingPoint : numberPosition.getSurroundingPoints()) {
                if (gearPoints.contains(surroundingPoint)) {
                    gearNumbers.put(surroundingPoint, numberPosition.number());
                }
            }
        }
        int sum = 0;
        for (Collection<Integer> value : gearNumbers.asMap().values()) {
            if (value.size() > 1) {
                sum += value.stream().mapToInt(Integer::valueOf).reduce(1, (a, b) -> a * b);
            }
        }
        return sum;
    }

    private List<Position> getNumberPositions(List<String> inputLines) {
        final List<Position> result = new ArrayList<>();
        for (int y = 0; y < inputLines.size(); y++) {
            final String line = inputLines.get(y);
            int x = 0;
            while (x < line.length()) {
                if (Character.isDigit(line.charAt(x))) {
                    int start = x;
                    do {
                        x++;
                    } while (x < line.length() && Character.isDigit(line.charAt(x)));
                    result.add(new Position(new Point(start, y), new Point(x - 1, y), Integer.parseInt(line.substring(start, x))));
                }
                x++;
            }
        }
        return result;
    }

    private Map<Point, String> getSymbolPoints(List<String> inputLines) {
        Map<Point, String> result = new HashMap<>();
        for (int y = 0; y < inputLines.size(); y++) {
            char[] line = inputLines.get(y).toCharArray();
            for (int x = 0; x < line.length; x++) {
                char c = line[x];
                if (c != '.' && !Character.isDigit(c)) {
                    result.put(new Point(x, y), Character.toString(c));
                }
            }
        }
        return result;
    }

    public static void main(String... args) throws IOException {
        Day3 day = new Day3();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    private record Position(Point from, Point to, int number) {
        public List<Point> getSurroundingPoints() {
            List<Point> result = new ArrayList<>();
            for (int x = from.x() - 1; x <= to.x() + 1; x++) {
                for (int y = from.y() - 1; y <= to.y() + 1; y++) {
                    result.add(new Point(x, y));
                }
            }
            return result;
        }
    }

    private record Point(int x, int y) {
    }
}
