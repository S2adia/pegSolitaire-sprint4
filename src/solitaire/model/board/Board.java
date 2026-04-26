package solitaire.model.board;

import solitaire.model.Cell;
import solitaire.model.Move;

import java.util.ArrayList;
import java.util.List;

public abstract class Board {

    protected final int size;
    protected Cell[][] grid;

    protected Board(int size) {
        this.size = size;
        this.grid = new Cell[size][size];
        fill(Cell.EMPTY);
        build();
    }

    protected abstract void build();

    public abstract List<int[]> directions();

    public List<Move> validMoves() {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] != Cell.PEG) continue;
                for (int[] dir : directions()) {
                    int rj = r + dir[0], cj = c + dir[1];
                    int rd = r + dir[0] * 2, cd = c + dir[1] * 2;
                    if (isPlayable(rj, cj) && grid[rj][cj] == Cell.PEG
                            && isPlayable(rd, cd) && grid[rd][cd] == Cell.HOLE) {
                        moves.add(Move.of(r, c, rj, cj, rd, cd));
                    }
                }
            }
        }
        return moves;
    }

    public void apply(Move move) {
        set(move.origin(),      Cell.HOLE);
        set(move.jumped(),      Cell.HOLE);
        set(move.destination(), Cell.PEG);
    }

    public void reverse(Move move) {
        set(move.origin(),      Cell.PEG);
        set(move.jumped(),      Cell.PEG);
        set(move.destination(), Cell.HOLE);
    }

    public void loadState(Cell[][] state) {
        for (int r = 0; r < size; r++)
            grid[r] = state[r].clone();
    }

    public int pegCount() {
        int count = 0;
        for (Cell[] row : grid)
            for (Cell cell : row)
                if (cell == Cell.PEG) count++;
        return count;
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public int getSize() {
        return size;
    }

    public String getBoardType() {
        return this.getClass().getSimpleName().replace("Board", "");
    }


    public boolean isPlayable(int r, int c) {
        return r >= 0 && r < size && c >= 0 && c < size && grid[r][c] != Cell.EMPTY;
    }

    protected void fill(Cell value) {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                grid[r][c] = value;
    }

    protected void set(int[] pos, Cell value) {
        grid[pos[0]][pos[1]] = value;
    }
}
