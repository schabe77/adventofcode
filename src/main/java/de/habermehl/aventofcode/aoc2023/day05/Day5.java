package de.habermehl.aventofcode.aoc2023.day05;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day5 {
    private static final Pattern SEEDS_PATTERN = Pattern.compile("^seeds:\\s+([\\d\\s]+)");
    private static final Pattern MAPPING_NAME_PATTERN = Pattern.compile("(\\w+)-to-(\\w+) map:");
    private static final Pattern MAPPING_VALUE_PATTERN = Pattern.compile("(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private final List<String> inputLines;

    public Day5() throws IOException {
        this(Utils.getInput("aoc2023/day05/input"));
    }

    Day5(List<String> inputLines) {
        this.inputLines = inputLines;
    }

    public long getPart1() {
        return getMinLocation(getAlmanac(inputLines, this::getSeedRangesPart1));
    }

    private List<Range> getSeedRangesPart1(List<Long> source) {
        return source.stream()
                .map(seed -> new Range(seed, seed))
                .toList();
    }

    public long getPart2() {
        return getMinLocation(getAlmanac(inputLines, this::getSeedRangesPart2));
    }

    private List<Range> getSeedRangesPart2(List<Long> source) {
        List<Range> seedRanges = new ArrayList<>();
        for (int i = 0; i < source.size(); i += 2) {
            long start = source.get(i);
            long length = source.get(i + 1);
            seedRanges.add(new Range(start, start + length - 1));
        }
        return seedRanges;
    }

    private long getMinLocation(Almanac almanac) {
        String resourceName = "seed";
        List<Range> resources = almanac.seeds();
        do {
            DestinationMappings mappings = almanac.getSourceToTargetMappings(resourceName);
            resources = resources.stream()
                    .map(mappings::getMappingTargets)
                    .flatMap(Collection::stream)
                    .distinct()
                    .toList();
            resourceName = mappings.targetResourceName();
        } while (!"location".equals(resourceName));
        return resources.stream().mapToLong(Range::start).min().orElse(Long.MAX_VALUE);
    }

    private static Almanac getAlmanac(List<String> inputLines, Function<List<Long>, List<Range>> seedRangesProvider) {
        Map<String, DestinationMappings> result = new HashMap<>();
        Map.Entry<String, String> resourceMappingNames = null;
        List<Range> seeds = null;
        for (String inputLine : inputLines) {
            if (seeds == null) {
                seeds = parseSeeds(inputLine, seedRangesProvider).orElse(null);
                continue;
            }
            Map.Entry<String, String> newResourceMappingNames = parseMappingName(inputLine).orElse(null);
            if (newResourceMappingNames != null) {
                resourceMappingNames = newResourceMappingNames;
                continue;
            }
            Map<Range, Long> destinationDiffs = parseDestinationDiffs(inputLine).orElse(null);
            if (destinationDiffs != null && resourceMappingNames != null) {
                String source = resourceMappingNames.getKey();
                String destination = resourceMappingNames.getValue();
                result.computeIfAbsent(source, key -> new DestinationMappings(destination, new TreeMap<>(Range.ASCENDING_ORDER)))
                        .destinationDiffs().putAll(destinationDiffs);
            }
        }
        final Map<String, DestinationMappings> fullRangeEntries = new HashMap<>();
        for (Map.Entry<String, DestinationMappings> entry : result.entrySet()) {
            fullRangeEntries.put(entry.getKey(), entry.getValue().withFullRangeMapping());
        }
        return new Almanac(seeds, fullRangeEntries);
    }

    private static Optional<List<Range>> parseSeeds(String inputLine, Function<List<Long>, List<Range>> seedRangesProvider) {
        Matcher matcher = SEEDS_PATTERN.matcher(inputLine);
        if (matcher.find()) {
            final List<Long> seeds = Stream.of(matcher.group(1).split("\\s+")).map(Long::valueOf).toList();
            return Optional.of(seedRangesProvider.apply(seeds));
        }
        return Optional.empty();
    }

    private static Optional<Map.Entry<String, String>> parseMappingName(String inputLine) {
        Matcher matcher = MAPPING_NAME_PATTERN.matcher(inputLine);
        if (matcher.find()) {
            return Optional.of(Map.entry(matcher.group(1), matcher.group(2)));
        }
        return Optional.empty();
    }

    private static Optional<Map<Range, Long>> parseDestinationDiffs(String inputLine) {
        Matcher matcher = MAPPING_VALUE_PATTERN.matcher(inputLine);
        if (matcher.find()) {
            long destination = Long.parseLong(matcher.group(1));
            long source = Long.parseLong(matcher.group(2));
            long range = Long.parseLong(matcher.group(3));
            long diff = destination - source;
            return Optional.of(Map.of(new Range(source, source + range - 1), diff));
        }
        return Optional.empty();
    }

    public static void main(String... args) throws IOException {
        Day5 day = new Day5();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    private record Almanac(List<Range> seeds, Map<String, DestinationMappings> resourceMappings) {
        DestinationMappings getSourceToTargetMappings(String resourceName) {
            return resourceMappings.get(resourceName);
        }
    }

    private record DestinationMappings(String targetResourceName, TreeMap<Range, Long> destinationDiffs) {
        public List<Range> getMappingTargets(Range mappingSource) {
            List<Range> result = new ArrayList<>();
            for (Map.Entry<Range, Long> entry : destinationDiffs.entrySet()) {
                long diff = entry.getValue();
                entry.getKey().getIntersect(mappingSource)
                        .ifPresent(intersect -> result.add(new Range(intersect.start() + diff, intersect.end() + diff)));
            }
            return result;
        }

        public DestinationMappings withFullRangeMapping() {
            return new DestinationMappings(targetResourceName, getFullRangesDestinationDiffs(destinationDiffs));
        }

        static TreeMap<Range, Long> getFullRangesDestinationDiffs(Map<Range, Long> destinationDiffs) {
            final TreeMap<Range, Long> targetMap = new TreeMap<>(Range.ASCENDING_ORDER);
            for (Map.Entry<Range, Long> entry : destinationDiffs.entrySet().stream().sorted(Map.Entry.comparingByKey(Range.ASCENDING_ORDER)).toList()) {
                if (targetMap.isEmpty() && entry.getKey().start() > Long.MIN_VALUE) {
                    targetMap.put(new Range(Long.MIN_VALUE, entry.getKey().start() - 1), 0L);
                }
                if (targetMap.lastKey().end() + 1 < entry.getKey().start()) {
                    targetMap.put(new Range(targetMap.lastKey().end() + 1, entry.getKey().start() - 1), 0L);
                }
                targetMap.put(entry.getKey(), entry.getValue());
            }
            if (targetMap.lastKey().end() < Long.MAX_VALUE) {
                targetMap.put(new Range(targetMap.lastKey().end() + 1, Long.MAX_VALUE), 0L);
            }
            return targetMap;
        }
    }

    private record Range(long start, long end) {
        static final Comparator<Range> ASCENDING_ORDER = Comparator.comparingLong(Range::start)
                .thenComparingLong(Range::end);

        Optional<Range> getIntersect(Range other) {
            long start = Math.max(start(), other.start());
            long end = Math.min(end(), other.end());
            return start <= end ? Optional.of(new Range(start, end)) : Optional.empty();
        }
    }
}
