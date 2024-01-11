import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;


//In this class the server can remove peers that are not connected from its list of peers.
public class HiloS {
    
	//Timer object used to schedule tasks at regular intervals.
        private Timer timer;
	// Object of the TimeOutTask class representing the task to perform
        private TimeOutTask task;
	// Object of the ServerImpl class
        ServerImpl s;
	// Initial time stamp
        long startTime;
	// Final time stamp
        long endTime;
        
	// Constructor that receives a ServerImpl object
        HiloS(ServerImpl s){
            this.s=s;
        }
        public void start() {
	     // Initializes the Timer object
             timer = new Timer();
	     // Initialize TimeOutTask object
             task = new TimeOutTask();
	     // Gets the current time in nanoseconds as initial timestamp
             startTime = System.nanoTime();
	     // Schedule the task to run every 5 seconds
             timer.scheduleAtFixedRate(task, 0, 5000);
        }
        


        public void stop() {
        
	    // Gets the current time in nanoseconds as the final timestamp
            endTime=System.nanoTime();
	    // Calculates the duration in seconds
            double duration = (endTime - startTime)/1000000000.0;
            DecimalFormat df = new DecimalFormat("#.##");
	    // Print total duration
            System.out.println("Total time: "+ df.format(duration) + "s");
	    // Cancel timer
            timer.cancel();
	    // Removes the pending tasks in the timer queue
            timer.purge();
        }
        class TimeOutTask extends TimerTask {
        
            @Override
            public void run() {
		    // Executes the checkPeers method of the ServerImpl object
                    s.checkPeers(); 
            }
        }
    }
