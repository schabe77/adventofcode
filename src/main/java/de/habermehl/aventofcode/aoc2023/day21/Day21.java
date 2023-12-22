package de.habermehl.aventofcode.aoc2023.day21;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.habermehl.aventofcode.aoc2023.Direction;
import de.habermehl.aventofcode.aoc2023.Position;
import de.habermehl.aventofcode.aoc2023.Utils;

public class Day21 {
    private final Set<Position> stones;
    private final Position startPosition;
    private final int width;
    private final int height;

    public Day21() throws IOException {
        this(Utils.getInput("aoc2023/day21/input"));
    }

    Day21(List<String> inputLines) {
        Entry<Position, Set<Position>> input = getGrid(inputLines);
        stones = input.getValue();
        startPosition = input.getKey();
        width = inputLines.get(0).length();
        height = inputLines.size();
    }

    public long getPart1() {
        List<Position> positions = List.of(startPosition);
        for (int i = 0; i < 6; i++) {
            positions = positions.stream()
                    .map(Day21::getSurroundingPoints)
                    .flatMap(List::stream)
                    .distinct()
                    .filter(this::isEmptyField)
                    .toList();
        }
        return positions.size();
    }

    /**
     * we leave the first grid after 65 steps. then we have a constant growth over
     * 131 (grid width) * 202300 steps: 131 * 202300 + 65 = 26501365 -> the step amount
     * this growth can be calculated with a polynomial, e.g. a*((n*(n-1))/2) + bn + c
     * where target n is 202300
     */
    public long getPart2() {
        long target = 26501365;
        long n = Math.floorDiv(target, width); // should be 202300 in our case
        List<Integer> factors = new ArrayList<>();
        List<Position> positions = List.of(startPosition);
        int i = 0;
        while (true) {
            i++;
            positions = positions.stream()
                    .map(Day21::getSurroundingPoints)
                    .flatMap(List::stream)
                    .distinct()
                    .filter(this::isEmptyField)
                    .toList();

            if (i % width == target % width) {
                factors.add(positions.size());
                if (factors.size() == 3) {
                    // target function is f(n) = a*(n*(n-1)/2) + b*n + c
                    // we have points n=0, n=1, n=2 and need f(n), for n=202300

                    // a*(0*(0-1)/2) + b*0 + c = factors.get(0) --> a*0 + b*0 + c = factors.get(0) --> c = factors.get(0)
                    long c = factors.get(0);
                    // a*(1*(1-1)/2) + b*1 + c = factors.get(1) --> b + c = factors.get(1)         --> b = factors.get(1) - c
                    long b = factors.get(1) - c;
                    // a*(2*(2-1)/2) + b*2 + c = factors.get(3) --> a + b*2 + c = factors.get(3)   --> a = factors.get(3) - b*2 - c
                    long a = factors.get(2) - 2 * b - c;

                    return a * ((n * (n - 1)) / 2) + b * n + c;
                }
            }
        }
    }

    private boolean isEmptyField(Position p) {
        final long adjustedX = p.x() % width;
        final long adjustedY = p.y() % height;
        return !stones.contains(new Position(adjustedX < 0 ? adjustedX + width : adjustedX, adjustedY < 0 ? adjustedY + height : adjustedY));
    }

    public static List<Position> getSurroundingPoints(Position position) {
        return List.of(position.moveTo(Direction.NORTH), position.moveTo(Direction.EAST), position.moveTo(Direction.SOUTH), position.moveTo(Direction.WEST));
    }

    private static Map.Entry<Position, Set<Position>> getGrid(List<String> inputLines) {
        Set<Position> result = new HashSet<>();
        Position startPosition = new Position(0L, 0L);
        for (int y = 0; y < inputLines.size(); y++) {
            final String line = inputLines.get(y);
            for (int x = 0; x < line.length(); x++) {
                char c = line.charAt(x);
                if (c == '#') {
                    result.add(new Position(x, y));
                } else if (c == 'S') {
                    startPosition = new Position(x, y);
                }
            }
        }
        return Map.entry(startPosition, result);
    }

    public static void main(String... args) throws IOException {
        Day21 day = new Day21();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }
}
