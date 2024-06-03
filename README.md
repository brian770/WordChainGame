# WordChainGame 서버 코드 

이 코드는 서버와 여러 클라이언트 간의 단어 연결 게임(WordChainGame)을 구현하는 Java 프로그램입니다. 
서버는 클라이언트의 입력을 받아 게임을 진행하며, 각 클라이언트는 차례대로 단어를 입력합니다. 
게임의 주요 기능과 구조를 분석하면 다음과 같습니다:

## 주요 클래스 및 멤버 변수

### Server 클래스

- **멤버 변수**
  - `ServerSocket serverSocket`: 서버 소켓 객체.
  - `List<ClientHandler> clients`: 연결된 클라이언트 핸들러 목록.
  - `Set<String> usedWords`: 사용된 단어 집합.
  - `int currentPlayerIndex`: 현재 플레이어 인덱스.
  - `int[] scores`: 플레이어 점수 배열.
  - `int gameTime`: 게임 총 시간 (초).
  - `EndToEndTimer gameTimer`: 게임 종료 타이머.
  - `String lastWord`: 마지막 단어.
  - `JTextArea chatArea`: 채팅 영역.
  - `int turnTime`: 각 플레이어의 턴 시간.
  - `Timer turnTimer`: 현재 플레이어의 타이머.
  - `int readyCount`: 준비 상태를 추적하기 위한 변수.

- **메서드**
  - `showGUI()`: 서버 GUI를 표시.
  - `start(int port)`: 서버 시작.
  - `startGame()`: 게임 시작.
  - `nextTurn()`: 다음 플레이어의 턴으로 전환.
  - `startTurnTimer(ClientHandler currentPlayer)`: 현재 플레이어의 턴 타이머 시작.
  - `endGame()`: 게임 종료.
  - `loadWordDatabase(String filePath, Map<String, String> wordMap)`: 엑셀 파일에서 단어 데이터베이스 로드.
  - `getExcelFilePath(char firstChar)`: 초성에 따른 엑셀 파일 경로 반환.
  - `getInitialConsonant(char syllable)`: 한글 음절의 초성 반환.
  - `getTwoEumBeobChikForms(String word)`: 단어의 두음 법칙 변환 형태 반환.
  - `makeHangul(char chosung, char jungsung)`: 초성과 중성으로 한글 문자 생성.
  - `makeHangul(char chosung, char jungsung, char jongsung)`: 초성, 중성, 종성으로 한글 문자 생성.
  - `isValidInput(String input)`: 입력된 단어의 유효성 검사.
  - `processInput(ClientHandler clientHandler, String input)`: 클라이언트 입력 처리.
  - `getWordMeaning(String word)`: 단어의 의미 반환.
  - `log(String message)`: 로그 메시지 출력.

### ClientHandler 클래스

- **멤버 변수**
  - `Socket socket`: 클라이언트 소켓.
  - `BufferedReader in`: 클라이언트 입력 스트림.
  - `PrintWriter out`: 클라이언트 출력 스트림.
  - `Server server`: 서버 참조.
  - `String name`: 클라이언트 이름.
  - `boolean isReady`: 준비 상태 변수.

- **메서드**
  - `run()`: 클라이언트와의 통신 처리.
  - `sendMessage(String message)`: 클라이언트에게 메시지 전송.
  - `getClientName()`: 클라이언트 이름 반환.

## 게임 흐름

1. **서버 시작**:
   - `Server` 클래스의 `start(int port)` 메서드가 호출되어 서버 소켓을 생성하고 클라이언트 연결을 기다림.
   - 클라이언트가 연결되면 `ClientHandler` 객체를 생성하고 클라이언트 목록에 추가.

2. **클라이언트 연결**:
   - 각 클라이언트는 `ClientHandler`의 `run()` 메서드를 통해 서버와 통신.
   - 클라이언트가 "READY" 메시지를 보내면 준비 상태로 전환.

3. **게임 시작**:
   - 모든 클라이언트가 준비 상태가 되면 `startGame()` 메서드가 호출되어 게임 시작.
   - 첫 단어는 "인절미"로 설정되고, 각 클라이언트는 차례대로 단어를 입력.

4. **턴 관리**:
   - `nextTurn()` 메서드를 통해 각 클라이언트의 턴을 관리.
   - `startTurnTimer(ClientHandler currentPlayer)` 메서드가 호출되어 각 클라이언트의 턴 타이머 시작.
   - 턴 타이머가 만료되면 다음 플레이어로 넘어감.

5. **단어 입력 및 검증**:
   - 클라이언트가 단어를 입력하면 `processInput(ClientHandler clientHandler, String input)` 메서드가 호출되어 단어의 유효성을 검증.
   - 유효한 단어라면 점수를 추가하고 다음 플레이어로 넘어감.

6. **게임 종료**:
   - `endGame()` 메서드가 호출되어 게임을 종료하고 점수를 출력.
   - 클라이언트 소켓을 닫고 목록에서 제거.

## 특이 사항

- **엑셀 파일 로드**:
  - `loadWordDatabase` 메서드를 사용하여 엑셀 파일에서 단어와 의미를 로드.
  - 초성에 따라 적절한 엑셀 파일을 선택하는 `getExcelFilePath` 메서드가 존재.

