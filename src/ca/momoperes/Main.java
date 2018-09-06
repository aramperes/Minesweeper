package ca.momoperes;

import javax.swing.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Sets the look of the GUI to the system's defaults, instead of Swing.
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        // Shows the main menu.
        MainMenuFrame mainMenu = new MainMenuFrame();
        mainMenu.setVisible(true);
    }
}
