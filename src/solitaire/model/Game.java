package solitaire.model;

import solitaire.model.board.Board;
import solitaire.model.board.BoardFactory;

import java.util.*;

public class Game {

    private Board board;
    private GameStatus status = GameStatus.IDLE;
    private int[] selected;
    private final Deque<Move> history = new ArrayDeque<>();

    public void newGame(String boardType, int size) {
        board    = BoardFactory.create(boardType, size);
        status   = GameStatus.PLAYING;
        selected = null;
        history.clear();
    }

    public boolean select(int row, int col) {
        if (status != GameStatus.PLAYING) return false;
        if (board.getGrid()[row][col] == Cell.PEG) {
            selected = new int[]{row, col};
            return true;
        }
        selected = null;
        return false;
    }

    public boolean attemptMove(int row, int col) {
        if (selected == null) return false;

        Optional<Move> match = board.validMoves().stream()
            .filter(m -> Arrays.equals(m.origin(), selected)
                      && Arrays.equals(m.destination(), new int[]{row, col}))
            .findFirst();

        if (match.isEmpty()) {
            selected = null;
            return false;
        }

        board.apply(match.get());
        history.push(match.get());
        selected = null;
        evaluateStatus();
        return true;
    }

    public boolean undo() {
        if (history.isEmpty() || board == null) return false;
        board.reverse(history.pop());
        status   = GameStatus.PLAYING;
        selected = null;
        return true;
    }

    public Set<String> reachableFromSelected() {
        if (selected == null) return Set.of();
        Set<String> keys = new HashSet<>();
        for (Move m : board.validMoves())
            if (Arrays.equals(m.origin(), selected))
                keys.add(m.destination()[0] + "," + m.destination()[1]);
        return keys;
    }

    private void evaluateStatus() {
        if (board.pegCount() == 1)         status = GameStatus.WON;
        else if (board.validMoves().isEmpty()) status = GameStatus.LOST;
    }

    public Board getBoard()        { return board; }
    public GameStatus getStatus()  { return status; }
    public int[] getSelected()     { return selected; }
    public boolean hasBoard()      { return board != null; }
}
