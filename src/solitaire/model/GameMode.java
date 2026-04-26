package solitaire.model;

import solitaire.model.board.Board;
import solitaire.model.board.BoardFactory;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class GameMode {

    protected Board board;
    protected GameStatus status = GameStatus.IDLE;
    protected final Deque<Move> history = new ArrayDeque<>();
    protected GameRecord record = null;

    public void newGame(String boardType, int size) {
        board = BoardFactory.create(boardType, size);
        status = GameStatus.PLAYING;
        history.clear();
        if (record != null) {
            record = new GameRecord(boardType, size, getModeName());
        }
    }

    public boolean undo() {
        if (history.isEmpty() || board == null) return false;
        board.reverse(history.pop());
        status = GameStatus.PLAYING;
        return true;
    }

    public void applyMove(Move move) {
        board.apply(move);
        history.push(move);
        evaluateStatus();
    }

    public void loadBoardState(Cell[][] state) {
        board.loadState(state);
        history.clear();
        status = GameStatus.PLAYING;
    }

    protected void evaluateStatus() {
        if (board.pegCount() == 1) {
            status = GameStatus.WON;
        } else if (board.validMoves().isEmpty()) {
            status = GameStatus.LOST;
        }
    }

    public Board getBoard() {
        return board;
    }

    public GameStatus getStatus() {
        return status;
    }

    public boolean hasBoard() {
        return board != null;
    }

    public Deque<Move> getHistory() {
        return history;
    }

    public void setRecording(boolean enabled) {
        record = enabled && board != null
            ? new GameRecord(board.getBoardType(), board.getSize(), getModeName())
            : null;
    }

    public GameRecord getRecord() { return record; }

    public void setState(Board board, Deque<Move> history, GameStatus status) {
        this.board = board;
        this.history.clear();
        this.history.addAll(history);
        this.status = status;
    }

    public abstract boolean handleCellClick(int row, int col);

    public abstract String getModeName();
}
