package ca.momoperes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Represents a Minesweeper game.
 *
 * The game starts by generating 10 hidden mines on a 10x10 map.
 *
 * When the player clicks on a tile, one of 2 things happens:
 *   1. If there is a mine at the tile, the player loses and all mines are revealed.
 *   2. If there are no mines, one of two things happens:
 *     i. If there is at least 1 mine directly next to the tile (including diagonals),
 *        the total number of adjacent mines is displayed on the tile.
 *     ii. If there are no adjacent mines, all adjacent tiles are clicked automatically,
 *         which clears the area.
 *
 * If the player uses CTRL while clicking on a tile, it is flagged (or un-flagged).
 *
 * If the player successfully flags all 10 mines without blowing up, the player wins.
 */
public class MinesweeperGameFrame extends JFrame {
    /**
     * The X/Y dimensions of the game
     */
    private static final int GAME_SIZE = 10;
    /**
     * The amount of mines on the map.
     */
    private static final int MINE_COUNT = 10;

    /**
     * A callback consumer for when the game ends.
     */
    private final Consumer<Optional<Boolean>> gameFinishedHandler;
    /**
     * Map of all buttons.
     */
    private final JButton[] gameButtons = new JButton[GAME_SIZE * GAME_SIZE];
    /**
     * Map of all mines.
     */
    private final boolean[] mineMap = new boolean[GAME_SIZE * GAME_SIZE];
    /**
     * Map of all flags.
     */
    private final boolean[] flagMap = new boolean[GAME_SIZE * GAME_SIZE];
    /**
     * Whether the game is in debug mode.
     */
    private final boolean debugMode;
    /**
     * The amount of correctly flagged bombs.
     */
    private int correctFlagCount = 0;

    /**
     * Initializes a new game window.
     *
     * @param debugMode           whether the game is in debug mode (i.e., mines are shown)
     * @param gameFinishedHandler a callback consumer for when the game ends
     */
    public MinesweeperGameFrame(boolean debugMode, Consumer<Optional<Boolean>> gameFinishedHandler) {
        this.gameFinishedHandler = gameFinishedHandler;
        this.debugMode = debugMode;

        setTitle("Minesweeper Game");
        setSize(800, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Cancel the game when the player closes the window
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                triggerGameResult(null);
            }
        });

        setupContents();
        placeMines();
    }

    /**
     * Places the mines on the map.
     */
    private void placeMines() {
        for (int i = 0; i < MINE_COUNT; i++) {
            int minePosition = ThreadLocalRandom.current().nextInt(GAME_SIZE * GAME_SIZE);
            if (isMine(minePosition)) {
                // There is already a mine, try again.
                i--;
                continue;
            }
            mineMap[minePosition] = true;

            if (debugMode) {
                gameButtons[minePosition].setText("MINE");
            }
        }
    }

    /**
     * GUI setup.
     */
    private void setupContents() {
        JPanel gamePanel = new JPanel();
        setContentPane(gamePanel);
        GridLayout layout = new GridLayout(GAME_SIZE, GAME_SIZE);
        gamePanel.setLayout(layout);

        // Generate the button grid
        for (int buttonIndex = 0; buttonIndex < GAME_SIZE * GAME_SIZE; buttonIndex++) {
            final int finalButtonIndex = buttonIndex;

            JButton button = new JButton();
            button.setFocusable(false);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.addActionListener(e -> {
                Point position = convertIndex(finalButtonIndex);
                // Ctrl+Click = Flag
                boolean ctrlClick = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
                if (ctrlClick) {
                    if (isFlag(finalButtonIndex)) {
                        // Point is already flagged
                        button.setText("");
                        if (isMine(finalButtonIndex)) {
                            // Un-flagged a correct flag
                            correctFlagCount--;
                        }
                    } else {
                        // Set point as flagged
                        button.setText("FLAG");
                        if (isMine(finalButtonIndex)) {
                            correctFlagCount++;
                        }
                    }
                    // Toggle flag state
                    flagMap[finalButtonIndex] = !flagMap[finalButtonIndex];
                    return;
                }

                if (isFlag(finalButtonIndex)) {
                    // Point is flagged, do nothing on normal click.
                    return;
                }

                // Explode
                if (isMine(finalButtonIndex)) {
                    // Show all the mines on the map
                    for (int i = 0; i < GAME_SIZE * GAME_SIZE; i++) {
                        if (isMine(i)) {
                            gameButtons[i].setText("MINE");
                        }
                    }
                    triggerGameResult(false);
                    return;
                }

                // Count mines around
                int minesAround = countMinesAround(position);
                button.setEnabled(false);
                if (isBoardClear()) {
                            triggerGameResult(true);
                }
                if (minesAround > 0) {
                    // Show surrounding mines count
                    button.setText(String.valueOf(minesAround));
                } else {
                    // Clear area
                    for (Point point : pointsAround(position)) {
                        gameButtons[convertPoint(point)].doClick(10);
                    }
                }
            });

            this.gameButtons[buttonIndex] = button;
            gamePanel.add(button);
        }
    }

    /**
     * Counts how many mines there are around a point.
     */
    private int countMinesAround(Point position) {
        return (int) pointsAround(position)
                .stream()
                .filter(this::isMine)
                .count();
    }

    /**
     * Lists points around a point, within the bounds of the map.
     */
    private Set<Point> pointsAround(Point position) {
        Set<Point> points = new HashSet<>();
        for (int relX = -1; relX <= 1; relX++) {
            for (int relY = -1; relY <= 1; relY++) {
                if (relX == 0 && relY == 0) {
                    continue;
                }
                int x = position.x + relX;
                int y = position.y + relY;

                if (x < 0 || x >= GAME_SIZE || y < 0 || y >= GAME_SIZE) {
                    // Out of bounds
                    continue;
                }

                points.add(new Point(x, y));
            }
        }
        return points;
    }

    /**
     * Notifies the MainMenuFrame that the game is finished.
     */
    private void triggerGameResult(Boolean won) {
        if (won == null) {
            this.gameFinishedHandler.accept(Optional.empty());
        } else {
            if (won) {
                Sounds.playWinSound();
            } else {
                Sounds.playLostSound();
            }
            JOptionPane.showMessageDialog(this, won ? "You won!" : "You lost!");
            dispose();
            this.gameFinishedHandler.accept(Optional.of(won));
        }
    }

    /**
     * Checks if the given position has a mine.
     */
    private boolean isMine(int index) {
        return mineMap[index];
    }

    /**
     * Checks if the given position has a mine.
     */
    private boolean isMine(Point point) {
        return mineMap[convertPoint(point)];
    }


    /**
     * Checks if the given position has a flag.
     */
    private boolean isFlag(int index) {
        return flagMap[index];
    }

    /**
     * Checks if the given position has a flag.
     */
    private boolean isFlag(Point point) {
        return flagMap[convertPoint(point)];
    }

    /**
     * Converts an array index to a Point (x, y)
     */
    private Point convertIndex(int index) {
        return new Point(index % GAME_SIZE, index / GAME_SIZE);
    }

    /**
     * Converts a Point (x, y) to an array index
     */
    private int convertPoint(Point point) {
        return point.y * GAME_SIZE + point.x;
    }

    /**
     * Loops through the entire board and checks if each button except for the mines are disabled
     */
    private boolean isBoardClear() {
        for (int i = 0; i < this.gameButtons.length; i++) {
          if(this.gameButtons[i].isEnabled() && !this.mineMap[i]) {
            return false;
          }
        }
        return true;
    }
}
