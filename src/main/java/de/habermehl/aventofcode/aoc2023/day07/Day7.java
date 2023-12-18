package de.habermehl.aventofcode.aoc2023.day07;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.habermehl.aventofcode.aoc2023.Utils;

public class Day7 {

    private static final Map<String, Integer> HAND_VALUES = Map.of(
            "5", 6,
            "4", 5,
            "3-2", 4,
            "3", 3,
            "2-2", 2,
            "2", 1);
    private static final Pattern CARD_BIDS = Pattern.compile("(\\w{5})\\s+(\\d+)");
    private final List<CardsBid> cardsBids;

    public Day7() throws IOException {
        this(Utils.getInput("aoc2023/day07/input"));
    }

    Day7(List<String> inputLines) {
        cardsBids = getCardsBids(inputLines);
    }

    public long getPart1() {
        return getWinnings("23456789TJQKA", "");
    }

    public long getPart2() {
        return getWinnings("J23456789TQKA", "J");
    }

    private long getWinnings(String cardValueOrder, String joker) {
        Map<String, Integer> handsValues = cardsBids.stream()
                .collect(Collectors.toMap(CardsBid::cards, cardsBid -> getHandValue(cardsBid.cards(), joker)));
        Map<Character, Integer> cardsValues = getCardsValues(cardValueOrder);
        CardsBid[] sortedCards = cardsBids.stream()
                .sorted(Comparator.comparing(CardsBid::cards, Comparator.comparingInt(handsValues::get))
                        .thenComparing(CardsBid::cards, (cards1, cards2) -> compareCardValues(cards1, cards2, cardsValues)))
                .toArray(CardsBid[]::new);
        long result = 0;
        for (int i = 1; i <= sortedCards.length; i++) {
            result += i * sortedCards[i - 1].bid();
        }
        return result;
    }

    private static int getHandValue(String cards, String joker) {
        Map<String, Integer> cardTypeAmount = cards.codePoints()
                .mapToObj(Character::toString)
                .collect(Collectors.toMap(Function.identity(), c -> 1, Integer::sum));
        if (cardTypeAmount.containsKey(joker)) {
            final String mostOccurringCard = cardTypeAmount.entrySet().stream()
                    .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
                    .map(Entry::getKey)
                    .filter(Predicate.not(joker::equals))
                    .findFirst()
                    .orElse(joker);
            return getHandValue(cards.replace(joker, mostOccurringCard), "");
        }
        String cardsAmount = cardTypeAmount.values().stream()
                .sorted(Comparator.reverseOrder())
                .map(String::valueOf)
                .collect(Collectors.joining("-"));
        return HAND_VALUES.entrySet().stream()
                .filter(entry -> cardsAmount.startsWith(entry.getKey()))
                .map(Entry::getValue)
                .max(Comparator.naturalOrder())
                .orElse(0);
    }

    private static int compareCardValues(String hand1, String hand2, Map<Character, Integer> cardsValues) {
        for (int i = 0; i < hand1.toCharArray().length; i++) {
            int result = Integer.compare(cardsValues.get(hand1.charAt(i)), cardsValues.get(hand2.charAt(i)));
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private static List<CardsBid> getCardsBids(List<String> inputLines) {
        List<CardsBid> result = new ArrayList<>();
        for (String inputLine : inputLines) {
            Matcher matcher = CARD_BIDS.matcher(inputLine);
            if (matcher.find()) {
                result.add(new CardsBid(matcher.group(1), Long.parseLong(matcher.group(2))));
            }
        }
        return result;
    }

    private static Map<Character, Integer> getCardsValues(String cardByValueOrder) {
        AtomicInteger value = new AtomicInteger(0);
        return cardByValueOrder.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toMap(Function.identity(), card -> value.incrementAndGet()));
    }

    public static void main(String... args) throws IOException {
        Day7 day = new Day7();
        System.out.println(day.getClass().getSimpleName() + " / part1: " + day.getPart1());
        System.out.println(day.getClass().getSimpleName() + " / part2: " + day.getPart2());
    }

    private record CardsBid(String cards, long bid) {
    }
}
