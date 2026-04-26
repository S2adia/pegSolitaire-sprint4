package solitaire.model.board;

import solitaire.model.Cell;

import java.util.List;

public class EnglishBoard extends Board {

    private static final List<int[]> DIRS = List.of(
        new int[]{0, 1}, new int[]{0, -1},
        new int[]{1, 0}, new int[]{-1, 0}
    );

    public EnglishBoard(int size) {
        super(size);
    }

    @Override
    protected void build() {
        int arm = size / 3;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++) {
                boolean inCorner = (r < arm || r >= size - arm) && (c < arm || c >= size - arm);
                grid[r][c] = inCorner ? Cell.EMPTY : Cell.PEG;
            }
        int mid = size / 2;
        grid[mid][mid] = Cell.HOLE;
    }

    @Override
    public List<int[]> directions() {
        return DIRS;
    }
}
