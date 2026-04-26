package solitaire.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GameRecord {

    public enum EventType { MOVE, RANDOMIZE }

    public record Event(EventType type, Move move, Cell[][] boardState) {}

    private final String boardType;
    private final int    boardSize;
    private final String gameMode;
    private final List<Event> events = new ArrayList<>();

    public GameRecord(String boardType, int boardSize, String gameMode) {
        this.boardType = boardType;
        this.boardSize = boardSize;
        this.gameMode  = gameMode;
    }

    public void addMove(Move move) {
        events.add(new Event(EventType.MOVE, move, null));
    }

    public void addRandomize(Cell[][] grid) {
        Cell[][] copy = new Cell[grid.length][];
        for (int r = 0; r < grid.length; r++) copy[r] = grid[r].clone();
        events.add(new Event(EventType.RANDOMIZE, null, copy));
    }

    public List<Event> getEvents()  { return List.copyOf(events); }
    public String getBoardType()    { return boardType; }
    public int    getBoardSize()    { return boardSize; }
    public String getGameMode()     { return gameMode; }

    public void save(Path path) throws IOException {
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(path))) {
            w.println(boardType + " " + boardSize + " " + gameMode);
            for (Event e : events) {
                if (e.type() == EventType.MOVE) {
                    Move m = e.move();
                    w.printf("MOVE %d %d %d %d %d %d%n",
                        m.origin()[0], m.origin()[1],
                        m.jumped()[0], m.jumped()[1],
                        m.destination()[0], m.destination()[1]);
                } else {
                    StringBuilder sb = new StringBuilder("RANDOMIZE");
                    for (Cell[] row : e.boardState())
                        for (Cell cell : row)
                            sb.append(' ').append(cellChar(cell));
                    w.println(sb);
                }
            }
        }
    }

    private static char cellChar(Cell c) {
        return switch (c) { case PEG -> 'P'; case HOLE -> 'H'; default -> 'E'; };
    }

    public static GameRecord load(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        String[] header  = lines.get(0).split(" ");
        String boardType = header[0];
        int    boardSize = Integer.parseInt(header[1]);
        String gameMode  = header[2];

        GameRecord record = new GameRecord(boardType, boardSize, gameMode);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("MOVE ")) {
                String[] p = line.split(" ");
                record.events.add(new Event(EventType.MOVE,
                    Move.of(Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                            Integer.parseInt(p[3]), Integer.parseInt(p[4]),
                            Integer.parseInt(p[5]), Integer.parseInt(p[6])),
                    null));

            } else if (line.startsWith("RANDOMIZE ")) {
                String[] p    = line.split(" ");
                Cell[][] grid = new Cell[boardSize][boardSize];
                int idx = 1;
                for (int r = 0; r < boardSize; r++)
                    for (int c = 0; c < boardSize; c++)
                        grid[r][c] = parseCell(p[idx++].charAt(0));
                record.events.add(new Event(EventType.RANDOMIZE, null, grid));
            }
        }
        return record;
    }

    private static Cell parseCell(char ch) {
        return switch (ch) { case 'P' -> Cell.PEG; case 'H' -> Cell.HOLE; default -> Cell.EMPTY; };
    }
}
