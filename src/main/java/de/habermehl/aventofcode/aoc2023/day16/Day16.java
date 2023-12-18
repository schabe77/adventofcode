package de.habermehl.aventofcode.aoc2023.day16;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import de.habermehl.aventofcode.aoc2023.Direction;
import de.habermehl.aventofcode.aoc2023.Grid;
import de.habermehl.aventofcode.aoc2023.Grid.GridEntry;
import de.habermehl.aventofcode.aoc2023.Position;
import de.habermehl.aventofcode.aoc2023.Utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Day16 {
    private final Grid<Mirror> grid;

    public Day16() throws IOException {
        this(Utils.getInput("aoc2023/day16/input"));
    }

    Day16(List<String> inputLines) {
        grid = getGrid(inputLines);
    }

    public long getPart1() {
        return getEnergizedFieldsAmount(new Ray(new Position(grid.min().x() - 1, grid.min().y()), Direction.EAST));
    }

    public long getPart2() {
        return getPart2InitialRays().stream()
                .mapToLong(this::getEnergizedFieldsAmount)
                .max()
                .orElse(Long.MAX_VALUE);
    }

    private long getEnergizedFieldsAmount(Ray initialRay) {
        Set<Ray> alreadyVisited = new HashSet<>();
        LinkedList<Ray> rays = new LinkedList<>();
        rays.add(initialRay);
        while (!rays.isEmpty()) {
            Ray ray = rays.removeFirst();
            Position nextPosition = ray.getNextPosition();
            if (!grid.isInGrid(nextPosition)) {
                continue;
            }
            List<Direction> newDirections = grid.getEntry(nextPosition)
                    .map(mirror -> mirror.getReflectionTargets(ray.direction().opposite()))
                    .orElse(List.of(ray.direction()));
            for (Direction newDirection : newDirections) {
                Ray newRay = new Ray(nextPosition, newDirection);
                if (alreadyVisited.add(newRay)) {
                    rays.add(newRay);
                }
            }
        }
        return alreadyVisited.stream().map(Ray::position).distinct().count();
    }

    private List<Ray> getPart2InitialRays() {
        final List<Ray> initialRays = new ArrayList<>();
        LongStream.rangeClosed(grid.min().x(), grid.max().x())
                .mapToObj(x -> List.of(
                        new Ray(new Position(x, -1), Direction.SOUTH),
                        new Ray(new Position(x, grid.max().y() + 1), Direction.NORTH)))
                .forEach(initialRays::addAll);
        LongStream.rangeClosed(grid.min().y(), grid.max().y())
                .mapToObj(y -> List.of(
                        new Ray(new Position(-1, y), Direction.EAST),
                        new Ray(new Position(grid.max().x() + 1, y), Direction.WEST)))
                .forEach(initialRays::addAll);
        return initialRays;
    }

    private static Grid<Mirror> getGrid(List<String> inputLines) {
        List<Mirror> result = new ArrayList<>();
        for (int y = 0; y < inputLines.size(); y++) {
            final String line = inputLines.get(y);
            for (int x = 0; x < line.length(); x++) {
                String type = Character.toString(line.charAt(x));
                MirrorType mirrorType = MirrorType.of(type);
                if (mirrorType != null) {
                    result.add(new Mirror(mirrorType, new Position(x, y)));
                } else if (!".".equals(type)) {
                    throw new IllegalStateException("unknown type found: " + type);
                }
            }
        }
        return new Grid(result);
    }

    public static void main(String... args) throws IOException {
        Day16 day = new Day16();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    @AllArgsConstructor
    private enum MirrorType {
        SLASH("/", Map.of(
                Direction.NORTH, List.of(Direction.WEST),
                Direction.WEST, List.of(Direction.NORTH),
                Direction.EAST, List.of(Direction.SOUTH),
                Direction.SOUTH, List.of(Direction.EAST)
        )),
        BACKSLASH("\\", Map.of(
                Direction.NORTH, List.of(Direction.EAST),
                Direction.WEST, List.of(Direction.SOUTH),
                Direction.EAST, List.of(Direction.NORTH),
                Direction.SOUTH, List.of(Direction.WEST)
        )),
        PIPE("|", Map.of(
                Direction.WEST, List.of(Direction.NORTH, Direction.SOUTH),
                Direction.EAST, List.of(Direction.NORTH, Direction.SOUTH)
        )),
        DASH("-", Map.of(
                Direction.NORTH, List.of(Direction.EAST, Direction.WEST),
                Direction.SOUTH, List.of(Direction.EAST, Direction.WEST)
        ));
        public static final Map<String, MirrorType> REVERSE_LOOKUP = Arrays.stream(MirrorType.values())
                .collect(Collectors.toMap(MirrorType::getText, Function.identity()));

        @Getter
        private final String text;
        private final Map<Direction, List<Direction>> targets;

        public static MirrorType of(String text) {
            return REVERSE_LOOKUP.get(text);
        }

        public List<Direction> getReflectionTargets(Direction sourceDirection) {
            return targets.getOrDefault(sourceDirection, List.of(sourceDirection.opposite()));
        }
    }

    //

    private record Ray(Position position, Direction direction) {
        Position getNextPosition() {
            return position.moveTo(direction);
        }
    }

    private record Mirror(MirrorType type, Position position) implements GridEntry {
        List<Direction> getReflectionTargets(Direction sourceDirection) {
            return type.getReflectionTargets(sourceDirection);
        }
    }
}
