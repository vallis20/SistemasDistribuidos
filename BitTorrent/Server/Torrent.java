
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Torrent implements Serializable,Remote{
    // Attributes for the Torrent class 
    // Name of the torrent
    private String name;
    // Length of each piece in Bytes (512KB)
    private final int pieceLength=524288; 
    // Number of pieces
    private int pieces; 
    // Total length of the file in bytes
    private long length; 
    // List of peers who have the complete file
    private ArrayList<Integer> seeders;
    // List of peers who are downloading the file
    private ArrayList<Integer> leechers; 
    
    // Serial version UID for serialization
    private static final long serialVersionUID = 1L;
    // Default constructor
    public Torrent()throws RemoteException{}

    public Torrent(String name, int pieceLength, int pieces, int length) throws RemoteException{
        this.name = name;
        this.pieces = (int)Math.ceil((double)length/pieceLength);
        this.length = length;
        this.seeders = new ArrayList<>();
        this.leechers = new ArrayList<>();
    }
    // Another constructor for creating a Torrent with only name and length
    public Torrent(String name, long length) throws RemoteException{
        this.name = name;
        this.length = length;
        this.pieces =  (int)Math.ceil((double)length/pieceLength);
        this.seeders = new ArrayList<>();
        this.leechers = new ArrayList<>();
    }
    // Method to add a seeder (peer with the complete file)
    public void addSeeder(Integer peerId)throws RemoteException{
        this.leechers.remove(peerId);//esto para eliminarlo de leecher si lo era
        this.seeders.remove(peerId);//esto es para evitar tener duplicados
        this.seeders.add(peerId);
    }
    // Method to remove a seeder
    public void dropSeeder(Integer peerId )throws RemoteException{
        this.seeders.remove(peerId);
    }
    // Method to get a copy of the list of seeders
    public ArrayList<Integer> getSeeders() throws RemoteException{
        return (ArrayList<Integer>) seeders.clone();
    }
    // Method to add a leecher (peer downloading the file)
    public void addLeecher(Integer peerId)throws RemoteException{
	//avoid duplicates
        this.leechers.remove(peerId);
        this.leechers.add(peerId);
    }
    // Method to remove a leecher
    public void dropLeecher(Integer peerId )throws RemoteException{
        this.leechers.remove(peerId);
    }
    // Method to get a copy of the list of leechers
    public ArrayList<Integer> getLeechers() throws RemoteException{
        return (ArrayList<Integer>) leechers.clone();
    }
    // Setter method to set the list of seeders
    public void setSeeders(ArrayList<Integer> seeders) throws RemoteException {
        this.seeders =seeders;
    }
    // Setter method to set the list of leechers
    public void setLeechers(ArrayList<Integer> leechers) throws RemoteException {
        this.leechers = leechers;
    }
    // Getter method to retrieve the name of the torrent
    public String getName() throws RemoteException{
        return name;
    }
    // Getter method to retrieve the piece length
    public int getPieceLength() throws RemoteException{
        return pieceLength;
    }
    // Getter method to retrieve the number of pieces
    public int getPieces() throws RemoteException{
        return pieces;
    }
     // Getter method to retrieve the total length of the file
    public long getLength() throws RemoteException{
        return length;
    }
    // Method to create a copy of the Torrent object
    public Torrent copy() throws RemoteException{
        Torrent to = new Torrent(this.name,this.length);
        to.setLeechers(this.getLeechers());
        to.setSeeders(this.getSeeders());
        return to;
    }
  
  
    
    

}
