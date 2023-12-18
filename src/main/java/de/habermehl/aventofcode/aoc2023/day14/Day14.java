package de.habermehl.aventofcode.aoc2023.day14;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Iterators;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import de.habermehl.aventofcode.aoc2023.Direction;
import de.habermehl.aventofcode.aoc2023.Position;
import de.habermehl.aventofcode.aoc2023.Utils;

public class Day14 {

    private final List<String> input;

    public Day14() throws IOException {
        this(Utils.getInput("aoc2023/day14/input"));
    }

    Day14(List<String> source) {
        input = source;
    }

    public long getPart1() {
        Grid grid = getGrid();
        grid.move(Direction.NORTH);
        return grid.getValue();
    }

    public long getPart2() {
        final int targetRuns = 1000000000;
        Grid grid = getGrid();
        Map<Long, Integer> resultHashCycle = new HashMap<>();
        int i = 0;
        while (i < targetRuns) {
            i++;
            grid.moveCycle();
            long hash = grid.getHash();
            Integer cycleWithSameResult = resultHashCycle.get(hash);
            if (cycleWithSameResult == null) {
                resultHashCycle.put(hash, i);
            } else {
                int diff = i - cycleWithSameResult;
                i = targetRuns - (targetRuns - i) % diff;
            }
        }
        return grid.getValue();
    }

    private Grid getGrid() {
        Set<Position> rocks = new HashSet<>();
        Set<Position> stones = new HashSet<>();
        for (int y = 0; y < input.size(); y++) {
            String line = input.get(y);
            for (int x = 0; x < line.length(); x++) {
                char c = line.charAt(x);
                if (c == 'O') {
                    stones.add(new Position(x, y));
                } else if (c == '#') {
                    rocks.add(new Position(x, y));
                }
            }
        }
        Position min = new Position(0, 0);
        Position max = new Position(input.get(0).length() - 1L, input.size() - 1L);
        // add rocks around grid - stones will stop there
        for (int x = -1; x <= max.x() + 1; x++) {
            for (int y = -1; y <= max.y() + 1; y++) {
                if (min.x() <= x && x <= max.x() && min.y() <= y && y <= max.y()) {
                    continue;
                }
                rocks.add(new Position(x, y));
            }
        }
        return new Grid(stones, rocks, min, max);
    }

    public static void main(String... args) throws IOException {
        Day14 day = new Day14();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    private record Grid(Set<Position> stones, Set<Position> rocks, Position min, Position max) {
        private static final Direction[] FULL_CYCLE = { Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST };

        public long getValue() {
            return stones().stream()
                    .mapToLong(position -> max.y() - position.y() + 1)
                    .sum();
        }

        void moveCycle() {
            Stream.of(FULL_CYCLE)
                    .forEach(this::move);
        }

        void move(Direction direction) {
            Set<Position> newPositions = new HashSet<>();
            while (!stones.isEmpty()) {
                move(Iterators.consumingIterator(stones.iterator()).next(), direction, newPositions);
            }
            stones.addAll(newPositions);
        }

        private boolean move(Position p, Direction direction, Set<Position> newPositions) {
            Position stone = p;
            boolean moved = false;
            while (true) {
                Position targetPosition = stone.moveTo(direction);
                if (rocks.contains(targetPosition) || newPositions.contains(targetPosition)) {
                    // target already blocked by unmovable element
                    newPositions.add(stone);
                    return moved;
                } else if (stones.contains(targetPosition)) {
                    // target blocked by another stone - try to move that one first
                    stones.remove(targetPosition);
                    if (!move(targetPosition, direction, newPositions)) {
                        // stone at target position couldn't be moved, this stone can't move either
                        newPositions.add(stone);
                        return moved;
                    }
                }
                stone = targetPosition;
                moved = true;
            }
        }

        @SuppressWarnings("UnstableApiUsage")
        public long getHash() {
            final Hasher hasher = Hashing.farmHashFingerprint64().newHasher();
            stones.stream()
                    .sorted(Comparator.comparing(Position::x).thenComparing(Position::y))
                    .forEach(position -> hasher.putLong(position.x()).putLong(position.y()));
            return hasher.hash().asLong();
        }
    }
}
