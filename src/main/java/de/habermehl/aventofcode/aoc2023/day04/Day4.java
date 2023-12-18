package de.habermehl.aventofcode.aoc2023.day04;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day4 {
    private static final Pattern GAME_PATTERN = Pattern.compile("^Card.*?: (.*)\\|(.*)$");
    private static final Pattern VALUES_PATTERN = Pattern.compile("(\\d+)");

    private final List<String> inputLines;

    public Day4() throws IOException {
        this(Utils.getInput("aoc2023/day04/input"));
    }

    Day4(List<String> inputLines) {
        this.inputLines = inputLines;
    }

    public int getPart1() {
        int points = 0;
        for (String inputLine : inputLines) {
            int matchingNumbersAmount = getMatchingNumberAmounts(inputLine);
            points += matchingNumbersAmount == 0 ? 0 : (1 << (matchingNumbersAmount - 1));
        }
        return points;
    }

    public int getPart2() {
        Map<Integer, Integer> cardWinningPoints = getCardWinningPoints(inputLines);
        int result = 0;
        for (int cardId : cardWinningPoints.keySet()) {
            result += getWonCards(cardId, cardWinningPoints);
        }
        return result;
    }

    private Map<Integer, Integer> getCardWinningPoints(List<String> inputLines) {
        Map<Integer, Integer> result = new TreeMap<>();
        for (int i = 0; i < inputLines.size(); i++) {
            result.put(i, getMatchingNumberAmounts(inputLines.get(i)));
        }
        return result;
    }

    private static int getMatchingNumberAmounts(String inputLine) {
        Matcher matcher = GAME_PATTERN.matcher(inputLine);
        if (matcher.find()) {
            Set<Integer> winningNumbers = getNumbers(matcher.group(1));
            return (int) getNumbers(matcher.group(2)).stream()
                    .filter(winningNumbers::contains)
                    .count();
        }
        return 0;
    }

    private static Set<Integer> getNumbers(String input) {
        Set<Integer> result = new TreeSet<>();
        Matcher matcher = VALUES_PATTERN.matcher(input);
        while (matcher.find()) {
            result.add(Integer.valueOf(matcher.group(1)));
        }
        return result;
    }

    private int getWonCards(int cardId, Map<Integer, Integer> cardWinningPoints) {
        int result = 1;
        int winningPoints = cardWinningPoints.getOrDefault(cardId, 0);
        for (int i = 1; i <= winningPoints; i++) {
            result += getWonCards(cardId + i, cardWinningPoints);
        }
        return result;
    }

    public static void main(String... args) throws IOException {
        Day4 day = new Day4();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }
}
