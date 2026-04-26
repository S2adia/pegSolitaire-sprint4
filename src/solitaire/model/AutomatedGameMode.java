package solitaire.model;

import java.util.List;
import java.util.Random;

public class AutomatedGameMode extends GameMode {

    private final Random random = new Random();

    @Override
    public boolean handleCellClick(int row, int col) {
        return false;
    }

    public boolean autoplay() {
        if (status != GameStatus.PLAYING) return false;

        List<Move> moves = board.validMoves();
        if (moves.isEmpty()) return false;

        Move move = moves.get(random.nextInt(moves.size()));
        board.apply(move);
        history.push(move);
        if (record != null) record.addMove(move);
        evaluateStatus();
        return true;
    }

    @Override
    public String getModeName() {
        return "Automated";
    }
}
