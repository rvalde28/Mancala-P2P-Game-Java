import java.io.PrintWriter;
import java.util.Scanner;

public class Timing {

    public static class Timer implements Runnable {

        private volatile boolean stopTimer = false;

        private long timerMilliSeconds =0;
        private long maxTime;


        Timer(PrintWriter out , int seconds){
            maxTime = seconds;
            out.println("TIMEOUT");
        }
        Timer(int seconds) {
          maxTime = seconds;
        }
        public String input ;
        @Override
        public void run() {
            try {
                while(!stopTimer) {

                    Thread.sleep(1000);

                    timerMilliSeconds = timerMilliSeconds+1000;
                    if((timerMilliSeconds/1000) >= maxTime){
                        stopTimer = true;
                       
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void signalStopTimer() {
            stopTimer = true;
        }

        //this method will be helpful to find the elapsed time in seconds
        public long getTotalTimeInSeconds() {
            return timerMilliSeconds/1000;
        }
        
        public boolean timeIsUp() {
            return stopTimer;
        }
        
        public long getMaxTime() {
            return maxTime;
        }

    }


}