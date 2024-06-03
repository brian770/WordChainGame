package WordChain;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class Server {

    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private Set<String> usedWords = new HashSet<>();
    private int currentPlayerIndex = 0;
    private int[] scores;
    private int gameTime = 180; // 게임 총 시간 (초)
    private EndToEndTimer gameTimer;
    private String lastWord = "인절미";
    private JTextArea chatArea;
    private int turnTime = 30; // 각 플레이어의 턴 시간
    private Timer turnTimer; // 현재 플레이어의 타이머
    private int readyCount = 0; // 준비 상태를 추적하기 위한 변수

    public static void main(String[] args) {
        Server server = new Server();
        server.showGUI();
    }

    public void showGUI() {
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        JPanel panel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        JLabel portLabel = new JLabel("Port: ");
        JTextField portField = new JTextField(10);
        JButton startButton = new JButton("Start Server");

        inputPanel.add(portLabel);
        inputPanel.add(portField);
        inputPanel.add(startButton);
        panel.add(inputPanel, BorderLayout.NORTH);

        startButton.addActionListener(e -> {
            int port = Integer.parseInt(portField.getText());
            new Thread(() -> start(port)).start();
            startButton.setEnabled(false);
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            log("Server started on port: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                synchronized (clients) {
                    clients.add(clientHandler);
                }
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame() {
        log("startGame: 게임 시작됨, 첫 단어: " + lastWord);
        if (clients.isEmpty()) {
            log("게임을 시작할 수 없습니다. 클라이언트가 없습니다.");
            return;
        }
        scores = new int[clients.size()];
        usedWords.add(lastWord);
        nextTurn();
        gameTimer = new EndToEndTimer();
        gameTimer.startTimer(gameTime, this::endGame);
    }

    private void nextTurn() {
        if (turnTimer != null) {
            turnTimer.cancel();
        }
        if (clients.isEmpty()) {
            endGame();
            return;
        }
        synchronized (clients) {
            if (currentPlayerIndex >= clients.size()) {
                currentPlayerIndex = 0;
            }
            if (currentPlayerIndex < clients.size()) {
                ClientHandler currentPlayer = clients.get(currentPlayerIndex);
                Set<String> lastWordFormsSet = getTwoEumBeobChikForms(lastWord);
                String lastWordForms = String.join(", ", lastWordFormsSet);
                currentPlayer.sendMessage("당신의 차례입니다. 단어를 입력하세요. 현재 단어: " + lastWord + " (" + lastWordForms + ")");
                startTurnTimer(currentPlayer);
            } else {
                log("nextTurn: 유효한 플레이어가 없습니다.");
            }
        }
    }

    private void startTurnTimer(final ClientHandler currentPlayer) {
        if (turnTimer != null) {
            turnTimer.cancel();
        }
        turnTimer = new Timer();
        turnTimer.scheduleAtFixedRate(new TimerTask() {
            int timeLeft = turnTime;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    timeLeft--;
                    currentPlayer.sendMessage("남은 시간: " + timeLeft + "초");
                } else {
                    turnTimer.cancel();
                    currentPlayer.sendMessage("시간이 초과되었습니다! 다음 플레이어의 차례입니다.");
                    synchronized (clients) {
                        currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
                    }
                    nextTurn();
                }
            }
        }, 0, 1000); // 1초마다 실행
    }

    private void endGame() {
        log("endGame() 메서드 호출됨");
        if (turnTimer != null) {
            turnTimer.cancel();
        }
        if (gameTimer != null) {
            gameTimer.stopTimer();
        }
        for (ClientHandler client : clients) {
            client.sendMessage("게임 종료!!");
            client.sendMessage("최종 점수:");
            for (int i = 0; i < clients.size(); i++) {
                client.sendMessage(clients.get(i).getClientName() + ": " + scores[i] + "점");
            }
        }
        log("게임이 종료되었습니다.");

        // 게임 종료 후 클라이언트 소켓을 닫습니다.
        for (ClientHandler client : clients) {
            try {
                client.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        clients.clear();
        readyCount = 0; // 준비 상태 초기화
    }

    private void loadWordDatabase(String filePath, Map<String, String> wordMap) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            Workbook workbook = null;
            if (filePath.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (filePath.endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            }

            if (workbook != null) {
                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    Cell wordCell = row.getCell(0);
                    Cell meaningCell = row.getCell(1);
                    if (wordCell != null && meaningCell != null) {
                        String word = wordCell.getStringCellValue().trim();
                        String meaning = meaningCell.getStringCellValue().trim();
                        wordMap.put(word, meaning);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getExcelFilePath(char firstChar) {
        switch (firstChar) {
            case 'ㄱ': return "C:/wordDB/wordDB1.xls";
            case 'ㄴ': return "C:/wordDB/wordDB8.xls";
            case 'ㄷ': return "C:/wordDB/wordDB3.xls";
            case 'ㄹ': return "C:/wordDB/wordDB8.xls";
            case 'ㅁ': return "C:/wordDB/wordDB5.xls";
            case 'ㅂ': return "C:/wordDB/wordDB6.xls";
            case 'ㅅ': return "C:/wordDB/wordDB7.xls";
            case 'ㅇ': return "C:/wordDB/wordDB8.xls";
            case 'ㅈ': return "C:/wordDB/wordDB9.xls";
            case 'ㅊ': return "C:/wordDB/wordDB10.xls";
            case 'ㅋ': return "C:/wordDB/wordDB11.xls";
            case 'ㅌ': return "C:/wordDB/wordDB12.xls";
            case 'ㅍ': return "C:/wordDB/wordDB13.xls";
            case 'ㅎ': return "C:/wordDB/wordDB14.xls";
            case 'ㄲ': return "C:/wordDB/wordDB15.xls";
            case 'ㄸ': return "C:/wordDB/wordDB16.xls";
            case 'ㅃ': return "C:/wordDB/wordDB17.xls";
            case 'ㅆ': return "C:/wordDB/wordDB18.xls";
            case 'ㅉ': return "C:/wordDB/wordDB19.xls";

            default: return null;
        }
    }

    private char getInitialConsonant(char syllable) {
        final char[] INITIAL_CONSONANTS = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
        if (syllable >= 0xAC00 && syllable <= 0xD7A3) {
            int unicode = syllable - 0xAC00;
            int initialIndex = unicode / (21 * 28);
            return INITIAL_CONSONANTS[initialIndex];
        }
        return syllable;
    }

    private Set<String> getTwoEumBeobChikForms(String word) {
        // 한글 초성, 중성, 종성 배열 선언
        final String[] CHOSUNGS = {"ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};
        final String[] JUNGSUNGS = {"ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"};
        final String[] JONGSUNGS = {"", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};

        Set<String> forms = new HashSet<>();

        if (word == null || word.isEmpty()) {
            return forms;
        }

        char lastChar = word.charAt(word.length() - 1);
        forms.add(String.valueOf(lastChar));

        if (lastChar >= '가' && lastChar <= '힣') {
            int uniBase = lastChar - 0xAC00;

            int chosungIndex = uniBase / (21 * 28);
            int jungsungIndex = (uniBase % (21 * 28)) / 28;
            int jongsungIndex = uniBase % 28;

            String chosung = CHOSUNGS[chosungIndex];
            String jungsung = JUNGSUNGS[jungsungIndex];
            String jongsung = JONGSUNGS[jongsungIndex];

            log("getTwoEumBeobChikForms: chosung - " + chosung + ", jungsung - " + jungsung + ", jongsung - " + jongsung);

            if (jongsung.equals("")) { // 종성이 없는 경우
                if (chosung.equals("ㄴ")) {
                    forms.add(makeHangul('ㅇ', jungsung.charAt(0)));
                    forms.add(makeHangul('ㄴ', jungsung.charAt(0)));
                } else if (chosung.equals("ㄹ")) {
                    forms.add(makeHangul('ㅇ', jungsung.charAt(0)));
                    forms.add(makeHangul('ㄹ', jungsung.charAt(0)));
                } else {
                    forms.add(makeHangul(chosung.charAt(0), jungsung.charAt(0)));
                }
            } else { // 종성이 있는 경우
                if (chosung.equals("ㄴ") || chosung.equals("ㄹ")) {
                    forms.add(makeHangul('ㅇ', jungsung.charAt(0), jongsung.charAt(0)));
                    forms.add(makeHangul(chosung.charAt(0), jungsung.charAt(0), jongsung.charAt(0)));
                } else {
                    forms.add(makeHangul(chosung.charAt(0), jungsung.charAt(0), jongsung.charAt(0)));
                }
            }
        }

        log("getTwoEumBeobChikForms: forms - " + forms);
        return forms;
    }

    private String makeHangul(char chosung, char jungsung) {
        int chosungIndex = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ".indexOf(chosung);
        int jungsungIndex = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ".indexOf(jungsung);
        int unicode = 0xAC00 + (chosungIndex * 21 * 28) + (jungsungIndex * 28);
        return String.valueOf((char) unicode);
    }

    private String makeHangul(char chosung, char jungsung, char jongsung) {
        int chosungIndex = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ".indexOf(chosung);
        int jungsungIndex = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ".indexOf(jungsung);
        int jongsungIndex = " ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ".indexOf(jongsung);
        int unicode = 0xAC00 + (chosungIndex * 21 * 28) + (jungsungIndex * 28) + jongsungIndex;
        return String.valueOf((char) unicode);
    }

    private boolean isValidInput(String input) {
        log("isValidInput 호출됨: " + input);
        if (input.isEmpty() || usedWords.contains(input)) {
            log("검증 실패: 입력이 비어있거나 이미 사용된 단어");
            return false;
        }

        char firstChar = getInitialConsonant(input.charAt(0));
        String filePath = getExcelFilePath(firstChar);
        if (filePath == null) {
            log("검증 실패: 파일 경로가 null");
            return false;
        }

        Map<String, String> wordMap = new HashMap<>();
        loadWordDatabase(filePath, wordMap);

        // 마지막 글자 확인
        String lastChar = lastWord.substring(lastWord.length() - 1);
        Set<String> validStartLetters = getTwoEumBeobChikForms(lastWord);
        boolean startsWithLastChar = validStartLetters.contains(String.valueOf(input.charAt(0)));

        // 단어 유효성 확인
        boolean isValid = wordMap.containsKey(input) && startsWithLastChar;

        if (!isValid) {
            log("검증 실패: " + input + " (시작 문자: " + firstChar + ", 마지막 문자: " + lastChar + ")");
            log("단어 존재 여부: " + wordMap.containsKey(input) + ", 시작 문자 일치 여부: " + startsWithLastChar);
        } else {
            log("검증 성공: " + input);
        }

        return isValid;
    }

    public synchronized void processInput(ClientHandler clientHandler, String input) {
        log("processInput 호출됨: " + input); // 디버깅 로그
        synchronized (clients) {
            if (clients.get(currentPlayerIndex) != clientHandler) {
                clientHandler.sendMessage("잘못된 입력입니다. 다른 플레이어의 차례입니다.");
                return;
            }

            if (isValidInput(input)) {
                if (turnTimer != null) {
                    turnTimer.cancel();
                }
                lastWord = input;
                usedWords.add(input);
                scores[currentPlayerIndex]++;
                String meaning = getWordMeaning(input);
                currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
                for (ClientHandler client : clients) {
                    client.sendMessage(clientHandler.getClientName() + " 님이 '" + input + "'을 입력했습니다. (뜻: " + meaning + ")");
                }
                nextTurn();
            } else {
                clientHandler.sendMessage("잘못된 입력입니다.");
            }
        }
    }

    private String getWordMeaning(String word) {
        char firstChar = getInitialConsonant(word.charAt(0));
        String filePath = getExcelFilePath(firstChar);
        Map<String, String> wordMap = new HashMap<>();
        loadWordDatabase(filePath, wordMap);
        return wordMap.get(word);
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    public class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private Server server;
        private String name;
        private boolean isReady = false; // 준비 상태를 나타내는 변수

        public ClientHandler(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("닉네임을 입력하세요: ");
                name = in.readLine();
                server.log(name + " 님이 입장하셨습니다.");
                synchronized (clients) {
                    for (ClientHandler client : server.clients) {
                        client.sendMessage(name + " 님이 입장하셨습니다.");
                    }
                }

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.equalsIgnoreCase("READY")) {
                        synchronized (clients) {
                            if (!isReady) {
                                isReady = true;
                                readyCount++;
                                sendMessage("준비 완료!");
                                if (readyCount == clients.size() && clients.size() >= 2) {
                                    startGame();
                                } else if (clients.size() < 2) {
                                    for (ClientHandler client : clients) {
                                        client.sendMessage("혼자서는 게임을 시작할 수 없습니다.");
                                    }
                                    readyCount = 0;
                                    isReady = false;
                                }
                            } else {
                                isReady = false;
                                readyCount--;
                                sendMessage("준비 취소");
                            }
                        }
                    } else {
                        server.log(name + "님의 입력: " + input); // 디버깅 로그
                        server.processInput(this, input);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clients) {
                    int leavingIndex = clients.indexOf(this);
                    clients.remove(this); // 클라이언트 목록에서 제거

                    if (isReady) {
                        readyCount--;
                    }

                    if (clients.size() < 2) {
                        endGame(); // 클라이언트가 두 명 미만일 때 게임 종료
                    } else {
                        if (currentPlayerIndex >= clients.size()) {
                            currentPlayerIndex = 0; // 현재 플레이어 인덱스가 클라이언트 수를 초과하지 않도록 조정
                        } else if (leavingIndex < currentPlayerIndex) {
                            currentPlayerIndex--; // 퇴장한 플레이어가 현재 인덱스보다 앞에 있으면 인덱스를 줄임
                        }
                    }
                }
                for (ClientHandler client : clients) {
                    client.sendMessage(name + " 님이 퇴장하셨습니다.");
                }
                server.log(name + " 님이 퇴장하셨습니다.");
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        public String getClientName() {
            return name;
        }
    }
}