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

public class MinesweeperGameFrame extends JFrame {
    private static final int GAME_SIZE = 8;
    private static final int MINE_COUNT = 10;

    private final Consumer<Optional<Boolean>> gameFinishedHandler;
    private final JButton[] gameButtons = new JButton[GAME_SIZE * GAME_SIZE];
    private final boolean[] mineMap = new boolean[GAME_SIZE * GAME_SIZE];
    private final boolean[] flagMap = new boolean[GAME_SIZE * GAME_SIZE];
    private final boolean debugMode;
    private int correctFlagCount = 0;

    public MinesweeperGameFrame(boolean debugMode, Consumer<Optional<Boolean>> gameFinishedHandler) {
        this.gameFinishedHandler = gameFinishedHandler;
        this.debugMode = debugMode;

        setTitle("Minesweeper Game");
        setSize(800, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                triggerGameResult(null);
            }
        });

        setupContents();
        placeMines();

        System.out.println(convertPoint(new Point(0, 1)));
    }

    private void placeMines() {
        for (int i = 0; i < MINE_COUNT; i++) {
            int minePosition = ThreadLocalRandom.current().nextInt(GAME_SIZE * GAME_SIZE);
            if (mineMap[minePosition]) {
                i--;
                continue;
            }
            mineMap[minePosition] = true;

            if (debugMode) {
                gameButtons[minePosition].setText("MINE");
            }
        }
    }

    private void setupContents() {
        JPanel gamePanel = new JPanel();
        setContentPane(gamePanel);
        GridLayout layout = new GridLayout(GAME_SIZE, GAME_SIZE);
        gamePanel.setLayout(layout);

        for (int buttonIndex = 0; buttonIndex < GAME_SIZE * GAME_SIZE; buttonIndex++) {
            final int finalButtonIndex = buttonIndex;

            JButton button = new JButton();
            button.addActionListener(e -> {
                Point position = convertIndex(finalButtonIndex);
                boolean ctrlClick = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
                if (ctrlClick) {
                    if (flagMap[finalButtonIndex]) {
                        button.setText("");
                        if (mineMap[finalButtonIndex]) {
                            correctFlagCount--;
                        }
                    } else {
                        button.setText("FLAG");
                        if (mineMap[finalButtonIndex]) {
                            correctFlagCount++;
                        }
                        if (correctFlagCount == MINE_COUNT) {
                            triggerGameResult(true);
                            dispose();
                        }
                    }
                    flagMap[finalButtonIndex] = !flagMap[finalButtonIndex];
                    return;
                }

                // Explode
                if (mineMap[finalButtonIndex]) {
                    for (int i = 0; i < GAME_SIZE * GAME_SIZE; i++) {
                        if (mineMap[i]) {
                            gameButtons[i].setText("MINE");
                        }
                    }
                    triggerGameResult(false);
                    dispose();
                    return;
                }

                // count mines around
                int minesAround = countMinesAround(position);
                button.setEnabled(false);
                if (minesAround > 0) {
                    button.setText(String.valueOf(minesAround));
                } else {
                    for (Point point : pointsAround(position)) {
                        gameButtons[convertPoint(point)].doClick(10);
                    }
                }
            });

            this.gameButtons[buttonIndex] = button;
            gamePanel.add(button);
        }
    }

    private int countMinesAround(Point position) {
        return (int) pointsAround(position)
                .stream()
                .filter(point -> mineMap[convertPoint(point)])
                .count();
    }

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
                    continue;
                }

                points.add(new Point(x, y));
            }
        }
        return points;
    }

    private void triggerGameResult(Boolean won) {
        if (won == null) {
            this.gameFinishedHandler.accept(Optional.empty());
        } else {
            JOptionPane.showMessageDialog(this, won ? "You won!" : "You lost!");
            this.gameFinishedHandler.accept(Optional.of(won));
        }
    }

    private Point convertIndex(int index) {
        return new Point(index % GAME_SIZE, index / GAME_SIZE);
    }

    private int convertPoint(Point point) {
        return point.y * GAME_SIZE + point.x;
    }
}
