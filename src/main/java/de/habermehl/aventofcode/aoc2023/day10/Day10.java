package de.habermehl.aventofcode.aoc2023.day10;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.habermehl.aventofcode.aoc2023.Direction;
import de.habermehl.aventofcode.aoc2023.Polygon;
import de.habermehl.aventofcode.aoc2023.Position;
import de.habermehl.aventofcode.aoc2023.Utils;

import lombok.AllArgsConstructor;

public class Day10 {

    private final PipeMap sourceMap;
    private final Position startPosition;

    public Day10() throws IOException {
        this(Utils.getInput("aoc2023/day10/input"));
    }

    Day10(List<String> inputLines) {
        Entry<PipeMap, Position> mapAndStartPoint = getMapAndStartPosition(inputLines);
        sourceMap = mapAndStartPoint.getKey();
        startPosition = mapAndStartPoint.getValue();
    }

    public long getPart1() {
        return getUsedPipesMinDistances(sourceMap, startPosition).values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(Long.MAX_VALUE);
    }

    public long getPart1v2() {
        return Math.round(getWayThroughPipe(sourceMap, startPosition).size() / 2.0);
    }

    public long getPart2() {
        Set<Position> usedPipesPositions = getUsedPipesMinDistances(sourceMap, startPosition).keySet();
        PipeMap cleanPipeMap = sourceMap.onlyWithPipesAt(usedPipesPositions);
        PipeMap highResolutionPipeMap = cleanPipeMap.withDoubledResolution();
        Set<Position> spacesConnectedToOutside = highResolutionPipeMap.getSpacesConnectedToOutside();

        return cleanPipeMap.getNonPipePositions().stream()
                .map(position -> new Position(position.x() * 2, position.y() * 2))
                .filter(Predicate.not(spacesConnectedToOutside::contains))
                .count();
    }

    public long getPart2v2() {
        Set<Position> wayThroughPipe = getWayThroughPipe(sourceMap, startPosition);
        Polygon polygon = new Polygon(new ArrayList<>(wayThroughPipe));
        long sum = 0;
        for (long x = sourceMap.min().x(); x <= sourceMap.max().x(); x++) {
            for (long y = sourceMap.min().y(); y <= sourceMap.max().y(); y++) {
                final Position position = new Position(x, y);
                if (!wayThroughPipe.contains(position) && polygon.contains(position)) {
                    sum++;
                }
            }
        }
        return sum;
    }

    private static LinkedHashSet<Position> getWayThroughPipe(PipeMap map, Position startPosition) {
        LinkedHashSet<Position> wayThroughPipe = new LinkedHashSet<>();
        Position lastPosition = startPosition;
        do {
            wayThroughPipe.add(lastPosition);
            lastPosition = map.getConnectedPoints(lastPosition).stream()
                    .filter(Predicate.not(wayThroughPipe::contains))
                    .findFirst()
                    .orElse(null);
        } while (lastPosition != null);
        return wayThroughPipe;
    }

    private static Map<Position, Long> getUsedPipesMinDistances(PipeMap map, Position startPosition) {
        List<List<Position>> unfinishedWays = new ArrayList<>();
        List<List<Position>> finishedWays = new ArrayList<>();
        unfinishedWays.add(List.of(startPosition));
        while (!unfinishedWays.isEmpty()) {
            List<List<Position>> newUnfinishedWays = new ArrayList<>();
            for (List<Position> unfinishedWay : unfinishedWays) {
                List<Position> nextStepTargets = map.getConnectedPoints(unfinishedWay.get(unfinishedWay.size() - 1)).stream()
                        .filter(Predicate.not(unfinishedWay::contains))
                        .toList();
                if (nextStepTargets.isEmpty()) {
                    finishedWays.add(unfinishedWay);
                    continue;
                }
                for (Position connectedPosition : nextStepTargets) {
                    ArrayList<Position> newWay = new ArrayList<>(unfinishedWay);
                    newWay.add(connectedPosition);
                    newUnfinishedWays.add(newWay);
                }
            }
            unfinishedWays = newUnfinishedWays;
        }
        Map<Position, Long> minPositions = new HashMap<>();
        for (List<Position> finishedWay : finishedWays) {
            for (int i = 0; i < finishedWay.size(); i++) {
                final long currentPos = i;
                minPositions.compute(finishedWay.get(i), (key, oldValue) -> Math.min(Optional.ofNullable(oldValue).orElse(Long.MAX_VALUE), currentPos));
            }
        }
        return minPositions;
    }

