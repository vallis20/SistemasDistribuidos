import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;



public class HiloP {
    
        private Timer timer;
        private TimeOutTask task;
        Progreso p,pB;
        PeerData rp;
        String instanceName;
        String filename;
        Fragmentacion frg;
        ClientI servingPeer=null;
        
        static final String key= "Mary has one cat";
        
        HiloP(PeerData rp,Progreso p,String instanceName, String filename){
            this.rp=rp;
            this.p=p;
            this.instanceName=instanceName;
            this.filename=filename;
            frg = new Fragmentacion();
        }
        
        HiloP(PeerData rp,String instanceName, String filename){
            this.rp=rp;
            this.p=null;
            this.instanceName=instanceName;
            this.filename=filename;
            frg = new Fragmentacion();
        }
        public void start() {
             
             if(p==null){// if the progress is null, it is because it is already known beforehand that it is a small file
                 getSingleFile();
                 return;
             }
             timer = new Timer();
             task = new TimeOutTask();
             timer.schedule(task, 0);
        }
        
        public boolean probe(){// test the connection to the peer
            boolean res=false;
            try {// gets the stub
                Registry registry = LocateRegistry.getRegistry(rp.host,rp.port);
                servingPeer =  (ClientI) registry.lookup("rmi://"+rp.host+":"+rp.port+"/"
                                + "Peer" + rp.Id);
                res=servingPeer.probe();
            } catch (NotBoundException e) {
                System.out.println("\nError: Invalid peer \""+rp+"\" entered.  Please enter valid peer");
            }catch (RemoteException e) {
                System.out.println("\nError: Invalid peer \""+rp+"\" entered.  Please enter valid peer");
            }
            
            return res;
        }
	// is used for very small files (512KB or less).
        public void getSingleFile(){
            try{
                byte[] temp;
                Registry registry = LocateRegistry.getRegistry(rp.host,rp.port);
                servingPeer =  (ClientI) registry.lookup("rmi://"+rp.host+":"+rp.port+"/"
                            + "Peer" + rp.Id);
                temp=servingPeer.obtain(filename,0);
                if(temp==null){
                    System.out.println("Error, temp=null");
                }
                frg.write(CryptoUtils.decrypt(key, temp),instanceName+"/"+filename);
                System.out.println("Download complete");
            } catch (NotBoundException e) {
                    System.out.println("\nError: Invalid peer \""+rp+"\" entered.  Please enter valid peer");
            }catch (RemoteException e) {
                    System.out.println("\nError: RemoteException <class HiloP:start>");
            }catch (CryptoException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
            }
        }
        
        public void stop() {
            timer.cancel();
            timer.purge();
        }
        class TimeOutTask extends TimerTask {

        
            @Override
            public void run() {
                byte[] temp = null;
                int i;
                int tries=0;
                int clears=0;
                try{
                    System.out.println("Connecting with: "+rp);
                    Registry registry = LocateRegistry.getRegistry(rp.host,rp.port);
		    //connects to the client that will be asked for the file (remote client)
                    servingPeer =  (ClientI) registry.lookup("rmi://"+rp.host+":"+rp.port+"/" + "Peer" + rp.Id);
		    // gets the progress of the remote client for the file to be downloaded
                    pB=servingPeer.getProgress(filename);
		    // the loop is fulfilled as long as you do not have the complete file
                    while(p.getValue()<100){
			// obtains the index of the part to download,verifying that the remote client also has it if the result is -1 it means either that the remote client has no more parts, or that the client has finished downloading.
                        if((i = p.getIndexPieceNull(pB)) == -1){
			    // wait 500ms
                            Thread.sleep(500);
			    //increases the attempt count by 1
                            tries++;
			    //if there are already 20 attempts, the progress is cleared
                            if(tries>20){
                                p.clear();
                                clears++;
                                if(clears>20){
                                    break;
                                }
                            }
			    //otherwise a new loop starts
                            continue;
                        }
			//download the part
                        temp=servingPeer.obtain(filename,i);
			//verifies that the part has been obtained
                        if(temp==null){
                            System.out.println("Error: temp=null; i="+i);
			    //wait 500ms
                            Thread.sleep(500);
			    //increases the attempt count by 1
                            tries++;
			    //if there are already 20 attempts, the progress is cleared
                            if(tries>20){
                                p.clear();
                                clears++;
                                if(clears>20){
                                    break;
                                }
                            }
                            continue;
                        }
			//write to disk the piece
                        frg.write(temp,instanceName+"/"+filename,i);
		        //update progres
                        p.setPieceValue(i,(short)2);
                    }
                    stop();
                } catch (NotBoundException e) {
                        System.out.println("\nError: Invalid peer \""+rp+"\" entered.  Please enter valid peer");
                }catch (RemoteException e) {
                        System.out.println("\nError: RemoteException <class HiloP:run>");
                } catch (InterruptedException ex) {
                    System.out.println("\nError: InterruptedException <class HiloP:run:sleep>");
                }
                    
            }
        }
    }
