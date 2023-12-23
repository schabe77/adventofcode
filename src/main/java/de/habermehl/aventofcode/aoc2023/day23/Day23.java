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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
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
        Entry<Set<Position>, List<Pose>> input = getHikingMap(inputLines);
        forest = input.getKey();
        slopes = input.getValue().stream().collect(Collectors.toMap(Pose::position, Pose::direction));
        start = new Position(1L, 0);
        finish = new Position(inputLines.get(0).length() - 2L, inputLines.size() - 1L);
    }

    public long getPart1() {
        return getLongestHike(false);
    }

    public long getPart2() {
        return getLongestHike(true);
    }

    public long getLongestHike(boolean ignoreSlopes) {
        Map<Position, List<PathSection>> junctions = getJunctions(ignoreSlopes);

        List<List<PathSection>> wayCandidates = new ArrayList<>();
        wayCandidates.add(List.of(junctions.get(start).iterator().next()));

        long longestHike = Long.MIN_VALUE;
        while (!wayCandidates.isEmpty()) {
            List<List<PathSection>> newWayCandidates = new ArrayList<>();
            for (List<PathSection> wayCandidate : wayCandidates) {
                PathSection lastStep = wayCandidate.get(wayCandidate.size() - 1);
                for (PathSection value : junctions.get(lastStep.target())) {
                    Position nextPosition = value.target();
                    if (nextPosition.equals(finish)) {
                        long wayLength = wayCandidate.stream().mapToLong(PathSection::wayLength).sum() + value.wayLength();
                        longestHike = Math.max(longestHike, wayLength);
                    } else if (wayCandidate.stream().noneMatch(pathSection -> pathSection.position().equals(nextPosition))) {
                        newWayCandidates.add(ImmutableList.<PathSection>builder().addAll(wayCandidate).add(value).build());
                    }
                }
            }
            wayCandidates = newWayCandidates;
        }
        return longestHike;
    }

    private Map<Position, List<PathSection>> getJunctions(boolean ignoreSlope) {
        LongSummaryStatistics xStats = forest.stream().mapToLong(Position::x).summaryStatistics();
        LongSummaryStatistics yStats = forest.stream().mapToLong(Position::y).summaryStatistics();
        Position min = new Position(xStats.getMin(), yStats.getMin());
        Position max = new Position(xStats.getMax(), yStats.getMax());

        Multimap<Position, Direction> junctions = getJunctions(min, max);
        Map<Position, List<PathSection>> result = new HashMap<>();
        for (Entry<Position, Collection<Direction>> junctionDirections : junctions.asMap().entrySet()) {
            Position junctionPosition = junctionDirections.getKey();
            List<PathSection> ways = junctionDirections.getValue().stream()
                    .map(direction -> getPathSection(junctionPosition, direction, junctions.keySet(), ignoreSlope))
                    .filter(pathSection -> pathSection.wayLength() > 0)
                    .toList();
            result.put(junctionDirections.getKey(), ways);
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

    private static Map.Entry<Set<Position>, List<Pose>> getHikingMap(List<String> inputLines) {
        Set<Position> result = new HashSet<>();
        List<Pose> slopes = new ArrayList<>();
        for (int y = 0; y < inputLines.size(); y++) {
            final String line = inputLines.get(y);
            for (int x = 0; x < line.length(); x++) {
                final char c = line.charAt(x);
                Direction direction = INPUT_DIRECTIONS.get(c);
                if (direction != null) {
                    slopes.add(new Pose(new Position(x, y), direction));
                } else if (c == '#') {
                    result.add(new Position(x, y));
                }
            }
        }
        result.addAll(getSurroundingForest(inputLines.get(0).length(), inputLines.size()));
        return Map.entry(result, slopes);
    }

    private static Set<Position> getSurroundingForest(int gridWidth, int gridHeight) {
        Set<Position> result = new HashSet<>();
        IntStream.of(-1, gridHeight)
                .mapToObj(y -> IntStream.rangeClosed(-1, gridWidth).mapToObj(x -> new Position(x, y)).toList())
                .forEach(result::addAll);
        IntStream.of(-1, gridWidth)
                .mapToObj(x -> IntStream.rangeClosed(-1, gridHeight).mapToObj(y -> new Position(x, y)).toList())
                .forEach(result::addAll);
        return result;
    }

    private Multimap<Position, Direction> getJunctions(Position min, Position max) {
        Multimap<Position, Direction> result = ArrayListMultimap.create();
        for (long y = min.y(); y <= max.y(); y++) {
            for (long x = min.x(); x < max.x(); x++) {
                Position position = new Position(x, y);
                if (forest.contains(position)) {
                    continue;
                }
                List<Direction> allowedDirections = ALLOWED_DIRECTIONS.stream()
                        .filter(direction -> !forest.contains(position.moveTo(direction)))
                        .toList();

                if (allowedDirections.size() > 2 || position.equals(start) || position.equals(finish)) {
                    result.putAll(position, allowedDirections);
                }
            }
        }
        return result;
    }

    public static void main(String... args) throws IOException {
        Day23 day = new Day23();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    private record PathSection(Position position, Position target, long wayLength) {
    }

    private record Pose(Position position, Direction direction) {
    }
}
