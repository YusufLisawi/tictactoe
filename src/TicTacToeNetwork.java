import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TicTacToeNetwork {
    private ServerSocket serverSocket;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final TicTacToeGame game;

    public TicTacToeNetwork(TicTacToeGame game) {
        this.game = game;
    }

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            game.getGui().showErrorMsg("Error starting server.");
            rollbackGUI();
        }

        new Thread(() -> {
            synchronized (this) {
            try {
                game.getGui().updateInfo("Waiting for player to connect...");
                socket = serverSocket.accept();
                initializeStreams();
                sendGridSize(game.getGridSize()); // Send grid size to the player

                game.getGui().showSuccessMsg(String.format( "Opponent connected from %s", socket.getInetAddress().getHostAddress()));

                game.setPlayer('X'); // Player is 'X' if hosting a game
                game.startGame();
            } catch (IOException e) {
                game.getGui().showErrorMsg("Error starting server.");
                game.getGui().updateInfo("Starting server failed. Try Again!");
                rollbackGUI();
            }
            }
        }).start();
    }

    public void connectToServer(String host, int port) {
        new Thread(() -> {
            synchronized (this) {
                try {
                    game.getGui().updateInfo(String.format("Connecting to %s:%d...", host, port));
                    close();
                    socket = new Socket(host, port);
                    initializeStreams();
                    int gridSize = receiveGridSize(); // Receive grid size from the server
                    game.setGridSize(gridSize);

                    game.getGui().addGridButtons(gridSize);
                    game.getGui().showSuccessMsg("Connected to host. Game started!");

                    game.setPlayer('O'); // Player is 'O' if connected to a host
                    game.startGame();
                } catch (IOException e) {
                    game.getGui().showErrorMsg(
                            String.format("Failed to connect to %s:%d", host, port)
                    );
                    game.getGui().updateInfo("Connection failed. Try Again!");
                    rollbackGUI();
                }
            }
        }).start();
    }

    private void rollbackGUI() {
        SwingUtilities.invokeLater(() -> {
            game.getGui().getRdoHost().setEnabled(true);
            game.getGui().getRdoPlayer().setEnabled(true);
            if (game.getGui().getRdoHost().isSelected()) {
                game.getGui().toggleField(true);
            }
            else if (game.getGui().getRdoPlayer().isSelected()) {
                game.getGui().toggleField(false);
            }
        });
    }

    private void initializeStreams() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    public Move receiveMove() throws IOException, ClassNotFoundException {
        Move move = (Move) in.readObject();
        return move;
    }

    public void sendMove(int row, int col) throws IOException {
        Move move = new Move(row, col);
        out.writeObject(move);
        out.flush();
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methods to handle transmission of grid size
    private void sendGridSize(int gridSize) {
        try {
            out.writeInt(gridSize);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            game.getGui().showErrorMsg("Error sending grid size.");
            System.exit(1);
        }
    }

    private int receiveGridSize() {
        try {
            return in.readInt();
        } catch (IOException e) {
            e.printStackTrace();
            game.getGui().showErrorMsg("Error receiving grid size.");
            return -1;
        }
    }
}
