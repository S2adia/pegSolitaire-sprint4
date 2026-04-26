package solitaire.model;

public record Move(int[] origin, int[] jumped, int[] destination) {

    public static Move of(int r0, int c0, int rj, int cj, int rd, int cd) {
        return new Move(new int[]{r0, c0}, new int[]{rj, cj}, new int[]{rd, cd});
    }
}
