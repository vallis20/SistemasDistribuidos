import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServerImpl extends UnicastRemoteObject implements ServerI {

	private static final long serialVersionUID = 1L;
    // ConcurrentHashMap that maps filenames to a list of integers (peerids)
	private volatile ConcurrentMap<String, Torrent> Tracker;
	private volatile ConcurrentMap<Integer,PeerData> peersId;

        
        DateFormat dateFormat;
        Calendar cal;

	public ServerImpl() throws RemoteException {
		super();
                Tracker = new ConcurrentHashMap<String, Torrent>();
                peersId = new ConcurrentHashMap<Integer,PeerData>();
                dateFormat = new SimpleDateFormat("HH:mm:ss");
                
	}
        
        @Override
	public synchronized boolean registryPeer(PeerData peer) throws RemoteException {
            PeerData tmp=peersId.get(peer.Id);
            if(tmp == null ) {
                // PeerId not registered
			peersId.put(peer.Id,peer);
                        cal = Calendar.getInstance();
                        System.out.print(dateFormat.format(cal.getTime())+": ");
                        System.out.println("Se ha añadido el peer "+peer.Id);
                        return true;
		}
            else{
                if(peer.equals(tmp) ){
                    // Same peer, return true
                    return true;
                }else{
                     // Different peer, attempt connection with the old peer
                    Registry registry = LocateRegistry.getRegistry(peer.host,peer.port);
                    try{
                        ClientI peerService =  (ClientI) registry.lookup("rmi://"+peer.host+":"+peer.port+"/" + "Peer" + peer.Id);
                        peerService.probe();
                    }catch(NotBoundException e){
                        // If there was a problem finding the old peer, replace it with the new one
                        peersId.put(peer.Id,peer);
                        return true;
                    }
                        // Connected with the old peer, return false (don't add the new peer)
                        return false;
                }
            }
        }
        
        public synchronized boolean unregistryPeer(PeerData peer) throws RemoteException {
            
           
            Registry registry = LocateRegistry.getRegistry(peer.host,peer.port);
            try{
                ClientI peerService =  (ClientI) registry.lookup("rmi://"+peer.host+":"+peer.port+"/" + "Peer" + peer.Id);
                peerService.probe();// Verify if the stub still exists
                return false;// If successful, return false (avoid removing the peer, preventing clients from removing others)
            }catch(NotBoundException e){
                 // If there was a problem finding the peer, remove it from the list
                peersId.remove(peer.Id, peer);
                cal = Calendar.getInstance();
                        System.out.print(dateFormat.format(cal.getTime())+": ");
                System.out.println("Se ha eliminado el peer "+peer.Id);
                return true;
            }
            
        }
        
        
        public synchronized void checkPeers(){
            
            Iterator<Map.Entry<Integer, PeerData>> it = peersId.entrySet().iterator();//se usa un iterador para poder eliminar los peers de la lista
            while(it.hasNext()) {
                Map.Entry<Integer, PeerData> entry = it.next();
                Integer key = entry.getKey();
                PeerData peer = entry.getValue();
                try{
                    Registry registry = LocateRegistry.getRegistry(peer.host,peer.port);
                    ClientI peerService =  (ClientI) registry.lookup("rmi://"+peer.host+":"+peer.port+"/" + "Peer" + peer.Id);
                    peerService.probe();
                }catch(NotBoundException e){
                     // If there was a problem finding the peer, remove it from the list
                    peersId.remove(key);
                    cal = Calendar.getInstance();
                    System.out.print(dateFormat.format(cal.getTime())+": ");
                    System.out.println("Se ha eliminado el peer "+key);
                    
                }catch(RemoteException e){
                   // If there was a problem finding the peer, remove it from the list
                    peersId.remove(key);
                    cal = Calendar.getInstance();
                    System.out.print(dateFormat.format(cal.getTime())+": ");
                    System.out.println("Se ha eliminado el peer "+key);
                }
            }
        }
        
	@Override
	public synchronized boolean registrySeeder(Integer peer,Torrent filename) throws RemoteException {
		// Add filenames and peerid to the registry (assign by returning peerids to clients)
	 
		Torrent tmp=null; //= new Torrent();
		 // Check if the file is already in the registry
		tmp = Tracker.get(filename.getName());
		
                if(tmp == null ) {
                     // File not found, add it to the registry
                        Tracker.put(filename.getName(),filename );
                        cal = Calendar.getInstance();
                        System.out.print(dateFormat.format(cal.getTime())+": ");
                        System.out.println("S["+peer+"] ha añadido el archivo: "+filename.getName());
                }
                else
                {
                   // File already exists, check if the peer is in the list
                    for(Integer id :  tmp.getSeeders()){
                            if(Objects.equals(id, peer)) {
                                // The peer is already associated with the file, return true
                                    return true;
                            }
                    }
                    // The file exists, but the peer is not in the list, add it
                    tmp.addSeeder(peer);
                    Tracker.put(filename.getName(), tmp);
                    cal = Calendar.getInstance();
                    System.out.print(dateFormat.format(cal.getTime())+": ");
                    System.out.println("S["+peer+"] ha actualizado el archivo: "+filename.getName());

                }
                return true;
	}
        
        public synchronized boolean registryLeecher(Integer peer,Torrent filename) throws RemoteException {
		// Add filenames and peerid to the registry (assign by returning peerids to clients)
	 
		Torrent tmp=null; 
		// Check if the file is already in the registry
		tmp = Tracker.get(filename.getName());
                if(tmp == null ) {
                    // File not found, add it to the registry
                        Tracker.put(filename.getName(),filename );
                        cal = Calendar.getInstance();
                        System.out.print(dateFormat.format(cal.getTime())+": ");
                        System.out.println("L["+peer+"] ha añadido el archivo: "+filename.getName());
                }
                else {
                    // File already exists, check if the peer is in the list
                    for(Integer id :  tmp.getLeechers()){
                            if(Objects.equals(id, peer))  
                             // The peer is already associated with the file, return true
                            {
                                    return true;
                            }
                    }
                    // The file exists, but the peer is not in the list, add it
                    tmp.addLeecher(peer);
                    Tracker.put(filename.getName(), tmp);
                    cal = Calendar.getInstance();
                    System.out.print(dateFormat.format(cal.getTime())+": ");
                    System.out.println("L["+peer+"] ha sido añadido al archivo: "+filename.getName());

                }
		return true;
	}
	
	 // method for clients to search the index for a file
        
	@Override
	public List<Integer> searchFile(String filename) throws RemoteException {
		// search the data structure for the file names.  return list of peers
		Torrent torrent = null;
		torrent = Tracker.get(filename);
                if(torrent ==null){
                        return null;
                }
                else{
                        return torrent.getSeeders();
                }
	}
        
        @Override
	public List<PeerData> getSeeders(String filename) throws RemoteException {
		// search the data structure for the file names.  return list of peers
		Torrent torrent = null;
                ArrayList<PeerData> peers=null;
		torrent = Tracker.get(filename);
                if(torrent ==null){
                        return null;
                }
                else{
                    peers = new ArrayList<PeerData>();
                        
                        Iterator it = torrent.getSeeders().iterator();
                        while(it.hasNext()) {
                            Integer i = (Integer)it.next();
                            PeerData p = peersId.get(i);
                            if(p==null)
                                torrent.dropSeeder(i);
                            else{
                                peers.add(p);
                            }
                        }
                }
                return peers;
	}
        
        @Override
	public List<PeerData> getLeechers(String filename) throws RemoteException {
		// search the data structure for the file names.  return list of peers
		Torrent torrent = null;
                ArrayList<PeerData> peers = null;
		torrent = Tracker.get(filename);
                if(torrent ==null){
                        return null;
                }
                else{
                    peers = new ArrayList<PeerData>();
                        for(Integer i: torrent.getLeechers())
                        {
                            PeerData p = peersId.get(i);
                            if(p==null)
                                torrent.dropLeecher(i);
                            else{
                                peers.add(p);
                            }
                        }
                }
                return peers;
	}
	
        public PeerData getPeer(int peerId) throws RemoteException{
             // Retrieve and return the details of a peer by its ID
            PeerData tmp = peersId.get(peerId);
            if(tmp==null)return null;
            // Return a copy of the peer
            return new PeerData(tmp); 
        }
        
        public Torrent getTorrent(String filename) {
            // Retrieve and return the details of a torrent by its filename
            Torrent tmp = null;
            tmp = Tracker.get(filename);
            return tmp;
        }
          // Simple method to check if the server is reachable
        public String probe() throws RemoteException{return "ok";}
	
}
