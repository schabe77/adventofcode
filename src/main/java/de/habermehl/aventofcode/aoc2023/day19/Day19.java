package de.habermehl.aventofcode.aoc2023.day19;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day19 {

    private static final Pattern WORKFLOW = Pattern.compile("^(\\w+)\\{(.*)\\}");
    private static final Pattern CONDITION = Pattern.compile("((\\w+)([<>])(\\d+):(\\w+)|(\\w+$))");
    private static final Pattern VARIABLE = Pattern.compile("(\\w)=(\\d+)");

    private final List<String> inputLines;

    public Day19() throws IOException {
        this(Utils.getInput("aoc2023/day19/input"));
    }

    Day19(List<String> inputLines) {
        this.inputLines = inputLines;
    }

    public long getPart1() {
        Map<String, Workflow> workflows = loadWorkflows().stream()
                .collect(Collectors.toMap(Workflow::name, Function.identity()));
        long value = 0;
        for (Map<String, Long> ratings : loadRatings()) {
            String target = workflows.get("in").getTarget(ratings);
            while (!"R".equals(target) && !"A".equals(target)) {
                target = workflows.get(target).getTarget(ratings);
            }
            if ("A".equals(target)) {
                value += ratings.values().stream().mapToLong(Long::longValue).sum();
            }
        }
        return value;
    }

    public long getPart2() {
        Map<String, Workflow> workflows = loadWorkflows().stream()
                .collect(Collectors.toMap(Workflow::name, Function.identity()));
        return getAcceptedConditionPaths("in", workflows, List.of()).stream()
                .mapToLong(this::getAcceptedCombinations)
                .sum();
    }

    private long getAcceptedCombinations(List<Condition> conditions) {
        Map<String, Set<Long>> acceptedRatings = new HashMap<>();
        for (String key : List.of("x", "m", "a", "s")) {
            acceptedRatings.put(key, LongStream.rangeClosed(1, 4000).boxed().collect(Collectors.toSet()));
        }
        for (Condition condition : conditions) {
            Set<Long> longs = acceptedRatings.get(condition.ratingKey());
            if (longs == null) {
                continue;
            }
            if ("<".equals(condition.operator())) {
                LongStream.rangeClosed(condition.rating(), 4000).forEach(longs::remove);
            } else {
                LongStream.rangeClosed(1, condition.rating()).forEach(longs::remove);
            }
        }
        return acceptedRatings.values().stream().mapToLong(Set::size).reduce(1, (a, b) -> a * b);
    }

    List<List<Condition>> getAcceptedConditionPaths(String workflowName, Map<String, Workflow> workflows, List<Condition> currentConditions) {
        if ("A".equals(workflowName)) {
            return List.of(currentConditions);
        } else if ("R".equals(workflowName)) {
            return List.of();
        }
        List<List<Condition>> result = new ArrayList<>();
        List<Condition> invertedConditions = new ArrayList<>();
        for (Condition condition : workflows.get(workflowName).conditions()) {
            List<Condition> newConditions = new ArrayList<>(currentConditions);
            newConditions.addAll(invertedConditions);
            if (condition.ratingKey() != null) {
                newConditions.add(condition);
                invertedConditions.add(condition.invert());
            }
            result.addAll(getAcceptedConditionPaths(condition.target(), workflows, newConditions));
        }
        return result;
    }

    private List<Workflow> loadWorkflows() {
        List<Workflow> workflows = new ArrayList<>();
        for (String inputLine : inputLines) {
            Matcher matcher = WORKFLOW.matcher(inputLine);
            if (matcher.find()) {
                Workflow workflow = createWorkflow(matcher.group(1), matcher.group(2));
                workflows.add(workflow);
            }
        }
        return workflows;
    }

    private Workflow createWorkflow(String name, String conditionsValue) {
        List<Condition> conditions = new ArrayList<>();
        for (String condition : conditionsValue.split(",")) {
            Matcher matcher = CONDITION.matcher(condition);
            while (matcher.find()) {
                if (matcher.group(6) == null) {
                    conditions.add(new Condition(matcher.group(2), matcher.group(3), Long.valueOf(matcher.group(4)), matcher.group(5)));
                } else {
                    conditions.add(Condition.otherwise(matcher.group(6)));
                }
            }
        }
        return new Workflow(name, conditions);
    }

    private List<Map<String, Long>> loadRatings() {
        List<Map<String, Long>> result = new ArrayList<>();
        for (String inputLine : inputLines) {
            if (!inputLine.startsWith("{")) {
                continue;
            }
            Map<String, Long> variables = new HashMap<>();
            Matcher matcher = VARIABLE.matcher(inputLine);
            while (matcher.find()) {
                variables.put(matcher.group(1), Long.valueOf(matcher.group(2)));
            }
            result.add(variables);
        }
        return result;
    }

    public static void main(String... args) throws IOException {
        Day19 day = new Day19();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    private record Workflow(String name, List<Condition> conditions) {
        public String getTarget(Map<String, Long> ratings) {
            for (Condition condition : conditions) {
                String target = condition.getTarget(ratings).orElse(null);
                if (target != null) {
                    return target;
                }
            }
            throw new IllegalStateException("unexpected state: no condition matches for " + name + " / " + ratings);
        }
    }

    private record Condition(String ratingKey, String operator, Long rating, String target) {
        public static Condition otherwise(String target) {
            return new Condition(null, null, null, target);
        }

        public Optional<String> getTarget(Map<String, Long> ratings) {
            if (ratingKey == null) {
                return Optional.of(target);
            }
            boolean matches = switch (operator) {
                case "<" -> ratings.get(ratingKey) < rating;
                case ">" -> ratings.get(ratingKey) > rating;
                default -> false;
            };
            return matches ? Optional.of(target) : Optional.empty();
        }

        public Condition invert() {
            return switch (operator) {
                case "<" -> new Condition(ratingKey, ">", rating - 1, target);
                case ">" -> new Condition(ratingKey, "<", rating + 1, target);
                default -> throw new IllegalStateException(this + " is not invertible");
            };
        }
    }
}
