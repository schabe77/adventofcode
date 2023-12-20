package de.habermehl.aventofcode.aoc2023.day20;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import de.habermehl.aventofcode.aoc2023.Utils;
import de.habermehl.aventofcode.aoc2023.day20.Day20.Module.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class Day20 {

    private static final Pattern PATTERN = Pattern.compile("^([&%]?)(\\w+) -> (.*)");
    private static final Module BUTTON = Module.builder().name("button").type(Type.BROADCAST).targets(List.of("broadcaster")).build();

    private final List<String> inputLines;

    public Day20() throws IOException {
        this(Utils.getInput("aoc2023/day20/input"));
    }

    Day20(List<String> inputLines) {
        this.inputLines = inputLines;
    }

    public long getPart1() {
        Map<String, Module> modules = loadModules().stream()
                .collect(Collectors.toMap(Module::getName, Function.identity()));

        Multiset<Signal> sentSignals = HashMultiset.create();
        for (int i = 0; i < 1000; i++) {
            final LinkedList<SignalTarget> queue = new LinkedList<>(BUTTON.getSignals(null, Signal.LOW));
            while (!queue.isEmpty()) {
                SignalTarget signalTarget = queue.removeFirst();
                sentSignals.add(signalTarget.signal());
                queue.addAll(modules.get(signalTarget.target()).getSignals(signalTarget.source(), signalTarget.signal()));
            }
        }

        return sentSignals.entrySet().stream().mapToLong(Multiset.Entry::getCount).reduce(1, (a, b) -> a * b);
    }

    public long getPart2() {
        Map<String, Module> modules = loadModules().stream()
                .collect(Collectors.toMap(Module::getName, Function.identity()));

        Map<String, Long> watchTargets = modules.values().stream()
                .filter(module -> module.getTargets().contains("rx"))
                .flatMap(rxSource -> rxSource.getLastReceivedSignals().keySet().stream().map(source -> rxSource.getName() + "." + source))
                .collect(Collectors.toMap(Function.identity(), source -> Long.MIN_VALUE));

        AtomicLong run = new AtomicLong(0);
        while (true) {
            run.incrementAndGet();
            final LinkedList<SignalTarget> queue = new LinkedList<>(BUTTON.getSignals(null, Signal.LOW));
            while (!queue.isEmpty()) {
                SignalTarget signalTarget = queue.removeFirst();
                if (signalTarget.signal == Signal.HIGH) {
                    watchTargets.computeIfPresent(signalTarget.target() + "." + signalTarget.source.getName(),
                            (key, oldValue) -> oldValue == Long.MIN_VALUE ? run.get() : oldValue);
                }
                queue.addAll(modules.get(signalTarget.target()).getSignals(signalTarget.source(), signalTarget.signal()));
            }

            if (!watchTargets.containsValue(Long.MIN_VALUE)) {
                return watchTargets.values().stream().mapToLong(Long::longValue).reduce(1, Utils::getLeastCommonMultiple);
            }
        }
    }

    private List<Module> loadModules() {
        Map<String, Module> modules = new ConcurrentHashMap<>();
        for (String inputLine : inputLines) {
            Matcher matcher = PATTERN.matcher(inputLine);
            if (matcher.find()) {
                String type = Strings.nullToEmpty(matcher.group(1));
                String name = matcher.group(2);
                List<String> targets = Stream.of(matcher.group(3).split(",")).map(String::trim).toList();
                modules.put(name, Module.builder().name(name).type(Type.of(type)).targets(targets).build());
            }
        }
        for (Module module : modules.values()) {
            if (module.getType() == Type.BROADCAST) {
                continue;
            }
            for (String target : module.getTargets()) {
                Module targetModule = modules.computeIfAbsent(target, key -> Module.builder().name(target).type(Type.NOOP).targets(List.of()).build());
                targetModule.registerSource(module.getName());
            }
        }
        return new ArrayList<>(modules.values());
    }

    public static void main(String... args) throws IOException {
        Day20 day = new Day20();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    @AllArgsConstructor
    @Builder
    @Data
    static final class Module {
        private final String name;
        private final Type type;
        private final List<String> targets;
        private final Map<String, Signal> lastReceivedSignals = new HashMap<>();
        private boolean active;

        public List<SignalTarget> getSignals(Module source, Signal signal) {
            return switch (type) {
                case NOOP -> List.of();
                case BROADCAST -> getSignals(signal);
                case FLIP_FLOP -> getFlipFlopSignals(signal);
                case CONJUNCTION -> getConjunctionSignals(source, signal);
            };
        }

        public void registerSource(String name) {
            lastReceivedSignals.put(name, Signal.LOW);
        }

        private List<SignalTarget> getFlipFlopSignals(Signal signal) {
            if (signal == Signal.HIGH) {
                return List.of();
            }
            active = !active;
            return getSignals(active ? Signal.HIGH : Signal.LOW);
        }

        private List<SignalTarget> getConjunctionSignals(Module source, Signal signal) {
            lastReceivedSignals.put(source.getName(), signal);
            Signal sendSignal = lastReceivedSignals.values().stream().anyMatch(Signal.LOW::equals) ? Signal.HIGH : Signal.LOW;
            return getSignals(sendSignal);
        }

        private List<SignalTarget> getSignals(Signal signal) {
            return targets.stream().map(target -> new SignalTarget(signal, this, target)).toList();
        }

        @AllArgsConstructor
        enum Type {
            NOOP(null),
            BROADCAST(""),
            FLIP_FLOP("%"),
            CONJUNCTION("&");
            private static final Map<String, Type> REVERSE_LOOKUP = Stream.of(Type.values())
                    .filter(moduleType -> moduleType.text != null)
                    .collect(Collectors.toMap(moduleType -> moduleType.text, Function.identity()));

            private final String text;

            public static Type of(String text) {
                return REVERSE_LOOKUP.get(text);
            }
        }
    }

    record SignalTarget(Signal signal, Module source, String target) {
    }

    enum Signal {
        LOW,
        HIGH;
    }
}
