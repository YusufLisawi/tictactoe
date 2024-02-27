import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TicTacToeGUI extends JFrame implements ActionListener {
    private JButton[][] gridBtn;
    private ChatPanel chatPanel;
    private JLabel lblInfo;
    private JLabel lblGridSize;
    private JRadioButton rdoHost;
    private JRadioButton rdoPlayer;
    private JTextField txtPort;
    private JTextField txtIP;
    private JTextField txtGridSize;
    private JButton btnStart;
    private JButton btnConnect;
    private TicTacToeGame game;
    private int gridSize;
    private JPanel gridPanel;

    public TicTacToeGUI() {
        game = new TicTacToeGame(this);
        setTitle("Tic Tac Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        createGUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createGUI() {
        JPanel p = new JPanel();
        lblInfo = new JLabel("Input port number");
        p.add(lblInfo);
        add(p);

        p = new JPanel();
        ButtonGroup group = new ButtonGroup();
        rdoHost = new JRadioButton("Host (X)");
        rdoPlayer = new JRadioButton("Player (O)");
        group.add(rdoHost);
        group.add(rdoPlayer);
        rdoHost.setSelected(true);

        p.add(rdoHost);
        p.add(rdoPlayer);
        add(p);

        p = new JPanel();
        txtPort = new JTextField(5);
        txtPort.setToolTipText("Port number used to accept connections");
        txtPort.setText("9099");
        p.add(txtPort);

        lblGridSize = new JLabel("Grid Size:");
        p.add(lblGridSize);
        txtGridSize = new JTextField(2);
        txtGridSize.setText("3");
        p.add(txtGridSize);
        add(p);

        p = new JPanel();
        btnStart = new JButton("Start");
        p.add(btnStart);

        txtIP = new JTextField(10);
        txtIP.setToolTipText("Connect to host using <IP>:<port>");
        txtIP.setEnabled(false);
        txtIP.setText("127.0.0.1:9099");
        p.add(txtIP);
        btnConnect = new JButton("Connect");
        btnConnect.setEnabled(false);
        p.add(btnConnect);
        add(p);

        btnConnect.setVisible(false);
        txtIP.setVisible(false);


        gridPanel = new JPanel();
        addGridButtons(3);
        add(gridPanel);

        rdoHost.addActionListener(this);
        rdoPlayer.addActionListener(this);
        btnStart.addActionListener(this);
        btnConnect.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == rdoHost) {
            lblInfo.setText("Input port number");
            toggleField(true);
        }
        else if (e.getSource() == btnStart) {
            String regex = "^\\d{4,5}$";
            if (!Pattern.matches("^\\d{1,2}$", txtGridSize.getText())) {
                showErrorMsg("Invalid grid size");
                return;
            }
            gridSize = Integer.parseInt(txtGridSize.getText());
            if (gridSize < 3 || gridSize > 10) {
                showErrorMsg("Grid size must be between 3 and 10");
                return;
            }
            addGridButtons(gridSize);
            game.setGridSize(gridSize);

            if (!Pattern.matches(regex, txtPort.getText())) {
                showErrorMsg("Invalid port number");
                return;
            }
            int port = Integer.parseInt(txtPort.getText());
            if (port < 1024 || port > 65535) {
                showErrorMsg("Port must be between 1024 and 65535");
                return;
            }
            disableAll();
            pack();
            game.startServer(port);
        }
        else if (e.getSource() == rdoPlayer) {
            lblInfo.setText("Input host IP address and port");
            toggleField(false);
        }
        else if (e.getSource() == btnConnect) {
            String regex =
                    "^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\" +
                            "d{1,3}):(\\d{1,5})$";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(txtIP.getText());
            if (!m.matches()) {
                showErrorMsg("Invalid IP.\nValid IP is <IP>:<port>");
                return;
            }

            final String host = m.group(1);
            final int port = Integer.parseInt(m.group(2));
            disableAll();
            pack();
            game.connectToServer(host, port);
        }
    }

    public void addGridButtons(int size) {
        gridBtn = new JButton[size][size];
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(size, size));
        for (int i = 0; i < size * size; i++) {
            int r = i / size;
            int c = i % size;

            JButton btn = new JButton(" ");
            btn.setEnabled(false);
            gridBtn[r][c] = btn;
            btn.setFont(new Font("Arial", Font.PLAIN, 50));
            btn.addActionListener(e -> {
                if (game.isGameOver()) {
                    return;
                }

                if (game.getPlayer() == game.getCurrentPlayer())
                    game.sendPlayerMove(btn, r, c);
            });
            gridPanel.add(btn);
        }
        revalidate();
        repaint();
        pack();
    }

    // Method to display the dialog after the game is over
    public void displayGameOverDialog() {
        String[] options = {"Rematch", "Quit"};
        int choice = JOptionPane.showOptionDialog(
                TicTacToeGUI.this,
                 "Game Over! What would you like to do?",
                "Game Over",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        switch (choice) {
            case 0: // Rematch
                startRematch();
                break;
            case 1: // Quit
                quitGame();
                break;
            default:
                break;
        }
    }

    private void startRematch() {
        game.setCurrentPlayer(game.getCurrentPlayer() == 'X' ? 'O' : 'X');
        game.startGame();
    }

    private void quitGame() {
        game.quitGame();
    }

    public void setGridEnabled(boolean enable) {
        for (JButton[] jButtons : gridBtn) {
            for (int j = 0; j < jButtons.length; j++) {
                jButtons[j].setEnabled(enable);
            }
        }
    }

    public void updateGrid(int row, int col, char player) {
        gridBtn[row][col].setText(String.valueOf(player));
        gridBtn[row][col].setEnabled(false);
    }

    public void updateInfo(String info) {
        lblInfo.setText(info);
    }

    public JRadioButton getRdoHost() {
        return rdoHost;
    }

    public JRadioButton getRdoPlayer() {
        return rdoPlayer;
    }

    public void showErrorMsg(String msg) {
        JOptionPane.showMessageDialog(
                getContentPane(),
                msg,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void showSuccessMsg(String msg) {
        JOptionPane.showMessageDialog(
                getContentPane(),
                msg,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void disableAll() {
        rdoHost.setEnabled(false);
        txtPort.setEnabled(false);
        btnStart.setEnabled(false);
        rdoPlayer.setEnabled(false);
        txtGridSize.setEnabled(false);
        lblGridSize.setEnabled(false);
        txtIP.setEnabled(false);
        btnConnect.setEnabled(false);
    }

    // True -> Host, False -> Player
    public void toggleField(boolean bool) {
        txtIP.setEnabled(!bool);
        txtIP.setVisible(!bool);
        btnConnect.setEnabled(!bool);
        btnConnect.setVisible(!bool);
        txtPort.setEnabled(bool);
        txtPort.setVisible(bool);
        btnStart.setEnabled(bool);
        btnStart.setVisible(bool);
        txtGridSize.setEnabled(bool);
        txtGridSize.setVisible(bool);
        lblGridSize.setEnabled(bool);
        lblGridSize.setVisible(bool);
        pack();
    }
}
