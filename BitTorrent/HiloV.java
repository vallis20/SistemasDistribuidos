
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class HiloV {

    private Timer timer;
    private TimeOutTask task;
    JProgressBar pbFile;
    Progreso p;
    ClientImpl c;

    long startTime;
    long endTime;

    HiloV(ClientImpl c, JProgressBar pbFile, Progreso p) {
        this.c = c;
        this.pbFile = pbFile;
        this.p = p;
    }

    public void start() {
        // EJERCICIO
        timer = new Timer();
        task = new TimeOutTask();
        startTime = System.nanoTime();// this is used to calculate the discharge time
        timer.scheduleAtFixedRate(task, 0, 100);// the task is sent to be called every 100ms (the window is updated)
    }

    public void stop() {

        endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1000000000.0;
        DecimalFormat df = new DecimalFormat("#.##");
        System.out.println("Download Response time: " + df.format(duration) + "s");
        JOptionPane.showMessageDialog(null, "Descarga finalizada");
        timer.cancel();
        timer.purge();
    }

    class TimeOutTask extends TimerTask {

        @Override
        public void run() {
            pbFile.setValue(p.getValue());// sets the progress value in the download window
            if (p.getValue() == 100) {// progress is complete
                c.unir(p.getFilename(), p.getPiecesLength());// joins the pieces together
                stop();// the task is stopped
            }
        }
    }
}