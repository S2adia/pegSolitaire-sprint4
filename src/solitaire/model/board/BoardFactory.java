package solitaire.model.board;

import java.util.List;

public class BoardFactory {

    public static final List<String> TYPES = List.of("English", "Diamond", "Hexagon");

    public static Board create(String type, int size) {
        if (size < 5 || size > 9 || size % 2 == 0) {
            throw new IllegalArgumentException("Board size must be odd and between 5 and 9, got: " + size);
        }
        return switch (type) {
            case "English"  -> new EnglishBoard(size);
            case "Diamond"  -> new DiamondBoard(size);
            case "Hexagon"  -> new HexagonBoard(size);
            default         -> throw new IllegalArgumentException("Unknown board type: " + type);
        };
    }
}
