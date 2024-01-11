import java.rmi.RemoteException;
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
        
        HiloV(ClientImpl c,JProgressBar pbFile ,Progreso p){
            this.c=c;
            this.pbFile=pbFile;
            this.p=p;
        }
        public void start() {
             timer = new Timer();
             task = new TimeOutTask();
	     // this is used to calculate the discharge time
             startTime = System.nanoTime();
	     // the task is sent to be called every 100ms (the window is updated)
             timer.scheduleAtFixedRate(task, 0, 100);
        }
        


        public void stop() {
        
            endTime=System.nanoTime();
            double duration = (endTime - startTime)/1000000000.0;
            DecimalFormat df = new DecimalFormat("#.##");
            System.out.println("Download Response time: "+ df.format(duration) + "s");
            JOptionPane.showMessageDialog(null, "Download Finished");
            timer.cancel();
            timer.purge();
        }
        class TimeOutTask extends TimerTask {

        
            @Override
            public void run() {
	 	    // sets the progress value in the download window
                    pbFile.setValue(p.getValue());
	 	    // progress is complete
                    if(p.getValue()==100){
			// joins the pieces together
                        c.unir(p.getFilename(),p.getPiecesLength());
			// the task is stopped
                        stop();
                    }
            }
        }
    }
