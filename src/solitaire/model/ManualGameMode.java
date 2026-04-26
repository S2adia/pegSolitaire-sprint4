package solitaire.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class ManualGameMode extends GameMode {

    private int[] selected;
    private final Random random = new Random();

    @Override
    public boolean handleCellClick(int row, int col) {
        if (status != GameStatus.PLAYING) return false;

        if (selected != null) {
            boolean moved = attemptMove(row, col);
            if (!moved) {
                select(row, col);
            }
            return moved;
        } else {
            return select(row, col);
        }
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
        if (record != null) record.addMove(match.get());
        selected = null;
        evaluateStatus();
        return true;
    }

    public Set<String> reachableFromSelected() {
        if (selected == null) return Set.of();
        Set<String> keys = new HashSet<>();
        for (Move m : board.validMoves()) {
            if (Arrays.equals(m.origin(), selected)) {
                keys.add(m.destination()[0] + "," + m.destination()[1]);
            }
        }
        return keys;
    }

    public int[] getSelected() {
        return selected;
    }

    public void randomize() {
        if (board == null) return;

        int target = 3 + random.nextInt(8); // 3-10 moves
        int applied = 0;

        while (applied < target) {
            List<Move> moves = board.validMoves();
            if (moves.isEmpty()) break;

            Move move = moves.get(random.nextInt(moves.size()));
            board.apply(move);
            applied++;
        }

        if (record != null) record.addRandomize(board.getGrid());
        history.clear();
        status = GameStatus.PLAYING;
    }

    @Override
    public String getModeName() {
        return "Manual";
    }

    @Override
    public boolean undo() {
        boolean result = super.undo();
        if (result) {
            selected = null;
        }
        return result;
    }
}
