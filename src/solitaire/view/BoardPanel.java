package solitaire.view;

import solitaire.model.Cell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class BoardPanel extends JPanel {

    private static final int CELL    = 70;
    private static final int RADIUS  = 25;

    private static final Color PEG_COLOR       = Color.BLACK;
    private static final Color PEG_HOVER       = new Color(0x4C6EF5);
    private static final Color SELECTED_COLOR  = new Color(0xF59F00);
    private static final Color REACHABLE_COLOR = new Color(0x74C0FC);
    private static final Color HOLE_COLOR      = Color.WHITE;
    private static final Color BG_COLOR        = Color.WHITE;
    private static final Color BORDER_COLOR    = Color.BLACK;

    private Cell[][] grid = new Cell[0][0];
    private String boardType = "English";
    private int[] selected;
    private Set<String> highlights = Set.of();
    private final Map<String, Point> centers = new HashMap<>();
    private String hoveredKey;
    private String[] animatedPath;

    private final BiConsumer<Integer, Integer> onClick;

    public BoardPanel(BiConsumer<Integer, Integer> onClick) {
        this.onClick = onClick;
        setBackground(BG_COLOR);
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                nearest(e.getX(), e.getY()).ifPresent(key -> {
                    String[] parts = key.split(",");
                    onClick.accept(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                });
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                String prev = hoveredKey;
                hoveredKey = nearest(e.getX(), e.getY()).orElse(null);
                if (!java.util.Objects.equals(prev, hoveredKey)) repaint();
            }
        });
    }

    
        public void render(Cell[][] grid, int[] selected, Set<String> highlights) {
            this.grid       = grid;
            this.selected   = selected;
            this.highlights = highlights;
            rebuildCenters();
            repaint();
        }

        public void setBoardType(String boardType) {
            this.boardType = boardType;
        }

        public void animateMove(solitaire.model.Move move) {
            animatedPath = new String[] {
                move.origin()[0] + "," + move.origin()[1],
                move.jumped()[0] + "," + move.jumped()[1],
                move.destination()[0] + "," + move.destination()[1]
            };
            repaint();
            
            Timer timer = new Timer(200, e -> {
                animatedPath = null;
                repaint();
            });
            timer.setRepeats(false);
            timer.start();
        }


    private void rebuildCenters() {
        centers.clear();
        int rows = grid.length;
        int cols = grid.length > 0 ? grid[0].length : 0;

        switch (boardType) {
            case "English" -> rebuildEnglish(rows, cols);
            case "Diamond" -> rebuildDiamond(rows, cols);
            case "Hexagon" -> rebuildHexagon(rows, cols);
            default -> rebuildEnglish(rows, cols);
        }

        // Calculate appropriate size based on board type
        int w, h;
        if (boardType.equals("Diamond")) {
            w = cols * CELL * 2;
            h = rows * CELL + CELL * 2;
        } else if (boardType.equals("Hexagon")) {
            w = cols * CELL + CELL * 2;
            h = (int)(rows * CELL * Math.sqrt(3) / 2) + CELL * 2;

        } else {
            w = cols * CELL + CELL * 2;
            h = rows * CELL + CELL * 2;
        }
        setPreferredSize(new Dimension(w, h));
        revalidate();
    }

    private void rebuildEnglish(int rows, int cols) {
        // English board: standard grid layout, no offset
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == Cell.EMPTY) continue;
                int cx = c * CELL + CELL;
                int cy = r * CELL + CELL;
                centers.put(r + "," + c, new Point(cx, cy));
            }
        }
    }

    private void rebuildDiamond(int rows, int cols) {
        // Diamond board: each row is centered, creating a diamond shape
        int mid = rows / 2;
        
        for (int r = 0; r < rows; r++) {
            // Count active cells in this row
            int activeCells = 0;
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != Cell.EMPTY) activeCells++;
            }
            
            // Center the row horizontally
            int startX = (cols - activeCells) * CELL / 2 + CELL;
            int cellIndex = 0;
            
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == Cell.EMPTY) continue;
                int cx = startX + cellIndex * CELL;
                int cy = r * CELL + CELL;
                centers.put(r + "," + c, new Point(cx, cy));
                cellIndex++;
            }
        }
    }
    private void rebuildHexagon(int rows, int cols) {
        double hexHeight = CELL * Math.sqrt(3) / 2;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == Cell.EMPTY) continue;

                // Standard odd-row shift for a horizontal pointy-topped honeycomb
                int honeycombOffset = (r % 2 == 1) ? CELL / 2 : 0;

                // Use actual column 'c', NOT 'cellIndex'
                int cx = c * CELL + honeycombOffset + CELL;
                int cy = (int)(r * hexHeight) + CELL;

                centers.put(r + "," + c, new Point(cx, cy));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Map.Entry<String, Point> entry : centers.entrySet()) {
            String key = entry.getKey();
            Point  p   = entry.getValue();
            String[] parts = key.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            drawCell(g, r, c, p.x, p.y);
        }
    }

    private void drawCell(Graphics2D g, int r, int c, int cx, int cy) {
        Cell cell = grid[r][c];
        String key = r + "," + c;

        boolean isSelected  = selected != null && selected[0] == r && selected[1] == c;
        boolean isHighlight = highlights.contains(key);
        boolean isHovered   = key.equals(hoveredKey);
        boolean isAnimated  = animatedPath != null && java.util.Arrays.asList(animatedPath).contains(key);

        // Draw cell border based on board type
        if (boardType.equals("Hexagon")) {
            drawHexagon(g, cx, cy, RADIUS, BORDER_COLOR, false);
        } else {
            // Draw square border for English and Diamond
            int cellSize = RADIUS * 2;
            g.setColor(BORDER_COLOR);
            g.drawRect(cx - RADIUS, cy - RADIUS, cellSize, cellSize);
        }

        if (cell == Cell.HOLE) {
            // Empty hole - just white background with border
            g.setColor(HOLE_COLOR);
            if (boardType.equals("Hexagon")) {
                drawHexagon(g, cx, cy, RADIUS - 1, HOLE_COLOR, true);
            } else {
                int cellSize = RADIUS * 2;
                g.fillRect(cx - RADIUS + 1, cy - RADIUS + 1, cellSize - 1, cellSize - 1);
            }
            
            // Draw animation highlight for holes (jumped position)
            if (isAnimated) {
                g.setColor(new Color(0xFFD43B)); // Bright yellow
                g.setStroke(new BasicStroke(3));
                g.drawOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);
            }
            return;
        }

        Color color = isSelected  ? SELECTED_COLOR
                    : isHighlight ? REACHABLE_COLOR
                    : isHovered   ? PEG_HOVER
                    :               PEG_COLOR;

        // Draw peg as filled circle
        int pegRadius = RADIUS - 5;
        g.setColor(color);
        g.fillOval(cx - pegRadius, cy - pegRadius, pegRadius * 2, pegRadius * 2);
        
        // Draw animation highlight
        if (isAnimated) {
            g.setColor(new Color(0xFFD43B)); // Bright yellow
            g.setStroke(new BasicStroke(3));
            g.drawOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);
        }
    }

    private void drawHexagon(Graphics2D g, int cx, int cy, int radius, Color color, boolean fill) {
        int[] xPoints = new int[6];
        int[] yPoints = new int[6];
        
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i; // 60 degrees
            xPoints[i] = (int)(cx + radius * Math.cos(angle));
            yPoints[i] = (int)(cy + radius * Math.sin(angle));
        }
        
        g.setColor(color);
        if (fill) {
            g.fillPolygon(xPoints, yPoints, 6);
        } else {
            g.drawPolygon(xPoints, yPoints, 6);
        }
    }

    private java.util.Optional<String> nearest(int x, int y) {
        double threshold = CELL * 0.55;
        String best = null;
        double bestDist = threshold;
        for (Map.Entry<String, Point> e : centers.entrySet()) {
            double d = e.getValue().distance(x, y);
            if (d < bestDist) { bestDist = d; best = e.getKey(); }
        }
        return java.util.Optional.ofNullable(best);
    }
}
