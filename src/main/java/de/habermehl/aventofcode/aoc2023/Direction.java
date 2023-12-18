package de.habermehl.aventofcode.aoc2023;

import java.util.stream.Stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Direction {
    NORTH(0, -1),
    EAST(+1, 0),
    SOUTH(0, +1),
    WEST(-1, 0);
    private static final Direction[] ALL = Direction.values();
    private static final Details[] DETAILS = Stream.of(ALL)
            .mapToInt(Direction::ordinal)
            .mapToObj(ordinal -> new Details(ALL[(ordinal + ALL.length - 1) % ALL.length], ALL[(ordinal + 1) % ALL.length], ALL[(ordinal + 2) % ALL.length]))
            .toArray(Details[]::new);

    private final long x;
    private final long y;

    public long x() {
        return x;
    }

    public long y() {
        return y;
    }

    public Direction left() {
        return getDetails().left();
    }

    public Direction right() {
        return getDetails().right();
    }

    public Direction opposite() {
        return getDetails().opposite();
    }

    private Details getDetails() {
        return DETAILS[this.ordinal()];
    }

    private record Details(Direction left, Direction right, Direction opposite) {
    }
}
