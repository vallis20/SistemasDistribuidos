
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;

public class HiloP {

    private Timer timer;
    private TimeOutTask task;
    Progreso p, pB;
    PeerData rp;
    String instanceName;
    String filename;
    Fragmentacion frg;
    ClientI servingPeer = null;

    static final String key = "SD_2024";

    HiloP(PeerData rp, Progreso p, String instanceName, String filename) {
        this.rp = rp;
        this.p = p;
        this.instanceName = instanceName;
        this.filename = filename;
        frg = new Fragmentacion();
    }

    HiloP(PeerData rp, String instanceName, String filename) {
        this.rp = rp;
        this.p = null;
        this.instanceName = instanceName;
        this.filename = filename;
        frg = new Fragmentacion();
    }

    public void start() {

        if (p == null) {// if the progress is null, it is because it is already known beforehand that it
                        // is a small file
            getSingleFile();
            return;
        }
        timer = new Timer();
        task = new TimeOutTask();
        timer.schedule(task, 0);
    }

    public boolean probe() {// test the connection to the peer
        boolean res = false;
        try {// gets the stub
            Registry registry = LocateRegistry.getRegistry(rp.host, rp.port);
            servingPeer = (ClientI) registry.lookup("rmi://" + rp.host + ":" + rp.port + "/"
                    + "Peer" + rp.Id);
            res = servingPeer.probe();
        } catch (NotBoundException e) {
            System.out.println("\nError - Invalid peer \"" + rp + "\" entered.  Please enter valid peer");
        } catch (RemoteException e) {
            System.out.println("\nError - Invalid peer \"" + rp + "\" entered.  Please enter valid peer");
        }

        return res;
    }

    public void getSingleFile() {// is used for very small files (512KB or less).
        try {
            byte[] temp;
            Registry registry = LocateRegistry.getRegistry(rp.host, rp.port);
            servingPeer = (ClientI) registry.lookup("rmi://" + rp.host + ":" + rp.port + "/"
                    + "Peer" + rp.Id);
            temp = servingPeer.obtain(filename, 0);
            if (temp == null) {
                System.out.println("Error, temp=null");
            }
            frg.write(CryptoUtils.decrypt(key, temp), instanceName + "/" + filename);
            System.out.println("Download complete");
        } catch (NotBoundException e) {
            System.out.println("\nError - Invalid peer \"" + rp + "\" entered.  Please enter valid peer");
        } catch (RemoteException e) {
            System.out.println("\nError - RemoteException <class HiloP:start>");
        } catch (CryptoException ex) {
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
            int tries = 0;
            int clears = 0;
            try {
                System.out.println("Conectando con: " + rp);
                Registry registry = LocateRegistry.getRegistry(rp.host, rp.port);
                servingPeer = (ClientI) registry.lookup("rmi://" + rp.host + ":" + rp.port + "/" + "Peer" + rp.Id);// connects
                                                                                                                   // to
                                                                                                                   // the
                                                                                                                   // client
                                                                                                                   // that
                                                                                                                   // will
                                                                                                                   // be
                                                                                                                   // asked
                                                                                                                   // for
                                                                                                                   // the
                                                                                                                   // file
                                                                                                                   // (remote
                                                                                                                   // client)

                pB = servingPeer.getProgress(filename);// gets the progress of the remote client for the file to be
                                                       // downloaded
                while (p.getValue() < 100) {// the loop is fulfilled as long as you do not have the complete file
                    if ((i = p.getIndexPieceNull(pB)) == -1) {// obtains the index of the part to download,
                        // verifying that the remote client also has it if the result is -1 it means
                        // either that the remote client has no more parts,
                        // or that the client has finished downloading.
                        Thread.sleep(500);// wait 500ms
                        tries++;// increases the attempt count by 1
                        if (tries > 20) {// if there are already 20 attempts, the progress is cleared
                            p.clear();
                            clears++;
                            if (clears > 20) {
                                break;
                            }
                        }
                        continue;// otherwise a new loop starts
                    }
                    temp = servingPeer.obtain(filename, i); // download the part
                    if (temp == null) {// verifies that the part has been obtained
                        System.out.println("Error: temp=null; i=" + i);
                        Thread.sleep(500);// wait 500ms
                        tries++;// increases the attempt count by 1
                        if (tries > 20) {// if there are already 20 attempts, the progress is cleared
                            p.clear();
                            clears++;
                            if (clears > 20) {
                                break;
                            }
                        }
                        continue;
                    }
                    frg.write(temp, instanceName + "/" + filename, i);// write to disk the piece
                    p.setPieceValue(i, (short) 2);// update progress
                }
                stop();
            } catch (NotBoundException e) {
                System.out.println("\nError - Invalid peer \"" + rp + "\" entered.  Please enter valid peer");
            } catch (RemoteException e) {
                System.out.println("\nError - RemoteException <class HiloP:run>");
            } catch (InterruptedException ex) {
                System.out.println("\nError - InterruptedException <class HiloP:run:sleep>");
            }

        }
    }
}