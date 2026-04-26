package solitaire.view;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AppWindow extends JFrame {

    public final SidebarPanel sidebar;
    public final BoardPanel   board;

    public AppWindow(BiConsumer<String, Integer> onNewGame,
                     BiConsumer<Integer, Integer> onCellClick,
                     Runnable onUndo,
                     Runnable onAutoplay,
                     Runnable onRandomize,
                     Consumer<String> onModeChange,
                     Consumer<Boolean> onRecordToggle,
                     Runnable onReplay) {
        super("Peg Solitaire");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        sidebar = new SidebarPanel(onNewGame, onUndo, onAutoplay, onRandomize, onModeChange,
                                   onRecordToggle, onReplay);
        board   = new BoardPanel(onCellClick);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(board);

        add(centerPanel, BorderLayout.CENTER);
        add(sidebar, BorderLayout.EAST);

        pack();
        setMinimumSize(new Dimension(1000, 800));
        setSize(1200, 900);
        setLocationRelativeTo(null);
    }

    public void setStatus(String text, Color color) {
        sidebar.setStatus(text, color);
    }

    public void setControlsEnabled(boolean enabled) {
        sidebar.setControlsEnabled(enabled);
    }

    public void setRecordingActive(boolean active) {
        sidebar.setRecordingActive(active);
    }

    public void setAutoplayRunning(boolean running) {
        sidebar.setAutoplayRunning(running);
    }
}
