package de.habermehl.aventofcode.aoc2023;

import java.util.List;

public record Polygon(List<Position> vertices) {
    public boolean contains(Position test) {
        return contains(test.x(), test.y());
    }

    public boolean contains(double x, double y) {
        boolean result = false;
        Position v1 = vertices.get(vertices.size() - 1);
        for (Position v2 : vertices) {
            if ((v1.y() > y) != (v2.y() > y)
                    && x < (v1.x() - v2.x()) * (y - v2.y()) / (v2.y() - v1.y()) + v1.x()) {
                result = !result;
            }
            v1 = v2;
        }
        return result;
    }
}