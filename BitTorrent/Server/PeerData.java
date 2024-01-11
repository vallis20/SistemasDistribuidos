import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Objects;
// Serializable and Remote interfaces are implemented to allow object serialization and RMI support
public class PeerData implements Serializable,Remote{
     // Attributes to store peer information
    int Id;
    int port;
    String host;
    
    private static final long serialVersionUID = 1L;
    // Copy constructor for creating a deep copy of PeerData
    public PeerData(PeerData p)throws RemoteException{
        this.Id = p.Id;
        this.host = p.host;
        this.port = p.port;
    }
    // Constructor to initialize PeerData with specific values
    public PeerData(int Id, String host,int port)throws RemoteException{
        this.Id = Id;
        this.host = host;
        this.port = port;
    }
    // Generate a hash code based on the attributes of the PeerData object
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + this.Id;
        hash = 41 * hash + this.port;
        hash = 41 * hash + Objects.hashCode(this.host);
        return hash;
    }
    // Check if two PeerData objects are equal by comparing their attributes
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PeerData other = (PeerData) obj;
        if (this.Id != other.Id) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        return true;
    }

    
    // Generate a string representation of the PeerData object
    @Override
    public String toString(){
        return "Peer" + Id ;
    }
    
    
    

}
