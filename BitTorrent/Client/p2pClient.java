import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;



public class p2pClient {
	public static void main(String args[]) throws Exception {
                PeerData p;
                String serverHost;
                int serverPort;
                Registry registry = null;
                String instanceName;
                ServerI IndexServer = null;
		// Verify that the number of parameters is correct.
		if (args.length != 5) {
			System.out.println("usage: p2pClient <serverHost> <serverPort> <ClientHost> <clientPort> <idPeer>\n\n");
			return;
		}
		// stores values in variables
		try {
			serverHost = args[0];
			serverPort = Integer.parseInt(args[1]);
                        p=new PeerData(Integer.parseInt(args[4]),args[2],Integer.parseInt(args[3]));
                        
		} catch (NumberFormatException e) {
			System.err.println("Argument must be and Integer");
			return;
		}
                if(serverPort==p.port){
                        System.out.println("Server's port can't be equal to Client's port \n\n");
                        return;
                }

		instanceName = "Peer" + p.Id;

		// assign security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
                
                try{	
			// search the server log
                        registry = LocateRegistry.getRegistry(serverHost,serverPort);
			// gets object from the server
                        IndexServer = (ServerI) registry.lookup("rmi://"+serverHost+":"+serverPort+"/server");
                        if(IndexServer.getPeer(p.Id)!=null){
                            System.out.println("peerId already exists. Please select another peerId");
                            System.exit(0);
                        }
		}catch (RemoteException e){
			System.out.println("\nError: Server \""+"rmi://"+serverHost+":"+serverPort+"/server"+"\" not found.");
			System.exit(0);
		}
		// creates a clientImpl object and passes it the reference to the server, and the peer data.
		ClientImpl clientImpl = new ClientImpl(IndexServer,p);
		// creates a record in the current machine with the given port
                registry = getRegistry(p.port);
                try{
		    // binds an instance of the clientImpl in the
                    registry.bind("rmi://"+p.host+":" + p.port + "/"+instanceName, clientImpl);
		// in case an instance with the same name has already been linked, this exception will be triggered
                }catch (RemoteException e) {
			System.out.println("\nError binding Peer "+ p.Id +" Please select another peerId");
			System.exit(0);
                }
		//shows the address of the instance
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Client: \n"+"IP Peer:"+p.host+"\nPort:" + p.port + "\nrunning ");
                // bucle to receive false
                while(clientImpl.run());
                
                System.out.println("Bye:D");
                try{
		    // here look for the record according to the given port
                    registry = getRegistry(p.port);
		    // remove the instance
                    registry.unbind("rmi://"+p.host+":" + p.port + "/"+instanceName);
                }catch(NotBoundException e){
                    System.out.println("Error: NotBoundException "+p);
                }
                catch(AccessException e){
                    System.out.println("Error: AccessException "+p);
                }
                catch(NullPointerException e){
                    System.out.println("Error: NullPointerException "+p);
                }
                catch(RemoteException e){
                    System.out.println("Error: RemoteException "+p);
                }
                try{
                    IndexServer.unregistryPeer(p);
                }catch(RemoteException em){
                    System.out.println("\nError: Can not connect to server\n");
                }finally{
                    System.exit(0);
                }
	}

        
private static Registry getRegistry(int port){
    Registry registry=null;
    try {
	// creates a record with the given port
        registry = LocateRegistry.createRegistry(port);
	// in case the record has already been created previously, this exception will be skipped
    }catch(ExportException e){
        try {
	    // here look for the record according to the given port
            registry = LocateRegistry.getRegistry(port);
	// any other error causes this exception
        }catch(RemoteException em){
            System.out.println("\nError: Registration was not obtained..\n");
            System.exit(0);
        }
    }catch (RemoteException ex) {
        System.out.println("\nError: The record was not created\n");
        System.exit(0);
    }
    return registry;
}
// Obtain the host but it is not reliable, so it is no longer used.
private static String getHost() {
            String host_ = "";
            String prefijo="192";
            boolean buscando=true;
            try {
                Enumeration e = NetworkInterface.getNetworkInterfaces();
                while(e.hasMoreElements() && buscando)
                {
                    NetworkInterface n = (NetworkInterface) e.nextElement();
                    Enumeration ee = n.getInetAddresses();
                    while (ee.hasMoreElements() && buscando)
                    {
                        InetAddress i = (InetAddress) ee.nextElement();
                        host_=i.getHostAddress();
                        if(host_.startsWith(prefijo))
                            buscando=false;
                    }
                }
                
            } catch (SocketException ex) {
                Logger.getLogger(p2pClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            return host_;
   }

}
