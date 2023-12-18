package de.habermehl.aventofcode.aoc2023.day11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.LongStream;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day11 {

    private final List<Position> galaxyPositions;

    public Day11() throws IOException {
        this(Utils.getInput("aoc2023/day11/input"));
    }

    Day11(List<String> inputLines) {
        galaxyPositions = getGalaxyPositions(inputLines);
    }

    public long getPart1() {
        return getGalaxyDistanceSum(addSpaces(galaxyPositions, 2));
    }

    public long getPart2() {
        return getGalaxyDistanceSum(addSpaces(galaxyPositions, 1_000_000));
    }

    private static long getGalaxyDistanceSum(List<Position> galaxyPositions) {
        long sum = 0;
        for (int i = 0; i < galaxyPositions.size(); i++) {
            Position galaxy = galaxyPositions.get(i);
            for (int j = i + 1; j < galaxyPositions.size(); j++) {
                sum += galaxy.getDistance(galaxyPositions.get(j));
            }
        }
        return sum;
    }

    private static List<Position> getGalaxyPositions(List<String> inputLines) {
        List<Position> result = new ArrayList<>();
        for (int y = 0; y < inputLines.size(); y++) {
            final String line = inputLines.get(y);
            for (int x = 0; x < line.length(); x++) {
                if (line.charAt(x) == '#') {
                    result.add(new Position(x, y));
                }
            }
        }
        return result;
    }

    private static List<Position> addSpaces(List<Position> galaxyPositions, int replaceEmptyLinesAmount) {
        final TreeSet<Long> usedXs = new TreeSet<>();
        final TreeSet<Long> usedYs = new TreeSet<>();
        for (Position position : galaxyPositions) {
            usedXs.add(position.x());
            usedYs.add(position.y());
        }
        long[] unusedXs = LongStream.range(usedXs.first(), usedXs.last()).filter(x -> !usedXs.contains(x)).sorted().toArray();
        long[] unusedYs = LongStream.range(usedYs.first(), usedYs.last()).filter(y -> !usedYs.contains(y)).sorted().toArray();

        final List<Position> result = new ArrayList<>(galaxyPositions.size());
        for (Position position : galaxyPositions) {
            Position adjustedPosition = position;
            for (int j = unusedXs.length - 1; j >= 0; j--) {
                if (adjustedPosition.x() > unusedXs[j]) {
                    adjustedPosition = new Position(adjustedPosition.x + replaceEmptyLinesAmount - 1, adjustedPosition.y());
                }
            }
            for (int j = unusedYs.length - 1; j >= 0; j--) {
                if (adjustedPosition.y() > unusedYs[j]) {
                    adjustedPosition = new Position(adjustedPosition.x, adjustedPosition.y + replaceEmptyLinesAmount - 1);
                }
            }
            result.add(adjustedPosition);
        }
        return result;
    }

    public static void main(String... args) throws IOException {
        Day11 day = new Day11();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    private record Position(long x, long y) {
        long getDistance(Position other) {
            return Math.abs(x - other.x) + Math.abs(y - other.y);
        }
    }
}
