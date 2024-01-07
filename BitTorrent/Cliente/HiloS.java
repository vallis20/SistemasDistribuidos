
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class HiloS {// this class allows the server to remove peers that are not connected from its
                    // list of peers.

    private Timer timer;
    private TimeOutTask task;
    ServerImpl s;
    long startTime;
    long endTime;

    HiloS(ServerImpl s) {// receives the ServerImpl object
        this.s = s;
    }

    public void start() {
        timer = new Timer();
        task = new TimeOutTask();
        startTime = System.nanoTime();
        timer.scheduleAtFixedRate(task, 0, 5000);// every 5s checks the connected peers
    }

    public void stop() {

        endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1000000000.0;
        DecimalFormat df = new DecimalFormat("#.##");
        System.out.println("Sesion total time: " + df.format(duration) + "s");
        timer.cancel();
        timer.purge();
    }

    class TimeOutTask extends TimerTask {

        @Override
        public void run() {
            s.checkPeers();
        }
    }
}