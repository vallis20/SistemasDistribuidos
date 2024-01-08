/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;


//In this class the server can remove peers that are not connected from its list of peers.
public class HiloS {
    
        private Timer timer; //Timer object used to schedule tasks at regular intervals.
        private TimeOutTask task;// Object of the TimeOutTask class representing the task to perform
        ServerImpl s;// Object of the ServerImpl class
        long startTime;// Initial time stamp
        long endTime;// Final time stamp
        
        HiloS(ServerImpl s){// Constructor that receives a ServerImpl object
            this.s=s;
        }
        public void start() {
             timer = new Timer();// Initializes the Timer object
             task = new TimeOutTask();// Initialize TimeOutTask object
             startTime = System.nanoTime();// Gets the current time in nanoseconds as initial timestamp
             timer.scheduleAtFixedRate(task, 0, 5000);// Schedule the task to run every 5 seconds
        }
        


        public void stop() {
        
            endTime=System.nanoTime();// Gets the current time in nanoseconds as the final timestamp
            double duration = (endTime - startTime)/1000000000.0;// Calculates the duration in seconds
            DecimalFormat df = new DecimalFormat("#.##");
            System.out.println("Tiempo total: "+ df.format(duration) + "s");// Print total duration
            timer.cancel();// Cancel timer
            timer.purge();// Removes the pending tasks in the timer queue
        }
        class TimeOutTask extends TimerTask {
        
            @Override
            public void run() {
                    s.checkPeers();  // Executes the checkPeers method of the ServerImpl object
            }
        }
    }