    private static Map.Entry<PipeMap, Position> getMapAndStartPosition(List<String> inputLines) {
        Set<Pipe> result = new HashSet<>();
        Position startPosition = null;
        for (int y = 0; y < inputLines.size(); y++) {
            final String line = inputLines.get(y);
            for (int x = 0; x < line.length(); x++) {
                String type = Character.toString(line.charAt(x));
                PipeType pipeType = PipeType.getPipeType(type);
                if (pipeType != null) {
                    result.add(new Pipe(pipeType, new Position(x, y)));
                } else if ("S".equals(type)) {
                    startPosition = new Position(x, y);
                } else if (!".".equals(type)) {
                    throw new IllegalStateException("unknown type found: " + type);
                }
            }
        }
        final Pipe startPipe = getStartPointPipe(startPosition, result);
        result.add(startPipe);
        return Map.entry(new PipeMap(result), startPipe.position());
    }

    private static Pipe getStartPointPipe(Position startPosition, Set<Pipe> pipes) {
        final Map<Position, Pipe> pipePositionLookup = pipes.stream()
                .collect(Collectors.toMap(Pipe::position, Function.identity()));
        for (PipeType pipeType : PipeType.values()) {
            Pipe pipe = new Pipe(pipeType, startPosition);
            boolean validType = true;
            for (Position position : pipe.getConnectedPipesPositions()) {
                Pipe other = pipePositionLookup.get(position);
                if (other == null || !other.isConnectedTo(pipe)) {
                    validType = false;
                }
            }
            if (validType) {
                return pipe;
            }
        }
        throw new IllegalStateException("no pipe type found");
    }

