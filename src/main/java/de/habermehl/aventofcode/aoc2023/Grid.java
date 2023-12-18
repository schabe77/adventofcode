package de.habermehl.aventofcode.aoc2023;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.habermehl.aventofcode.aoc2023.Grid.GridEntry;

public record Grid<T extends GridEntry>(Position min, Position max, Map<Position, T> entryLookup) {

    public Grid(Collection<T> entries) {
        this(getMinMax(entries), entries);
    }

    private Grid(Map.Entry<Position, Position> minMaxPosition, Collection<T> entries) {
        this(minMaxPosition.getKey(), minMaxPosition.getValue(), entries.stream().collect(Collectors.toMap(T::position, Function.identity())));
    }

    public Optional<T> getEntry(Position position) {
        return Optional.ofNullable(entryLookup.get(position));
    }

    public boolean isInGrid(Position position) {
        return position.x() >= min.x() && position.x() <= max.x()
                && position.y() >= min.y() && position.y() <= max.y();
    }

    public static <T extends GridEntry> Map.Entry<Position, Position> getMinMax(Collection<T> entries) {
        TreeSet<Long> allX = new TreeSet<>();
        TreeSet<Long> allY = new TreeSet<>();
        for (T entry : entries) {
            allX.add(entry.position().x());
            allY.add(entry.position().y());
        }
        return Map.entry(new Position(allX.first(), allY.first()), new Position(allX.last(), allY.last()));
    }

    public interface GridEntry {
        Position position();
    }
}
