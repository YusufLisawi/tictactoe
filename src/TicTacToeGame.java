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
        int winLength = 3;

        // Check rows and columns
        for (int i = 0; i < gridSize; i++) {
            int sumXRow = 0, sumORow = 0;
            int sumXCol = 0, sumOCol = 0;

            for (int j = 0; j < gridSize; j++) {
                // Rows
                if (grid[i][j] == 'X') {
                    sumXRow++;
                    sumORow = 0;
                } else if (grid[i][j] == 'O') {
                    sumORow++;
                    sumXRow = 0;
                } else {
                    sumXRow = sumORow = 0;
                }

                // Columns
                if (grid[j][i] == 'X') {
                    sumXCol++;
                    sumOCol = 0;
                } else if (grid[j][i] == 'O') {
                    sumOCol++;
                    sumXCol = 0;
                } else {
                    sumXCol = sumOCol = 0;
                }

                // Check for a winner in this row or column
                if (sumXRow == winLength || sumXCol == winLength) {
                    return 'X';
                }
                if (sumORow == winLength || sumOCol == winLength) {
                    return 'O';
                }
            }
        }

        // Check diagonals
        for (int i = 0; i <= gridSize - winLength; i++) {
            for (int j = 0; j <= gridSize - winLength; j++) {
                int sumXDiag1 = 0, sumODiag1 = 0;
                int sumXDiag2 = 0, sumODiag2 = 0;

                for (int k = 0; k < winLength; k++) {
                    // Diagonal from top left to bottom right
                    if (grid[i + k][j + k] == 'X') {
                        sumXDiag1++;
                        sumODiag1 = 0;
                    } else if (grid[i + k][j + k] == 'O') {
                        sumODiag1++;
                        sumXDiag1 = 0;
                    } else {
                        sumXDiag1 = sumODiag1 = 0;
                    }

                    // Diagonal from top right to bottom left
                    if (grid[i + k][j + winLength - 1 - k] == 'X') {
                        sumXDiag2++;
                        sumODiag2 = 0;
                    } else if (grid[i + k][j + winLength - 1 - k] == 'O') {
                        sumODiag2++;
                        sumXDiag2 = 0;
                    } else {
                        sumXDiag2 = sumODiag2 = 0;
                    }

                    // Check for a winner in this diagonal
                    if (sumXDiag1 == winLength || sumXDiag2 == winLength) {
                        return 'X';
                    }
                    if (sumODiag1 == winLength || sumODiag2 == winLength) {
                        return 'O';
                    }
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
