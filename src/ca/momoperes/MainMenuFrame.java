package ca.momoperes;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class MainMenuFrame extends JFrame {

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

        // Set borders to panel
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        panel.setBorder(padding);

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

        // Bottom "Game Start" group
        {
            JPanel gameStartGroup = new JPanel();
            BorderLayout gameStartLayout = new BorderLayout();
            gameStartLayout.setVgap(10);
            gameStartGroup.setLayout(gameStartLayout);
            panel.add(gameStartGroup, BorderLayout.PAGE_END);

            // Game in progress label
            final JLabel progressLabel = new JLabel("Game in Progress!!!11");
            progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
            progressLabel.setVisible(false);
            gameStartGroup.add(progressLabel, BorderLayout.PAGE_START);

            // Add button
            final JButton startButton = new JButton("Start Game!");
            startButton.setPreferredSize(new Dimension(0, 100));
            startButton.addActionListener(e -> startGame(startButton, progressLabel));
            gameStartGroup.add(startButton, BorderLayout.PAGE_END);
        }
    }

    private void startGame(final JButton startButton, final JLabel progressLabel) {
        startButton.setEnabled(false);
        progressLabel.setVisible(true);

        MinesweeperGameFrame game = new MinesweeperGameFrame(debugMode, hasWon -> {
            startButton.setEnabled(true);
            progressLabel.setVisible(false);
            if (!hasWon.isPresent()) {
                System.out.println("Game cancelled");
            } else if (hasWon.get()) {
                System.out.println("We won!");
            } else {
                System.out.println("We lost!");
            }
        });
        game.setVisible(true);
    }
}
