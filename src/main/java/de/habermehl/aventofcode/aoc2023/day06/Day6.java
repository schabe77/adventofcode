package de.habermehl.aventofcode.aoc2023.day06;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day6 {
    private static final Pattern VALUE_PATTERN = Pattern.compile("(Time|Distance):\\s+([\\d\\s]+)");
    private final List<String> inputLines;

    public Day6() throws IOException {
        this(Utils.getInput("aoc2023/day06/input"));
    }

    Day6(List<String> inputLines) {
        this.inputLines = inputLines;
    }

    public long getPart1() {
        return getResult(UnaryOperator.identity());
    }

    public long getPart2() {
        return getResult(string -> string.replace(" ", ""));
    }

    private long getResult(UnaryOperator<String> timeInputProcessor) {
        return getTimeDistances(timeInputProcessor).entrySet().stream()
                .map(entry -> getRacesAmountSurpassingDistancePq(entry.getKey(), entry.getValue()))
                .reduce(1L, Math::multiplyExact);
    }

    private static long getRacesAmountSurpassingDistancePq(long raceTimeMillis, long distanceToPass) {
        long p = -raceTimeMillis;
        long q = distanceToPass;

        double firstPqPart = -(p / 2.0);
        // we don't want the x for the distance to pass, but a higher distance (add 1 to the current distance record)
        double secondPqPart = Math.sqrt(((p * p) / 4.0) - (q + 1));

        long firstDistancePassingButtonPressTime = (long) Math.ceil(firstPqPart - secondPqPart);
        long secondDistancePassingButtonPressTime = (long) Math.floor(firstPqPart + secondPqPart);

        return secondDistancePassingButtonPressTime - firstDistancePassingButtonPressTime + 1;
    }

    private Map<Long, Long> getTimeDistances(UnaryOperator<String> timeInputProcessor) {
        Map<String, List<Long>> values = new HashMap<>();
        for (String inputLine : inputLines) {
            Matcher matcher = VALUE_PATTERN.matcher(inputLine);
            if (matcher.find()) {
                values.put(matcher.group(1), Stream.of(timeInputProcessor.apply(matcher.group(2)).split("\\s+")).map(Long::valueOf).toList());
            }
        }
        Iterator<Long> times = values.get("Time").iterator();
        Iterator<Long> distances = values.get("Distance").iterator();
        Map<Long, Long> result = new HashMap<>();
        while (times.hasNext() && distances.hasNext()) {
            result.put(times.next(), distances.next());
        }
        return result;
    }

    public static void main(String... args) throws IOException {
        Day6 day = new Day6();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }
}
