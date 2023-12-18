package de.habermehl.aventofcode.aoc2023;

public record Position(long x, long y) {
    public Position moveTo(Direction direction) {
        return new Position(x + direction.x(), y + direction.y());
    }

    public Position moveTo(Direction direction, long steps) {
        return new Position(x + direction.x() * steps, y + direction.y() * steps);
    }
}