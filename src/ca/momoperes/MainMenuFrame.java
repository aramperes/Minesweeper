package ca.momoperes;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * The main menu, which:
 * - Shows the total of games won and lost since startup.
 * - Allows the player to start a new game if none is in progress.
 * - Can toggle the debug mode.
 */
public class MainMenuFrame extends JFrame {

    private int gamesWonCount = 0;
    private int gamesLostCount = 0;
    private boolean debugMode = false;

    public MainMenuFrame() throws HeadlessException {
        setTitle("Minesweeper: Main Menu");
        setSize(400, 500);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setupContents();
    }

    private void setupContents() {
        // Create JPanel
        JPanel panel = new JPanel();
        setContentPane(panel);

        // Set layout
        BorderLayout borderLayout = new BorderLayout();
        panel.setLayout(borderLayout);


        // Debug bar
        JMenuBar bar = new JMenuBar();
        JMenu debugMenu = new JMenu("Debug Mode");
        JCheckBoxMenuItem checkbox = new JCheckBoxMenuItem("Enabled");
        checkbox.addActionListener(e -> {
            this.debugMode = checkbox.isSelected();
        });
        debugMenu.add(checkbox);
        bar.add(debugMenu);
        panel.add(bar, BorderLayout.PAGE_START);

        JLabel gameStatsLabel = new JLabel(formatStatsLabel());
        gameStatsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(gameStatsLabel, BorderLayout.CENTER);

        // Bottom "Game Start" group
        {
            JPanel gameStartGroup = new JPanel();
            BorderLayout gameStartLayout = new BorderLayout();
            gameStartLayout.setVgap(10);
            gameStartGroup.setLayout(gameStartLayout);


            // Set borders to panel
            Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
            gameStartGroup.setBorder(padding);

            panel.add(gameStartGroup, BorderLayout.PAGE_END);

            // Game in progress label
            final JLabel progressLabel = new JLabel("Game in Progress!!!11");
            progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
            progressLabel.setVisible(false);
            gameStartGroup.add(progressLabel, BorderLayout.PAGE_START);

            // Add button
            final JButton startButton = new JButton("Start Game!");
            startButton.setPreferredSize(new Dimension(0, 100));
            startButton.addActionListener(e -> startGame(startButton, progressLabel, gameStatsLabel));
            gameStartGroup.add(startButton, BorderLayout.PAGE_END);
        }
    }

    private String formatStatsLabel() {
        return String.format("<html>" +
                "Games won: %d" +
                "<br><br>" +
                "Games lost: %d" +
                "</html>", gamesWonCount, gamesLostCount);
    }

    private void startGame(JButton startButton, JLabel progressLabel, final JLabel gameStatsLabel) {
        startButton.setEnabled(false);
        progressLabel.setVisible(true);

        MinesweeperGameFrame game = new MinesweeperGameFrame(debugMode, hasWon -> {
            // Game has finished
            startButton.setEnabled(true);
            progressLabel.setVisible(false);

            // If the value is empty (Optional), then the game was cancelled.
            if (hasWon.isPresent()) {
                if (hasWon.get()) {
                    gamesWonCount++;
                } else {
                    gamesLostCount++;
                }
                gameStatsLabel.setText(formatStatsLabel());
            }
        });
        game.setVisible(true);
    }
}
