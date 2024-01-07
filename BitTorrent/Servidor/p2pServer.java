

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

class p2pServer {

        static int port;
        static String host;
        static Registry registry = null;
                
	static public void main(String args[]) throws Exception{
		  // Check if the correct number of command-line arguments is provided
                
                if(args.length!=2){
                    System.out.println("Usage: p2pServer <host> <port>");
                    System.exit(0);
                }
				 // Assign values from command-line arguments
                host=args[0];
                port = Integer.parseInt(args[1]);      
		// Assign a security manager if not already assigned
		if(System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}
		
                    
		// Create an instance of ServerImpl
		ServerImpl serverImpl = new ServerImpl();
		
		 // Bind to the RMI registry
		try {
                        
                        registry = LocateRegistry.createRegistry(port);
			registry.bind("rmi://"+host+":" + port + "/server", serverImpl);
		 } catch (AlreadyBoundException e) {
			 // TODO Auto-generated catch block
			 System.out.println("\nError - Server is already bound.\n");
			 System.exit(0);
		 }
		
		
		System.out.println("Server \""+"rmi://"+host+":" + port + "/server"+"\" corriendo...");
		
		//main program loop
		int num=1;
                HiloS h = new HiloS(serverImpl);// Create a thread to periodically check connected peers
                h.start(); // Start the thread
		System.out.println("Presiona 0 para salir: ");
		while(num!=0)
		{
			Scanner scan = new Scanner(System.in);
			num=scan.nextInt();
		}
		h.stop();// Stop the thread
		//unbind and exit
		registry.unbind("rmi://"+host+":" + port + "/server");
		System.exit(0);	
	}
	
}
