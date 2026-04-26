package solitaire.view;

import solitaire.model.board.BoardFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SidebarPanel extends JPanel {

    private static final Color BG        = Color.WHITE;
    private static final Color ACCENT    = new Color(0x3B5BDB);
    private static final Color REC_COLOR = new Color(0xC92A2A);
    private static final Font  LABEL     = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font  HEAD      = new Font("SansSerif", Font.BOLD, 13);
    private static final Font  SMALL     = new Font("SansSerif", Font.PLAIN, 12);

    private final JSpinner    sizeSpinner;
    private final ButtonGroup typeGroup    = new ButtonGroup();
    private final ButtonGroup modeGroup    = new ButtonGroup();
    private final JLabel      statusLabel;
    private final JLabel      recordingLabel;
    private final JButton     newGameButton;
    private final JButton     replayButton;
    private final JButton     autoplayButton;
    private final JButton     randomizeButton;
    private final JButton     undoButton;

    // Keep references to setup controls so we can lock them during replay
    private final java.util.List<JComponent> setupControls = new java.util.ArrayList<>();

    public SidebarPanel(BiConsumer<String, Integer> onNewGame,
                       Runnable onUndo,
                       Runnable onAutoplay,
                       Runnable onRandomize,
                       Consumer<String> onModeChange,
                       Consumer<Boolean> onRecordToggle,
                       Runnable onReplay) {

        setBackground(BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0xDEE2E6)),
            new EmptyBorder(24, 18, 24, 18)
        ));
        setPreferredSize(new Dimension(220, 0));

        // ── SETUP SECTION ─────────────────────────────────────────

        add(sectionLabel("Board Type"));
        add(vgap(8));

        for (String type : BoardFactory.TYPES) {
            JRadioButton btn = new JRadioButton(type);
            btn.setFont(LABEL);
            btn.setBackground(BG);
            btn.setActionCommand(type);
            btn.setAlignmentX(LEFT_ALIGNMENT);
            if (type.equals("English")) btn.setSelected(true);
            typeGroup.add(btn);
            add(btn);
            add(vgap(4));
            setupControls.add(btn);
        }

        add(vgap(16));

        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sizePanel.setBackground(BG);
        sizePanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sizeLabel = new JLabel("Board size ");
        sizeLabel.setFont(HEAD);
        sizeSpinner = new JSpinner(new SpinnerNumberModel(7, 5, 9, 2));
        sizeSpinner.setFont(LABEL);
        JComponent editor = sizeSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de)
            de.getTextField().setColumns(3);
        sizePanel.add(sizeLabel);
        sizePanel.add(sizeSpinner);
        add(sizePanel);
        setupControls.add(sizeSpinner);

        add(vgap(16));

        add(sectionLabel("Game Mode"));
        add(vgap(8));

        JRadioButton manualBtn = new JRadioButton("Manual");
        manualBtn.setFont(LABEL);
        manualBtn.setBackground(BG);
        manualBtn.setActionCommand("Manual");
        manualBtn.setAlignmentX(LEFT_ALIGNMENT);
        manualBtn.setSelected(true);
        manualBtn.addActionListener(e -> onModeChange.accept("Manual"));
        modeGroup.add(manualBtn);
        add(manualBtn);
        add(vgap(4));
        setupControls.add(manualBtn);

        JRadioButton autoBtn = new JRadioButton("Automated");
        autoBtn.setFont(LABEL);
        autoBtn.setBackground(BG);
        autoBtn.setActionCommand("Automated");
        autoBtn.setAlignmentX(LEFT_ALIGNMENT);
        autoBtn.addActionListener(e -> onModeChange.accept("Automated"));
        modeGroup.add(autoBtn);
        add(autoBtn);
        setupControls.add(autoBtn);

        // ── DIVIDER ───────────────────────────────────────────────

        add(vgap(20));
        add(divider());
        add(vgap(16));

        // ── ACTIONS SECTION ───────────────────────────────────────

        newGameButton = styledButton("New Game", ACCENT, Color.WHITE);
        newGameButton.addActionListener(e -> onNewGame.accept(selectedType(), selectedSize()));
        add(newGameButton);
        add(vgap(8));

        replayButton = styledButton("Replay", new Color(0x495057), Color.WHITE);
        replayButton.addActionListener(e -> onReplay.run());
        add(replayButton);
        add(vgap(8));

        randomizeButton = styledButton("Randomize", new Color(0xF76707), Color.WHITE);
        randomizeButton.addActionListener(e -> onRandomize.run());
        randomizeButton.setVisible(true);
        add(randomizeButton);

        autoplayButton = styledButton("\u25B6  Autoplay", new Color(0x2F9E44), Color.WHITE);
        autoplayButton.addActionListener(e -> onAutoplay.run());
        autoplayButton.setVisible(false);
        add(autoplayButton);

        add(vgap(8));

        undoButton = styledButton("Undo", new Color(0x868E96), Color.WHITE);
        undoButton.addActionListener(e -> onUndo.run());
        add(undoButton);

        // ── DIVIDER ───────────────────────────────────────────────

        add(vgap(20));
        add(divider());
        add(vgap(16));

        // ── RECORDING SECTION ─────────────────────────────────────

        JCheckBox recordCheck = new JCheckBox("Record game");
        recordCheck.setFont(LABEL);
        recordCheck.setBackground(BG);
        recordCheck.setAlignmentX(LEFT_ALIGNMENT);
        recordCheck.addActionListener(e -> onRecordToggle.accept(recordCheck.isSelected()));
        add(recordCheck);
        add(vgap(6));

        recordingLabel = new JLabel("\u25CF REC");
        recordingLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        recordingLabel.setForeground(REC_COLOR);
        recordingLabel.setAlignmentX(LEFT_ALIGNMENT);
        recordingLabel.setVisible(false);
        add(recordingLabel);

        // ── STATUS ────────────────────────────────────────────────

        add(vgap(20));
        add(divider());
        add(vgap(12));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(SMALL);
        statusLabel.setForeground(new Color(0x495057));
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(statusLabel);

        add(Box.createVerticalGlue());
    }

    // ── Public API ────────────────────────────────────────────────────

    public void setStatus(String text, Color color) {
        statusLabel.setText("<html><body style='width:150px'>" + text + "</body></html>");
        statusLabel.setForeground(color);
    }

    public void setMode(String mode) {
        boolean isManual = mode.equals("Manual");
        autoplayButton.setVisible(!isManual);
        randomizeButton.setVisible(isManual);
    }

    public void setButtonStates(boolean undoEnabled, boolean actionEnabled) {
        undoButton.setEnabled(undoEnabled);
        autoplayButton.setEnabled(actionEnabled);
        randomizeButton.setEnabled(actionEnabled);
    }

    /** Called by controller when continuous autoplay starts/stops. */
    public void setAutoplayRunning(boolean running) {
        autoplayButton.setText(running ? "\u25A0  Stop" : "\u25B6  Autoplay");
    }

    /** Called by controller to show/hide the REC indicator. */
    public void setRecordingActive(boolean active) {
        recordingLabel.setVisible(active);
    }

    /** Lock/unlock ALL interactive controls (used during replay). */
    public void setControlsEnabled(boolean enabled) {
        newGameButton.setEnabled(enabled);
        replayButton.setEnabled(enabled);
        undoButton.setEnabled(enabled);
        autoplayButton.setEnabled(enabled);
        randomizeButton.setEnabled(enabled);
        for (JComponent c : setupControls) c.setEnabled(enabled);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private String selectedType() {
        return typeGroup.getSelection().getActionCommand();
    }

    private int selectedSize() {
        return (Integer) sizeSpinner.getValue();
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(new Color(0x868E96));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(LABEL);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Component vgap(int h) {
        return Box.createVerticalStrut(h);
    }

    private JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(0xDEE2E6));
        return sep;
    }
}
