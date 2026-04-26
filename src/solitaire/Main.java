package solitaire;

import solitaire.controller.GameController;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameController::new);
    }
}
