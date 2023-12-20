package de.habermehl.aventofcode.aoc2023;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public final class Utils {
    private Utils() {
        // nothing to initialize
    }

    public static List<String> getInput(String filename) throws IOException {
        final URL resource = Utils.class.getClassLoader().getResource(filename);
        if (resource == null) {
            throw new IOException("couldn't find resource " + filename);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()))) {
            return reader.lines().toList();
        }
    }

    public static long getLeastCommonMultiple(long m, long n) {
        return (m * n) / getGreatestCommonDivisor(m, n);
    }

    public static long getGreatestCommonDivisor(long m, long n) {
        return n == 0 ? m : getGreatestCommonDivisor(n, m % n);
    }

    public static char[][] getGrid(List<String> input) {
        char[][] grid = new char[input.size()][input.get(0).length()];
        for (int y = 0; y < input.size(); y++) {
            String line = input.get(y);
            for (int x = 0; x < line.length(); x++) {
                grid[y][x] = line.charAt(x);
            }
        }
        return grid;

    }

    public static char[][] flipRight(char[][] source) {
        char[][] result = new char[source[0].length][source.length];
        for (int y = 0; y < source.length; y++) {
            char[] line = source[y];
            for (int x = 0; x < line.length; x++) {
                result[x][source.length - y - 1] = line[x];
            }
        }
        return result;
    }
}
