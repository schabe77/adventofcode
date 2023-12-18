package de.habermehl.aventofcode.aoc2023.day18;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.habermehl.aventofcode.aoc2023.Direction;
import de.habermehl.aventofcode.aoc2023.Position;
import de.habermehl.aventofcode.aoc2023.Utils;

public class Day18 {

    private static final Pattern PATTERN = Pattern.compile("(\\w+) (\\d+) \\(#(\\w{5})(\\w)\\)");
    private static final Map<String, Direction> DIRECTION_MAPPING = Map.of(
            "R", Direction.EAST,
            "0", Direction.EAST,
            "D", Direction.SOUTH,
            "1", Direction.SOUTH,
            "L", Direction.WEST,
            "2", Direction.WEST,
            "U", Direction.NORTH,
            "3", Direction.NORTH
    );
    private final List<String> inputLines;

    public Day18() throws IOException {
        this(Utils.getInput("aoc2023/day18/input"));
    }

    Day18(List<String> inputLines) {
        this.inputLines = inputLines;
    }

    public long getPart1() {
        return getArea(getInput(true));
    }

    public long getPart2() {
        return getArea(getInput(false));
    }

    private long getArea(List<DiggingInstructions> instructions) {
        long border = 0L;
        long area = 0L;
        Position position = new Position(0L, 0L);
        for (DiggingInstructions instruction : instructions) {
            position = position.moveTo(instruction.direction(), instruction.steps());
            border += instruction.steps();
            if (instruction.direction() == Direction.SOUTH) {
                area += position.x() * instruction.steps();
            } else if (instruction.direction() == Direction.NORTH) {
                area -= position.x() * instruction.steps();
            }
        }
        return Math.abs(area) + border / 2 + 1;
    }

    private List<DiggingInstructions> getInput(boolean part1) {
        List<DiggingInstructions> result = new ArrayList<>();
        for (String inputLine : inputLines) {
            Matcher matcher = PATTERN.matcher(inputLine);
            if (matcher.find()) {
                long steps = part1 ? Long.parseLong(matcher.group(2)) : Long.valueOf(matcher.group(3), 16);
                Direction direction = DIRECTION_MAPPING.get(matcher.group(part1 ? 1 : 4));
                result.add(new DiggingInstructions(direction, steps));
            }
        }
        return result;
    }

    public static void main(String... args) throws IOException {
        Day18 day = new Day18();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    private record DiggingInstructions(Direction direction, long steps) {
    }
}