- **두음 법칙**:
  - `getTwoEumBeobChikForms` 메서드를 통해 두음 법칙에 따라 단어를 변환.

## 요약

이 코드는 서버와 여러 클라이언트 간의 통신을 통해 단어 연결 게임을 구현합니다. 
서버는 클라이언트의 입력을 받아 단어의 유효성을 검사하고, 각 클라이언트는 차례대로 단어를 입력하며 점수를 획득합니다. 
게임이 종료되면 최종 점수가 출력됩니다.


# WordChainGame 클라이언트 코드

이 코드는 클라이언트가 서버와 통신하여 단어 연결 게임에 참여할 수 있도록 하는 Java 프로그램입니다. 
클라이언트는 GUI를 통해 서버와 통신하고, 단어를 입력하고, 게임의 진행 상태를 실시간으로 확인할 수 있습니다. 
주요 기능과 구조를 분석하면 다음과 같습니다:

## 주요 클래스 및 멤버 변수

### Client 클래스

- **멤버 변수**
  - `Socket socket`: 서버와의 통신을 위한 소켓 객체.
  - `PrintWriter out`: 서버로 메시지를 보내기 위한 출력 스트림.
  - `BufferedReader in`: 서버로부터 메시지를 받기 위한 입력 스트림.
  - `JTextPane chatArea`: 채팅 메시지를 표시하는 텍스트 영역.
  - `JTextField userInputField`: 사용자 입력 필드.
  - `JLabel timerLabel`: 타이머 라벨.
  - `JProgressBar progressBar`: 진행률 바.
  - `JTextPane meaningArea`: 단어의 의미를 표시하는 텍스트 영역.
  - `String name`: 클라이언트 이름.

- **메서드**
  - `main(String[] args)`: 클라이언트 프로그램 시작점.
  - `showGUI()`: 클라이언트 GUI를 생성하고 표시.
  - `start(String serverAddress, int port)`: 서버와의 연결 시작.
  - `sendMessage()`: 서버로 메시지를 전송.
  - `sendReadyMessage()`: 서버로 준비 상태 메시지를 전송.
  - `appendMessage(String message)`: 채팅 영역에 메시지를 추가.
  - `updateMeaningArea(String meaning)`: 단어 의미 영역을 업데이트.
  - `extractMeaning(String message)`: 메시지에서 단어의 의미를 추출.

### IncomingReader 클래스

- **메서드**
  - `run()`: 서버로부터 메시지를 읽어들이고 처리.

## 클라이언트 GUI 구성

### showGUI() 메서드

1. **프레임 설정**:
   - `JFrame`을 생성하고 기본 종료 동작과 크기를 설정.

2. **패널 구성**:
   - `chatArea`: 채팅 메시지를 HTML 형식으로 표시.
   - `userInputField`와 `sendButton`: 사용자 입력을 위한 텍스트 필드와 버튼.
   - `meaningArea`: 단어의 의미를 HTML 형식으로 표시.
   - `connectionPanel`: 서버 연결 설정을 위한 입력 필드와 버튼.
   - `timerPanel`: 타이머와 진행률 바를 포함하는 패널.

3. **이벤트 리스너**:
   - `sendButton`과 `userInputField`에 메시지 전송 이벤트를 연결.
   - `connectButton`에 서버 연결 이벤트를 연결.
   - `readyButton`에 준비 상태 전송 이벤트를 연결.

## 클라이언트와 서버 간 통신

### start() 메서드

- 서버 주소와 포트를 받아 소켓을 생성하고 입출력 스트림을 초기화.
- `IncomingReader` 스레드를 시작하여 서버로부터 메시지를 읽어들임.
- 클라이언트 이름을 서버로 전송.

### 메시지 전송 및 처리

#### sendMessage() 메서드

- `userInputField`에서 입력된 메시지를 서버로 전송하고 필드를 비움.

#### sendReadyMessage() 메서드

- "READY" 메시지를 서버로 전송.

### IncomingReader 클래스

- 서버로부터 메시지를 읽어들이고, 타이머와 메시지를 실시간으로 갱신.

#### run() 메서드

- 메시지를 읽어들이고, 타이머 메시지와 일반 메시지를 구분하여 처리.
- 타이머 메시지는 `timerLabel`과 `progressBar`를 업데이트.
- 일반 메시지는 채팅 영역에 추가하고, 단어의 의미를 추출하여 의미 영역을 업데이트.

## 메시지 처리

### appendMessage() 메서드

- HTML 형식의 메시지를 `chatArea`에 추가.

### updateMeaningArea() 메서드

- 단어의 의미를 `meaningArea`에 업데이트.

### extractMeaning() 메서드

- 메시지에서 단어의 의미를 추출하여 반환.

## 요약

이 코드는 클라이언트가 서버와 통신하여 단어 연결 게임에 참여할 수 있도록 하는 Java 프로그램입니다. 
클라이언트는 GUI를 통해 단어를 입력하고, 게임 상태를 실시간으로 확인하며, 서버로부터 받은 메시지를 처리합니다. 
클라이언트는 서버와의 연결을 관리하고, 각종 UI 요소를 통해 게임 정보를 표시합니다.
