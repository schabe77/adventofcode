package de.habermehl.aventofcode.aoc2023.day17;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.habermehl.aventofcode.aoc2023.Direction;
import de.habermehl.aventofcode.aoc2023.Grid;
import de.habermehl.aventofcode.aoc2023.Grid.GridEntry;
import de.habermehl.aventofcode.aoc2023.Position;
import de.habermehl.aventofcode.aoc2023.Utils;

public class Day17 {
    private final Grid<NumberPosition> grid;

    public Day17() throws IOException {
        this(Utils.getInput("aoc2023/day17/input"));
    }

    Day17(List<String> inputLines) {
        grid = getGrid(inputLines);
    }

    public long getPart1() {
        return getMinimumHeatLoss(1, 3);
    }

    public long getPart2() {
        return getMinimumHeatLoss(4, 10);
    }

    private long getMinimumHeatLoss(int minForwardSteps, int maxForwardSteps) {
        Map<Step, Long> storedHeatLosses = new HashMap<>();
        LinkedList<Step> steps = new LinkedList<>();
        steps.add(new Step(grid.min(), Direction.EAST, 0));
        steps.add(new Step(grid.min(), Direction.SOUTH, 0));
        while (!steps.isEmpty()) {
            Step previousStep = steps.removeFirst();
            Direction lastDirection = previousStep.direction();
            for (Direction newDirection : List.of(lastDirection.left(), lastDirection, lastDirection.right())) {
                boolean changedDirection = newDirection != lastDirection;
                if ((changedDirection && previousStep.forwardSteps() < minForwardSteps)
                        || (!changedDirection && previousStep.forwardSteps() >= maxForwardSteps)) {
                    continue;
                }
                Step nextStep = previousStep.moveTo(newDirection);
                Long newHeatLoss = grid.getEntry(nextStep.position())
                        .map(entry -> entry.number() + storedHeatLosses.getOrDefault(previousStep, 0L))
                        .orElse(null);
                Long lastCalculatedHeatLoss = storedHeatLosses.get(nextStep);
                if (newHeatLoss != null && (lastCalculatedHeatLoss == null || newHeatLoss < lastCalculatedHeatLoss)) {
                    storedHeatLosses.put(nextStep, newHeatLoss);
                    steps.add(nextStep);
                }
            }
        }
        return storedHeatLosses.entrySet().stream()
                .filter(wayPoint -> wayPoint.getKey().position().equals(grid.max()) && wayPoint.getKey().forwardSteps() >= minForwardSteps)
                .mapToLong(Entry::getValue)
                .min()
                .orElse(Long.MIN_VALUE);
    }

    private static Grid<NumberPosition> getGrid(List<String> inputLines) {
        List<NumberPosition> result = new ArrayList<>();
        for (int y = 0; y < inputLines.size(); y++) {
            final String line = inputLines.get(y);
            for (int x = 0; x < line.length(); x++) {
                result.add(new NumberPosition(new Position(x, y), Integer.parseInt(String.valueOf(line.charAt(x)))));
            }
        }
        return new Grid<>(result);
    }

    public static void main(String... args) throws IOException {
        Day17 day = new Day17();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    private record Step(Position position, Direction direction, int forwardSteps) {
        Step moveTo(Direction newDirection) {
            return new Step(position.moveTo(newDirection), newDirection, direction == newDirection ? forwardSteps + 1 : 1);
        }
    }

    private record NumberPosition(Position position, int number) implements GridEntry {
    }
}
