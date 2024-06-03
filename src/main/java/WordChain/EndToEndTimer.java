package WordChain;

import java.util.Timer;
import java.util.TimerTask;

public class EndToEndTimer {
    private Timer timer;
    private int secondsLeft;
    private Runnable onEnd;

    public void startTimer(int seconds, Runnable onEnd) {
        this.onEnd = onEnd;
        this.secondsLeft = seconds;
        this.timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (secondsLeft > 0) {
                    secondsLeft--;
                } else {
                    timer.cancel();
                    onEnd.run();
                }
            }
        }, 0, 1000);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }
}