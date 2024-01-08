/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;



public class HiloS {//esta clase sirve para que el servidor pueda eliminar los peer que no estan conectados de su lista de peers
    
        private Timer timer;
        private TimeOutTask task;
        ServerImpl s;
        long startTime;
        long endTime;
        
        HiloS(ServerImpl s){//recibe el objeto de ServerImpl
            this.s=s;
        }
        public void start() {
             timer = new Timer();
             task = new TimeOutTask();
             startTime = System.nanoTime();
             timer.scheduleAtFixedRate(task, 0, 5000);//cada 5s comprueba los peers conectados
        }
        


        public void stop() {
        
            endTime=System.nanoTime();
            double duration = (endTime - startTime)/1000000000.0;
            DecimalFormat df = new DecimalFormat("#.##");
            System.out.println("Tiempo total: "+ df.format(duration) + "s");
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