    public static void main(String... args) throws IOException {
        Day10 day = new Day10();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    @AllArgsConstructor
    private enum PipeType {
        LOWER_LEFT("L", List.of(Direction.EAST, Direction.NORTH)),
        UPPER_LEFT("F", List.of(Direction.EAST, Direction.SOUTH)),
        LOWER_RIGHT("J", List.of(Direction.WEST, Direction.NORTH)),
        UPPER_RIGHT("7", List.of(Direction.WEST, Direction.SOUTH)),
        VERTICAL("|", List.of(Direction.NORTH, Direction.SOUTH)),
        HORIZONTAL("-", List.of(Direction.WEST, Direction.EAST));

        private final String text;
        private final List<Direction> connections;

        private static final Map<String, PipeType> REVERSE_LOOKUP = Stream.of(PipeType.values())
                .collect(Collectors.toMap(pipeType -> pipeType.text, Function.identity()));

        public List<Position> getConnections(Position position) {
            return connections.stream()
                    .map(position::moveTo)
                    .toList();
        }

        public static PipeType getPipeType(String text) {
            return REVERSE_LOOKUP.get(text);
        }
    }

    private record PipeMap(Position min, Position max, Map<Position, Pipe> pipePositions) {
        public PipeMap(Collection<Pipe> pipes) {
            this(getMinMax(pipes), pipes);
        }

        private PipeMap(Map.Entry<Position, Position> minMaxPosition, Collection<Pipe> pipes) {
            this(minMaxPosition.getKey(), minMaxPosition.getValue(), pipes.stream().collect(Collectors.toMap(Pipe::position, Function.identity())));
        }

        public PipeMap onlyWithPipesAt(Set<Position> positions) {
            return new PipeMap(positions.stream().map(pipePositions::get).filter(Objects::nonNull).toList());
        }

        public PipeMap withDoubledResolution() {
            List<Pipe> newPositions = new ArrayList<>();
            for (Pipe pipe : pipePositions.values()) {
                Position scaledPosition = new Position(pipe.position().x() * 2, pipe.position().y() * 2);
                newPositions.add(new Pipe(pipe.type(), scaledPosition));
                Optional.ofNullable(pipePositions.get(pipe.position().moveTo(Direction.EAST)))
                        .filter(rightSidePipe -> rightSidePipe.isConnectedTo(pipe))
                        .ifPresent(rightSidePipe -> newPositions.add(new Pipe(PipeType.HORIZONTAL, scaledPosition.moveTo(Direction.EAST))));
                Optional.ofNullable(pipePositions.get(pipe.position().moveTo(Direction.SOUTH)))
                        .filter(downSidePipe -> downSidePipe.isConnectedTo(pipe))
                        .ifPresent(downSidePipe -> newPositions.add(new Pipe(PipeType.VERTICAL, scaledPosition.moveTo(Direction.SOUTH))));
            }
            return new PipeMap(newPositions);
        }

        public Set<Position> getSpacesConnectedToOutside() {
            Position start = min.moveTo(Direction.WEST).moveTo(Direction.NORTH);
            Position end = max.moveTo(Direction.EAST).moveTo(Direction.SOUTH);

            Set<Position> knownCandidatesPositions = new HashSet<>(List.of(start));
            LinkedList<Position> positionsToCheck = new LinkedList<>(List.of(start));
            while (!positionsToCheck.isEmpty()) {
                final Position position = positionsToCheck.pop();
                for (Position surroundingPoint : getSurroundingPoints(position)) {
                    if (!knownCandidatesPositions.contains(surroundingPoint) && pipePositions.get(surroundingPoint) == null && isInGrid(position, start, end)) {
                        knownCandidatesPositions.add(surroundingPoint);
                        positionsToCheck.add(surroundingPoint);
                    }
                }
            }
            return knownCandidatesPositions;
        }

        private static Set<Position> getSurroundingPoints(Position p) {
            return Set.of(p.moveTo(Direction.NORTH), p.moveTo(Direction.EAST), p.moveTo(Direction.SOUTH), p.moveTo(Direction.WEST));
        }

        private static boolean isInGrid(Position position, Position min, Position max) {
            return position.x() >= min.x() && position.x() <= max.x()
                    && position.y() >= min.y() && position.y() <= max.y();
        }

        public Set<Position> getNonPipePositions() {
            Set<Position> result = new HashSet<>();
            for (long x = min.x(); x <= max.x(); x++) {
                for (long y = min.y(); y <= max.y(); y++) {
                    Position position = new Position(x, y);
                    if (!pipePositions.containsKey(position)) {
                        result.add(position);
                    }
                }
            }
            return result;
        }

        public List<Position> getConnectedPoints(Position point) {
            final Pipe pipe = pipePositions.get(point);
            if (pipe == null) {
                return List.of();
            }
            return pipe.getConnectedPipesPositions().stream()
                    .map(pipePositions::get)
                    .filter(otherPipe -> otherPipe != null && otherPipe.isConnectedTo(pipe))
                    .map(Pipe::position)
                    .toList();
        }

        private static Map.Entry<Position, Position> getMinMax(Collection<Pipe> pipes) {
            TreeSet<Long> allX = new TreeSet<>();
            TreeSet<Long> allY = new TreeSet<>();
            for (Pipe pipe : pipes) {
                allX.add(pipe.position().x());
                allY.add(pipe.position().y());
            }
            return Map.entry(new Position(allX.first(), allY.first()), new Position(allX.last(), allY.last()));
        }
    }

    private record Pipe(PipeType type, Position position) {
        List<Position> getConnectedPipesPositions() {
            return type.getConnections(position);
        }

        boolean isConnectedTo(Pipe other) {
            if (!getConnectedPipesPositions().contains(other.position)) {
                return false;
            }
            return other.getConnectedPipesPositions().contains(position);
        }
    }
}
