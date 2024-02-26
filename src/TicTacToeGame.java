import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TicTacToeGame {

    private final TicTacToeGUI gui;
    private final TicTacToeNetwork network;
    private int gridSize;
    private char[][] grid;
    private char currentPlayer = 'X', player;
    private boolean gameOver = false;
    private int turnNo;

    public TicTacToeGame(TicTacToeGUI gui) {
        this.gui = gui;
        this.network = new TicTacToeNetwork(this);
    }

    public void startGame() {
        grid = new char[gridSize][gridSize];
        gameOver = false;
        gui.addGridButtons(gridSize);
        gui.setGridEnabled(true);
        turnNo = 1;

        if (player == currentPlayer)
            gui.updateInfo("Turn 1 - Your turn");
        else
            gui.updateInfo("Turn 1 - Opponent's turn");

        new Thread(() -> {
            try {
                while (!gameOver) {
                    if (player != currentPlayer) {
                        receiveOpponentMove();
                    }
                    Thread.sleep(1);
                }
                gui.displayGameOverDialog();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendPlayerMove(JButton btn, int row, int col) {
        try {
            network.sendMove(row, col);
            btn.setText(String.valueOf(player));
            btn.setEnabled(false);
            grid[row][col] = player;

            updatePlayer();
        }
        catch (IOException e) {
            gui.showErrorMsg("Error sending move to opponent.");
            network.close();
            System.exit(1);
        }
    }

    public void receiveOpponentMove() {
        try {
            Move move = network.receiveMove();
            int row = move.row;
            int col = move.col;
            grid[row][col] = currentPlayer;
            gui.updateGrid(row, col, currentPlayer);

            updatePlayer();
        } catch (IOException | ClassNotFoundException e) {
            gui.showErrorMsg("Error receiving move from opponent.");
            network.close();
            System.exit(1);
        }
    }

    private synchronized void updatePlayer() {
        char winner = getWinner();
        if (winner != ' ') {
            gui.updateInfo("Game over");
            JOptionPane.showMessageDialog(
                    gui.getContentPane(),
                    (winner == player) ? "You win." : "You lose.",
                    "Game over",
                    JOptionPane.INFORMATION_MESSAGE
            );
            gameOver = true;
        }
        else if (turnNo == gridSize * gridSize) {
            gui.updateInfo("Game over");
            JOptionPane.showMessageDialog(
                    gui.getContentPane(),
                    "Draw.",
                    "Game over",
                    JOptionPane.INFORMATION_MESSAGE
            );
            gameOver = true;
        }
        else {
            if (currentPlayer == 'O') currentPlayer = 'X';
            else currentPlayer = 'O';

            if (player == currentPlayer)
                gui.updateInfo(String.format("Turn %d - Your turn", turnNo));
            else
                gui.updateInfo(String.format("Turn %d - Opponent's turn", turnNo));

            turnNo++;
        }
    }

    public char getWinner() {
        for (int i = 0; i < gridSize; i++) {
            int sumXRow = 0, sumORow = 0;
            int sumXCol = 0, sumOCol = 0;
            int sumXTL = 0, sumOTL = 0;
            int sumXTR = 0, sumOTR = 0;

            for (int j = 0; j < gridSize; j++) {
                // Rows
                if (grid[i][j] == 'X') {
                    sumXRow++;
                    sumORow = 0; // Reset sum O count
                } else if (grid[i][j] == 'O') {
                    sumORow++;
                    sumXRow = 0; // Reset sum X count
                } else {
                    sumXRow = sumORow = 0; // Reset both counts
                }

                // Columns
                if (grid[j][i] == 'X') {
                    sumXCol++;
                    sumOCol = 0; // Reset sum O count
                } else if (grid[j][i] == 'O') {
                    sumOCol++;
                    sumXCol = 0; // Reset sum X count
                } else {
                    sumXCol = sumOCol = 0; // Reset both counts
                }

                // Diagonal starting from top left
                if (grid[j][j] == 'X') {
                    sumXTL++;
                    sumOTL = 0; // Reset sum O count
                } else if (grid[j][j] == 'O') {
                    sumOTL++;
                    sumXTL = 0; // Reset sum X count
                } else {
                    sumXTL = sumOTL = 0; // Reset both counts
                }

                // Diagonal starting from top right
                if (grid[j][gridSize - 1 - j] == 'X') {
                    sumXTR++;
                    sumOTR = 0; // Reset sum O count
                } else if (grid[j][gridSize - 1 - j] == 'O') {
                    sumOTR++;
                    sumXTR = 0; // Reset sum X count
                } else {
                    sumXTR = sumOTR = 0; // Reset both counts
                }

                // Check for a winner in this row, column, or diagonal
                if (sumXRow >= 3 || sumXCol >= 3 || sumXTL >= 3 || sumXTR >= 3) {
                    return 'X';
                }
                if (sumORow >= 3 || sumOCol >= 3 || sumOTL >= 3 || sumOTR >= 3) {
                    return 'O';
                }
            }
        }

        return ' ';
    }


    public TicTacToeGUI getGui() {
        return gui;
    }

    public void startServer(int port ) {
        network.startServer(port);
    }

    public void connectToServer(String host, int port) {
        network.connectToServer(host, port);
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(char player) {
        this.currentPlayer = player;
    }

    public char getPlayer() {
        return player;
    }

    public void setPlayer(char player) {
        this.player = player;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void quitGame() {
        network.close();
        System.exit(0);
    }
}
