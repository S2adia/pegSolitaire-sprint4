package solitaire.model.board;

import solitaire.model.Cell;

import java.util.List;

public class DiamondBoard extends Board {

    private static final List<int[]> DIRS = List.of(
        new int[]{0, 1}, new int[]{0, -1},
        new int[]{1, 0}, new int[]{-1, 0}
    );

    public DiamondBoard(int size) {
        super(size);
    }

    @Override
    protected void build() {
        int mid = size / 2;
        for (int r = 0; r < size; r++) {
            int dist = Math.abs(r - mid);
            for (int c = 0; c < size; c++)
                grid[r][c] = (Math.abs(c - mid) + dist <= mid) ? Cell.PEG : Cell.EMPTY;
        }
        grid[mid][mid] = Cell.HOLE;
    }

    @Override
    public List<int[]> directions() {
        return DIRS;
    }
}
