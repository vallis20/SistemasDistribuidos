
import java.rmi.*;

//interface for ClientImpl
public interface ClientI extends java.rmi.Remote {

   /**
   * @param file:  FileName
   * @param piece: The number of pieces to obtain
   */
    public byte[] obtain(String file,int piece) 
		throws RemoteException;
        
   // *Used to test the connection
    public boolean probe()
                throws RemoteException;
        
    /**
   * Get the progress object for the specific file.
   * 
   * @param filename: Name of file from which its progress is to be obtained
   */
    public Progreso getProgress(String filename) 
                throws RemoteException;
        
    //new connections and avoids connecting with those who are already connected to 
    public int getId() 
                throws RemoteException;
	
}
