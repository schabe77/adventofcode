package de.habermehl.aventofcode.aoc2023.day08;

import static de.habermehl.aventofcode.aoc2023.Utils.getLeastCommonMultiple;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Iterators;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day8 {

    private static final Pattern TARGETS = Pattern.compile("(\\w+)\\s+=\\s+\\((\\w+), (\\w+)\\)");
    private final List<String> inputLines;
    private final Map<String, Node> nodes;

    public Day8() throws IOException {
        this(Utils.getInput("aoc2023/day08/input"));
    }

    Day8(List<String> inputLines) {
        this.inputLines = inputLines;
        nodes = getTargetNodes(inputLines);
    }

    public long getPart1() {
        return getSteps("AAA", "ZZZ"::equals);
    }

    public long getPart2() {
        long i = 1;
        for (String start : nodes.keySet().stream().filter(value -> value.endsWith("A")).toList()) {
            i = getLeastCommonMultiple(i, getSteps(start, value -> value.endsWith("Z")));
        }
        return i;
    }

    private long getSteps(String start, Predicate<String> endCondition) {
        Iterator<String> directions = Iterators.cycle(inputLines.get(0).split(""));
        String nextWay = start;
        long steps = 0;
        do {
            steps++;
            String direction = directions.next();
            nextWay = nodes.get(nextWay).getNext(direction);
        } while (!endCondition.test(nextWay));
        return steps;
    }

    private static Map<String, Node> getTargetNodes(List<String> inputLines) {
        Map<String, Node> result = new LinkedHashMap<>();
        for (String inputLine : inputLines) {
            Matcher matcher = TARGETS.matcher(inputLine);
            if (matcher.find()) {
                result.put(matcher.group(1), new Node(matcher.group(2), matcher.group(3)));
            }
        }
        return result;
    }

    public static void main(String... args) throws IOException {
        Day8 day = new Day8();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    private record Node(String left, String right) {
        String getNext(String direction) {
            return "L".equals(direction) ? left : right;
        }
    }
}
