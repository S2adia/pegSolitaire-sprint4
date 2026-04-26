package solitaire.controller;

import solitaire.model.AutomatedGameMode;
import solitaire.model.GameMode;
import solitaire.model.GameRecord;
import solitaire.model.GameStatus;
import solitaire.model.ManualGameMode;
import solitaire.view.AppWindow;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GameController {

    private GameMode currentMode    = new ManualGameMode();
    private boolean  recordingEnabled = false;

    // Continuous autoplay state
    private Timer   autoplayTimer  = null;
    private boolean isAutoplaying  = false;

    // Replay state
    private Timer              replayTimer  = null;
    private List<GameRecord.Event> replayEvents = null;
    private int                replayIndex  = 0;

    private final AppWindow window;

    public GameController() {
        window = new AppWindow(
            this::onNewGame,
            this::onCellClick,
            this::onUndo,
            this::onAutoplay,
            this::onRandomize,
            this::onModeChange,
            this::onRecordToggle,
            this::onReplay
        );
        window.setStatus("Press New Game to start", new Color(0x868E96));
        window.setVisible(true);
    }

    // ── Game actions ──────────────────────────────────────────────────

    private void onNewGame(String boardType, int size) {
        stopAutoplay();
        saveIfRecordingInProgress();
        currentMode.newGame(boardType, size);
        window.board.setBoardType(boardType);
        if (recordingEnabled) {
            currentMode.setRecording(true);
            window.setRecordingActive(true);
        }
        refresh();
    }

    private void onCellClick(int row, int col) {
        currentMode.handleCellClick(row, col);
        refresh();
    }

    private void onUndo() {
        if (currentMode.undo()) refresh();
    }

    private void onModeChange(String mode) {
        stopAutoplay();

        // Preserve board state across mode switch
        solitaire.model.board.Board currentBoard =
            currentMode.hasBoard() ? currentMode.getBoard() : null;
        java.util.Deque<solitaire.model.Move> currentHistory =
            new java.util.ArrayDeque<>(currentMode.getHistory());
        GameStatus currentStatus = currentMode.getStatus();

        currentMode = mode.equals("Manual") ? new ManualGameMode() : new AutomatedGameMode();

        if (currentBoard != null) {
            currentMode.setState(currentBoard, currentHistory, currentStatus);
        }

        // Re-arm recording on new mode instance if a game is running
        if (recordingEnabled && currentMode.hasBoard()
                && currentMode.getStatus() == GameStatus.PLAYING) {
            currentMode.setRecording(true);
        }

        window.sidebar.setMode(mode);
        refresh();
    }

    /**
     * Toggles continuous autoplay: first click starts a timer that makes one
     * move every 300 ms; clicking again (or reaching game over) stops it.
     */
    private void onAutoplay() {
        if (!(currentMode instanceof AutomatedGameMode)) return;

        if (isAutoplaying) {
            stopAutoplay();
            return;
        }

        if (currentMode.getStatus() != GameStatus.PLAYING) return;

        isAutoplaying = true;
        window.setAutoplayRunning(true);

        autoplayTimer = new Timer(300, e -> {
            AutomatedGameMode auto = (AutomatedGameMode) currentMode;
            if (!auto.autoplay()) {
                stopAutoplay();
                refresh();
                return;
            }
            solitaire.model.Move lastMove = currentMode.getHistory().peek();
            if (lastMove != null) window.board.animateMove(lastMove);
            refresh();

            // Stop automatically when game ends
            if (currentMode.getStatus() != GameStatus.PLAYING) {
                stopAutoplay();
            }
        });
        autoplayTimer.start();
    }

    private void onRandomize() {
        if (currentMode instanceof ManualGameMode manual) {
            manual.randomize();
            refresh();
        }
    }

    // ── Recording ─────────────────────────────────────────────────────

    private void onRecordToggle(boolean enabled) {
        recordingEnabled = enabled;
        if (enabled && currentMode.hasBoard()
                && currentMode.getStatus() == GameStatus.PLAYING) {
            currentMode.setRecording(true);
            window.setRecordingActive(true);
        } else {
            currentMode.setRecording(false);
            window.setRecordingActive(false);
        }
    }

    private void saveRecording(GameRecord record) {
        Path dir = Path.of("recordings");
        String ts = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = record.getBoardType() + "_" + record.getBoardSize()
            + "_" + record.getGameMode() + "_" + ts + ".txt";
        try {
            record.save(dir.resolve(filename));
        } catch (IOException ex) {
            System.err.println("Failed to save recording: " + ex.getMessage());
        }
    }

    /** Save the current recording if one is in progress with events. */
    private void saveIfRecordingInProgress() {
        GameRecord rec = currentMode.getRecord();
        if (rec != null && !rec.getEvents().isEmpty()
                && currentMode.getStatus() == GameStatus.PLAYING) {
            saveRecording(rec);
        }
    }

    // ── Replay ────────────────────────────────────────────────────────

    private void onReplay() {
        JFileChooser chooser = new JFileChooser("recordings");
        chooser.setDialogTitle("Select a recording to replay");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Text recordings (*.txt)", "txt"));

        if (chooser.showOpenDialog(window) != JFileChooser.APPROVE_OPTION) return;

        try {
            startReplay(GameRecord.load(chooser.getSelectedFile().toPath()));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(window,
                "Could not load recording:\n" + ex.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startReplay(GameRecord record) {
        if (replayTimer != null) replayTimer.stop();
        stopAutoplay();

        onModeChange(record.getGameMode());
        currentMode.newGame(record.getBoardType(), record.getBoardSize());
        window.board.setBoardType(record.getBoardType());
        refresh();

        window.setControlsEnabled(false);
        window.setStatus("Replaying...", new Color(0x3B5BDB));

        replayEvents = record.getEvents();
        replayIndex  = 0;

        replayTimer = new Timer(600, e -> stepReplay());
        replayTimer.start();
    }

    private void stepReplay() {
        if (replayIndex >= replayEvents.size()) {
            replayTimer.stop();
            window.setControlsEnabled(true);
            refresh(); // show final game status
            return;
        }

        GameRecord.Event event = replayEvents.get(replayIndex++);

        if (event.type() == GameRecord.EventType.MOVE) {
            currentMode.applyMove(event.move());
            window.board.animateMove(event.move());
        } else {
            currentMode.loadBoardState(event.boardState());
        }

        refresh();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void stopAutoplay() {
        if (autoplayTimer != null) {
            autoplayTimer.stop();
            autoplayTimer = null;
        }
        if (isAutoplaying) {
            isAutoplaying = false;
            window.setAutoplayRunning(false);
        }
    }

    private void refresh() {
        if (!currentMode.hasBoard()) return;

        int[] selected = null;
        java.util.Set<String> reachable = java.util.Set.of();
        if (currentMode instanceof ManualGameMode manual) {
            selected = manual.getSelected();
            reachable = manual.reachableFromSelected();
        }

        window.board.render(currentMode.getBoard().getGrid(), selected, reachable);
        window.setStatus(statusText(), statusColor());

        boolean undoEnabled    = !currentMode.getHistory().isEmpty();
        boolean actionEnabled  = currentMode.getStatus() == GameStatus.PLAYING
                                 && !currentMode.getBoard().validMoves().isEmpty();
        window.sidebar.setButtonStates(undoEnabled, actionEnabled);

        // Auto-save when game ends while recording
        GameStatus s = currentMode.getStatus();
        if ((s == GameStatus.WON || s == GameStatus.LOST)
                && currentMode.getRecord() != null
                && !currentMode.getRecord().getEvents().isEmpty()) {
            saveRecording(currentMode.getRecord());
            currentMode.setRecording(false);
            window.setRecordingActive(false);
        }
    }

    private String statusText() {
        return switch (currentMode.getStatus()) {
            case IDLE    -> "Press New Game to start";
            case PLAYING -> "Pegs left: " + currentMode.getBoard().pegCount();
            case WON     -> "You won! One peg left";
            case LOST    -> "No moves left. Game over.";
        };
    }

    private Color statusColor() {
        return switch (currentMode.getStatus()) {
            case IDLE    -> new Color(0x868E96);
            case PLAYING -> new Color(0x495057);
            case WON     -> new Color(0x2F9E44);
            case LOST    -> new Color(0xE03131);
        };
    }
}
