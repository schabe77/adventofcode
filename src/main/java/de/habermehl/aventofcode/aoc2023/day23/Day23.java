package de.habermehl.aventofcode.aoc2023.day23;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.habermehl.aventofcode.aoc2023.Direction;
import de.habermehl.aventofcode.aoc2023.Position;
import de.habermehl.aventofcode.aoc2023.Utils;

public class Day23 {
    private static final Map<Character, Direction> INPUT_DIRECTIONS = Map.of(
            '^', Direction.NORTH,
            '>', Direction.EAST,
            'v', Direction.SOUTH,
            '<', Direction.WEST);
    private static final List<Direction> ALLOWED_DIRECTIONS = new ArrayList<>(INPUT_DIRECTIONS.values());
    private final Set<Position> forest;
    private final Map<Position, Direction> slopes;
    private final Position start;
    private final Position finish;

    public Day23() throws IOException {
        this(Utils.getInput("aoc2023/day23/input"));
    }

    Day23(List<String> inputLines) {
        Entry<Set<Position>, Map<Position, Direction>> input = getHikingMap(inputLines);
        forest = input.getKey();
        slopes = input.getValue();
        start = new Position(1L, 0);
        finish = new Position(inputLines.get(0).length() - 2L, inputLines.size() - 1L);
    }

    public long getPart1() {
        return getLongestHike(false);
    }

    public long getPart2() {
        return getLongestHike(true);
    }

    private long getLongestHike(boolean ignoreSlopes) {
        Map<Position, List<PathSection>> junctions = getCrossroads(ignoreSlopes);
        return getLongestHikeRecursively(new ArrayList<>(List.of(junctions.get(start).iterator().next())), junctions);
    }

    private long getLongestHikeRecursively(ArrayList<PathSection> currentPath, Map<Position, List<PathSection>> junctions) {
        PathSection lastStep = currentPath.get(currentPath.size() - 1);
        long longestHike = Long.MIN_VALUE;
        for (PathSection value : junctions.get(lastStep.target())) {
            Position nextPosition = value.target();
            if (nextPosition.equals(finish)) {
                return currentPath.stream().mapToLong(PathSection::length).sum() + value.length();
            }
            if (currentPath.stream().noneMatch(pathSection -> pathSection.position().equals(nextPosition))) {
                currentPath.add(value);
                longestHike = Math.max(longestHike, getLongestHikeRecursively(currentPath, junctions));
                currentPath.remove(currentPath.size() - 1);
            }
        }
        return longestHike;
    }

    private Map<Position, List<PathSection>> getCrossroads(boolean ignoreSlope) {
        LongSummaryStatistics xStats = forest.stream().mapToLong(Position::x).summaryStatistics();
        LongSummaryStatistics yStats = forest.stream().mapToLong(Position::y).summaryStatistics();
        Position min = new Position(xStats.getMin(), yStats.getMin());
        Position max = new Position(xStats.getMax(), yStats.getMax());

        Multimap<Position, Direction> crossroads = getCrossroads(min, max);
        Map<Position, List<PathSection>> result = new HashMap<>();
        for (Entry<Position, Collection<Direction>> crossroadDirections : crossroads.asMap().entrySet()) {
            Position crossroadPosition = crossroadDirections.getKey();
            List<PathSection> ways = crossroadDirections.getValue().stream()
                    .map(direction -> getPathSection(crossroadPosition, direction, crossroads.keySet(), ignoreSlope))
                    .filter(pathSection -> pathSection.length() > 0)
                    .toList();
            result.put(crossroadPosition, ways);
        }
        return result;
    }

    private PathSection getPathSection(Position position, Direction direction, Set<Position> junctions, boolean ignoreSlopes) {
        Set<Position> way = new HashSet<>();
        Position testPosition = position.moveTo(direction);
        Direction lastDirection = direction;
        while (!junctions.contains(testPosition)) {
            final Position pos = testPosition;
            Direction slopeDirection = ignoreSlopes ? null : slopes.get(testPosition);
            List<Direction> potentialDirections = Optional.ofNullable(slopeDirection).map(List::of).orElse(ALLOWED_DIRECTIONS).stream()
                    .filter(Predicate.not(lastDirection.opposite()::equals))
                    .filter(d -> !forest.contains(pos.moveTo(d)))
                    .toList();
            if (potentialDirections.isEmpty()) {
                return new PathSection(null, null, 0L);
            }
            way.add(testPosition);
            lastDirection = potentialDirections.iterator().next();
            testPosition = testPosition.moveTo(lastDirection);
        }
        return new PathSection(position, testPosition, way.size() + 1L);
    }

    private static Map.Entry<Set<Position>, Map<Position, Direction>> getHikingMap(List<String> inputLines) {
        Set<Position> result = new HashSet<>();
        Map<Position, Direction> slopes = new HashMap<>();
        for (int y = 0; y < inputLines.size(); y++) {
            final String line = inputLines.get(y);
            for (int x = 0; x < line.length(); x++) {
                final char c = line.charAt(x);
                Direction direction = INPUT_DIRECTIONS.get(c);
                if (direction != null) {
                    slopes.put(new Position(x, y), direction);
                } else if (c == '#') {
                    result.add(new Position(x, y));
                }
            }
        }
        return Map.entry(result, slopes);
    }

    private Multimap<Position, Direction> getCrossroads(Position min, Position max) {
        Multimap<Position, Direction> result = ArrayListMultimap.create();
        for (long y = min.y(); y <= max.y(); y++) {
            for (long x = min.x(); x < max.x(); x++) {
                Position position = new Position(x, y);
                if (forest.contains(position)) {
                    continue;
                }
                List<Direction> allowedDirections = ALLOWED_DIRECTIONS.stream()
                        .filter(direction -> isValidPosition(position.moveTo(direction), min, max))
                        .toList();
                if (allowedDirections.size() > 2 || position.equals(start) || position.equals(finish)) {
                    result.putAll(position, allowedDirections);
                }
            }
        }
        return result;
    }

    private boolean isValidPosition(Position position, Position min, Position max) {
        if (forest.contains(position)) {
            return false;
        }
        return min.x() <= position.x() && position.x() <= max.x()
                && min.y() <= position.y() && position.y() <= max.y();
    }

    public static void main(String... args) throws IOException {
        Day23 day = new Day23();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    private record PathSection(Position position, Position target, long length) {
    }
}
