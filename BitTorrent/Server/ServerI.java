
import java.rmi.*;
import java.util.List;

//interface for the index server
public interface ServerI extends java.rmi.Remote {
        
        // Register peer data
        public boolean registryPeer(PeerData peer) 
		throws RemoteException; 
         // Unregister peer data
        public boolean unregistryPeer(PeerData peer) 
		throws RemoteException; 
        
        // Register a seeder for a specific file associated with a peer
	public  boolean registrySeeder(Integer peer,Torrent filename) 
		throws RemoteException; 
        // Register a leecher for a specific file associated with a peer
        public  boolean registryLeecher(Integer peer,Torrent filename) 
		throws RemoteException; 

	 // Search for a file and return a list of peer IDs that have it
	public  List<Integer> searchFile(String filename)
		throws RemoteException;
        // Get a list of seeders for a specific file
        public  List<PeerData> getSeeders(String filename)
		throws RemoteException;
        // Get a list of leechers for a specific file
        public  List<PeerData> getLeechers(String filename)
		throws RemoteException;
        
        // Get peer data for a specific peer ID
        public  PeerData getPeer(int peerId)
		throws RemoteException;
        // Get Torrent information for a specific file
        public Torrent getTorrent(String filename)
		throws RemoteException;
        // Probe method to check server status
        public String probe() throws RemoteException;
	
}
