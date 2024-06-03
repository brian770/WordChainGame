package WordChain;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JTextPane chatArea;
    private JTextField userInputField;
    private JLabel timerLabel;
    private JProgressBar progressBar;
    private JTextPane meaningArea;
    private String name;

    public static void main(String[] args) {
        Client client = new Client();
        client.showGUI();
    }

    public void showGUI() {
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);

        JPanel panel = new JPanel(new BorderLayout());
        chatArea = new JTextPane();
        chatArea.setContentType("text/html");
        chatArea.setEditable(false);
        panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        userInputField = new JTextField();
        inputPanel.add(userInputField, BorderLayout.CENTER);
        JButton sendButton = new JButton("Send");
        inputPanel.add(sendButton, BorderLayout.EAST);

        sendButton.addActionListener(e -> sendMessage());
        userInputField.addActionListener(e -> sendMessage());
        panel.add(inputPanel, BorderLayout.SOUTH);

        meaningArea = new JTextPane();
        meaningArea.setContentType("text/html");
        meaningArea.setEditable(false);
        JScrollPane meaningScrollPane = new JScrollPane(meaningArea);
        meaningScrollPane.setPreferredSize(new Dimension(400, 100));

        JPanel connectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel portLabel = new JLabel("Port: ");
        JTextField portField = new JTextField(10);
        JLabel nameLabel = new JLabel("Name: ");
        JTextField nameField = new JTextField(10);
        JButton connectButton = new JButton("Connect");
        JButton readyButton = new JButton("준비");

        gbc.gridx = 0;
        gbc.gridy = 0;
        connectionPanel.add(portLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        connectionPanel.add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        connectionPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        connectionPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        connectionPanel.add(connectButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        connectionPanel.add(readyButton, gbc);

        connectButton.addActionListener(e -> {
            int port = Integer.parseInt(portField.getText());
            name = nameField.getText();
            new Thread(() -> start("localhost", port)).start();
            connectButton.setEnabled(false);
        });

        readyButton.addActionListener(e -> {
            sendReadyMessage();
            if (readyButton.getText().equals("준비")) {
                readyButton.setText("준비 완료");
            } else {
                readyButton.setText("준비");
            }
        });

        JPanel timerPanel = new JPanel(new BorderLayout());
        timerLabel = new JLabel("남은 시간: 0초", SwingConstants.CENTER);
        progressBar = new JProgressBar(0, 30);
        progressBar.setValue(30);
        timerPanel.add(timerLabel, BorderLayout.NORTH);
        timerPanel.add(progressBar, BorderLayout.SOUTH);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(connectionPanel, BorderLayout.NORTH);
        topPanel.add(timerPanel, BorderLayout.SOUTH);

        JPanel upperPanel = new JPanel(new BorderLayout());
        upperPanel.add(topPanel, BorderLayout.NORTH);
        upperPanel.add(meaningScrollPane, BorderLayout.CENTER);

        frame.add(upperPanel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public void start(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(new IncomingReader()).start();

            out.println(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = userInputField.getText();
        out.println(message);
        userInputField.setText("");
    }

    private void sendReadyMessage() {
        out.println("READY");
    }

    private class IncomingReader implements Runnable {
        @Override
        public void run() {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("남은 시간: ")) {
                        final String finalMessage = message;
                        SwingUtilities.invokeLater(() -> {
                            timerLabel.setText(finalMessage);
                            int timeLeft = Integer.parseInt(finalMessage.split(" ")[2].replace("초", ""));
                            progressBar.setValue(timeLeft);
                        });
                    } else {
                        final String finalMessage = message;
                        SwingUtilities.invokeLater(() -> {
                            String meaning = extractMeaning(finalMessage);
                            if (!meaning.isEmpty()) {
                                updateMeaningArea(meaning);
                            }
                            appendMessage(finalMessage.replaceAll("\\(뜻:.*\\)", ""));
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void appendMessage(String message) {
        try {
            HTMLDocument doc = (HTMLDocument) chatArea.getDocument();
            HTMLEditorKit editorKit = (HTMLEditorKit) chatArea.getEditorKit();

            StringBuilder htmlMessage = new StringBuilder();
            htmlMessage.append("<p>");
            htmlMessage.append(message);
            htmlMessage.append("</p>");

            editorKit.insertHTML(doc, doc.getLength(), htmlMessage.toString(), 0, 0, null);
            chatArea.setCaretPosition(doc.getLength());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateMeaningArea(String meaning) {
        SwingUtilities.invokeLater(() -> {
            meaningArea.setText(meaning);
        });
    }

    private String extractMeaning(String message) {
        StringBuilder meanings = new StringBuilder();
        Pattern pattern = Pattern.compile("입력했습니다\\.(.*)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            String extractedText = matcher.group(1).trim();
            meanings.append(extractedText.replaceAll("(\r\n|\n|\r)", "<br>"));
        }

        return meanings.toString();
    }
}