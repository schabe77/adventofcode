package de.habermehl.aventofcode.aoc2023.day22;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day22 {
    private static final Pattern BRICK_POSITIONS = Pattern.compile("(\\d+),(\\d+),(\\d+)~(\\d+),(\\d+),(\\d+)");
    private final List<Brick> bricks;
    private final Multimap<Long, Long> bricksAbove;
    private final Multimap<Long, Long> bricksBelow;

    public Day22() throws IOException {
        this(Utils.getInput("aoc2023/day22/input"));
    }

    Day22(List<String> inputLines) {
        bricks = getDroppedBricks(getBricks(inputLines));
        bricksAbove = getBricksAbove(bricks);
        bricksBelow = bricksAbove.entries().stream()
                .collect(Multimaps.toMultimap(Map.Entry::getValue, Map.Entry::getKey, LinkedListMultimap::create));
    }

    public long getPart1() {
        return bricks.stream()
                .filter(brick -> getFallingBricksAmountWhenRemoved(brick) == 0L)
                .count();
    }

    public long getPart2() {
        return bricks.stream()
                .mapToLong(this::getFallingBricksAmountWhenRemoved)
                .sum();
    }

    long getFallingBricksAmountWhenRemoved(Brick brick) {
        Set<Long> alreadyFallen = new HashSet<>(List.of(brick.ordinal()));
        List<Long> testBricks = List.of(brick.ordinal());
        while (!testBricks.isEmpty()) {
            List<Long> newTestBricks = new ArrayList<>();
            for (Long testBrick : testBricks) {
                Collection<Long> aboveBricks = bricksAbove.get(testBrick);
                for (Long aboveBrick : aboveBricks) {
                    if (bricksBelow.get(aboveBrick).stream().noneMatch(Predicate.not(alreadyFallen::contains))) {
                        newTestBricks.add(aboveBrick);
                        alreadyFallen.add(aboveBrick);
                    }
                }
            }
            testBricks = newTestBricks;
        }
        return alreadyFallen.size() - 1L;
    }

    private static Multimap<Long, Long> getBricksAbove(List<Brick> bricks) {
        Multimap<Long, Long> bricksAbove = LinkedListMultimap.create();
        for (int i = 0; i < bricks.size(); i++) {
            Brick brickA = bricks.get(i);
            for (int j = i + 1; j < bricks.size(); j++) {
                Brick brickB = bricks.get(j);
                if (brickA.isDirectlyBelow(brickB)) {
                    bricksAbove.put(brickA.ordinal(), brickB.ordinal());
                } else if (brickB.isDirectlyBelow(brickA)) {
                    bricksAbove.put(brickB.ordinal(), brickA.ordinal());
                }
            }
        }
        return bricksAbove;
    }

    private static List<Brick> getBricks(List<String> inputLines) {
        List<Brick> bricks = new ArrayList<>();
        long line = 0;
        for (String inputLine : inputLines) {
            Matcher m = BRICK_POSITIONS.matcher(inputLine);
            if (m.find()) {
                Brick brick = Brick.of(line,
                        new Voxel(Long.parseLong(m.group(1)), Long.parseLong(m.group(2)), Long.parseLong(m.group(3))),
                        new Voxel(Long.parseLong(m.group(4)), Long.parseLong(m.group(5)), Long.parseLong(m.group(6)))
                );
                bricks.add(brick);
            }
            line++;
        }
        return bricks;
    }

    private static List<Brick> getDroppedBricks(List<Brick> source) {
        List<Brick> result = new ArrayList<>();
        Set<Voxel> alreadyUsedVoxels = new HashSet<>();
        for (Brick brick : source.stream().sorted(Comparator.comparing(brick -> brick.from().z())).toList()) {
            while (brick.from().z() > 1 && brick.getBelowPlane().stream().noneMatch(alreadyUsedVoxels::contains)) {
                brick = brick.moveDown(1L);
            }
            result.add(brick);
            alreadyUsedVoxels.addAll(brick.voxels());
        }
        return result;
    }

    public static void main(String... args) throws IOException {
        Day22 day = new Day22();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    record Voxel(long x, long y, long z) {
    }

    record Brick(long ordinal, Set<Voxel> voxels, Voxel from, Voxel to) {

        public boolean isDirectlyBelow(Brick other) {
            return !Sets.intersection(getAbovePlane(), other.getLowerPlane()).isEmpty();
        }

        public Brick moveDown(long amount) {
            if (amount == 0) {
                return this;
            }
            return Brick.of(ordinal, new Voxel(from.x(), from().y(), from().z() - amount), new Voxel(to.x(), to().y(), to().z() - amount));
        }

        public Set<Voxel> getAbovePlane() {
            return getPlane(to.z() + 1);
        }

        public Set<Voxel> getBelowPlane() {
            return getPlane(from.z() - 1);
        }

        public Set<Voxel> getLowerPlane() {
            return getPlane(from.z());
        }

        private Set<Voxel> getPlane(long z) {
            final Set<Voxel> result = new HashSet<>();
            for (long x = from.x(); x <= to.x(); x++) {
                for (long y = from.y(); y <= to.y(); y++) {
                    result.add(new Voxel(x, y, z));
                }
            }
            return result;
        }

        static Brick of(long ordinal, Voxel from, Voxel to) {
            Set<Voxel> voxels = new HashSet<>();
            for (long x = from.x(); x <= to.x(); x++) {
                for (long y = from.y(); y <= to.y(); y++) {
                    for (long z = from.z(); z <= to.z(); z++) {
                        voxels.add(new Voxel(x, y, z));
                    }
                }
            }
            return new Brick(ordinal, voxels, from, to);
        }
    }
}
