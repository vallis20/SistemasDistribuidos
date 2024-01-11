
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;



//Here is the full functionality of the p2p client
public class ClientImpl extends UnicastRemoteObject implements ClientI  {

	// peer identifier
	
        final int PIECE_LENGTH=524288-16;// the length of the parts = 512KB - 16bytes for encryption
        static final String key= "Mary has one cat";

        ServerI IndexServer; // Connection with the TrackerServer
        String instanceName; // Instance's name
        String dirname; // Share Directory's name
        File dir; // Is used to read files from the directory
        
        int inConnections;// number of input connections
        int outConnections;// number of output connections
        
        PeerData p;// the peer data
        final Fragmentacion frg;// Used to perform read, write and fragment operations with the files
        
        ConcurrentMap<String,Progreso> progreso; // with this, the peers can communicate with each other and know who has which file
   /**
     *
     * @param IndexServer It is the connection to the server
     * @param p           These are the peer data
     * @throws RemoteException
     */
    public ClientImpl(ServerI IndexServer,PeerData p) throws RemoteException {

                super();
                progreso = new ConcurrentHashMap<String,Progreso>();
                this.IndexServer = IndexServer;
                this.p = p;
                instanceName = "Peer" + p.Id;// is the name of the instance, which corresponds to the shared folder
                dirname = instanceName;  
                dir = new File(dirname);// the name of the directory. It was handled with another variable in case in the future the address of the directory was changed.
		if (!dir.exists()) {// if the directory does not exist, it is created
			System.out.println("Creating new shared directory");
			dir.mkdir();
		}
                frg=new Fragmentacion();
	}
        
    // the main method
    public boolean run(){
            // main UI loop
		register(dir);
		int choice=0;// here you save the chosen option
		String s;// is used to read the option
		Scanner scan = new Scanner(System.in);// is used to read the option
		InputStreamReader stream = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(stream);// is used to read the file name
		boolean loop = true;
		String filename;
		
                
                try {
                    System.out.println("--------"+p+" Menú----------");
                    System.out.println("1 - Search for file");
                    System.out.println("2 - Download file");
                    System.out.println("3 - List files in shared directory");
                    System.out.println("4 - Exit");
                    
                    System.out.print("\n\n>");

                    s = scan.nextLine();
                    try { choice = Integer.parseInt(s.trim()); }
                    catch(NumberFormatException e) {
                    }

                    switch (choice) {


                        case 1:
                            System.out.print("Enter filename: ");
                            filename = in.readLine();
                            System.out.print("\n");
                            search(filename);
                            break;

                        case 2:
                            System.out.print("Enter filename: ");
                            filename = in.readLine();
                            getFile(filename);
                            
                            break;

                        case 3:
                            list();
                            break;

                        case 4:
                            
                            loop = false;
                            break;

                        
                        default:
                            System.out.println("\nPlease enter a valid option\n");
                            break;
                    }

                }
                catch(IOException ex) {
                    Logger.getLogger(ClientImpl.class.getName()).log(Level.SEVERE, null, ex);
                }

                return loop;
        }
	// is a remote function, it returns an array of bytes
        @Override
	public byte[] obtain(String file, int piece) {
		// check the limit of connections, since 15 is the maximum.
		if(inConnections<15)inConnections++;
                else return null;
                
                byte[] bytes = null;
                
		String pathfile = instanceName + "/" + file+"."+piece+".bin";

		
		File readfile = new File(pathfile);
		// The fragment does not exist
		if (!readfile.exists()) { 
                    String pathfile2frag=instanceName + "/" + file; 
		    // search for the complete file
                    File file2frag = new File(pathfile2frag); 
		    // does not exist returns null
                    if (!file2frag.exists()) {
			//if it does not exist, returns null
                        inConnections--;
                        return null;
                    }
		    // means that it is the first piece
                    if(piece==0){
                        int size = (int) file2frag.length();
			// File is smaller than 512KB
                        if(size<=PIECE_LENGTH){
			    // If yes, it reads the file and returns it
                            bytes = frg.read(file2frag);
                            inConnections--;
                            try{
                                bytes = CryptoUtils.encrypt(key, bytes);
                            }catch (CryptoException ex) {
                                System.out.println(ex.getMessage());
                                ex.printStackTrace();
                            }
                            return bytes;
                        }
                    }
		    // in case the file is larger than 512KB, it fragments it
                    frg.fragmentar(pathfile2frag,instanceName+"/",PIECE_LENGTH);
		}
		// Read the fragment to return it
                bytes = frg.read(readfile); 
		inConnections--;
		 //returns the bytes
		return bytes; 

	}

	//Method for clients to list files currently in their shared director
	// shows the complete files being shared
        public void list() {
		File[] sharedfiles = dir.listFiles();
		System.out.println("\n\nFiles in shared directory: ");
                for (int i = 0; i < sharedfiles.length; i++) {
                    if(! sharedfiles[i].getName().endsWith(".bin")){
                        System.out.println(sharedfiles[i].getName());
                    }
                    
		}
		System.out.print("\n\n");

	}

	//Method for clients to list files currently in their shared directory
	private void search(String filename)
			throws RemoteException {
		//Count the responsive time 
		double startTime=System.nanoTime();
		double endTime;
                
		try {
			// gets a list of peer IDs
			List<Integer> peers =  IndexServer.searchFile(filename); 

			// No ones have the file
			if (peers == null) {
				System.out.println("\n\nThere are no peers with the file "+ filename + "\n\n");
				return;
			}

			// 1 or more peers have the file
			System.out.print("The peers ");
			// shows the id's of each peer
                        for(Integer pId : peers){
                            System.out.println(""+pId + ",");
                        }
                        
			System.out.print(" they have the file: " + filename+ "\n");

		} finally {
			endTime=System.nanoTime();
		}
		
                double duration = (endTime - startTime)/1000000000.0;
                DecimalFormat df = new DecimalFormat("#.##");
                System.out.println("Search time: "+ df.format(duration) + "s");
		
	}

	// records all files in the directory
	private void register(File dir){ 

		// go through shared directory and register filenames with index serve
		File[] sharedfiles = dir.listFiles();
                int totalSF=0;
                Torrent tmp;
		System.out.println(dir.getPath());
		// no files
		if (sharedfiles.length == 0) { 
                    System.out.println("This peer has not files");
                    return;
		}
                
                try{
		    // the peer is registered in case for some reason its registration was lost on the server side.
                    IndexServer.registryPeer(p); 
                }catch(RemoteException em){
                    System.out.println("\nError: Could not connect to the server.\nWill try again in: 2s");
                    try {
                        Thread.sleep(5000);
			// basically makes a loop that ends when it gets a response from the server.
                        register(dir);
                        return;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                try{
                     // log all files
                    for (int i = 0; i < sharedfiles.length; i++) {
			// makes a filter to avoid recording fragments
                        if(! sharedfiles[i].getName().endsWith(".bin")){ 
			    // create a new torrent file
                            tmp = new Torrent(sharedfiles[i].getName(),sharedfiles[i].length()); 
			    // adds himself as a seeder 
                            tmp.addSeeder(p.Id); 
			     // registers it on the server
                            IndexServer.registrySeeder(p.Id,tmp);
			     //Create a progress object
                            Progreso prg = new Progreso(tmp.getName(),tmp.getPieces());
			    // because it is your file, marks progress as complete
                            prg.setFull(); 
			     // adds the object to its progress list
                            progreso.put(sharedfiles[i].getName(),prg);
			     // adds 1 to the counter of registered files
                            totalSF++;
                        }
                    }
                }catch(RemoteException e){
                    System.out.println("Filed to connect to server");
                    return;
                }
                System.out.println("Registred files: " + totalSF);

	}

        
	// Method to set up connection with peer, call obtain() to get file, write file to local shared directory and register file with Index Server
        
        public Progreso getProgress(String filename){
	    // return the progress related to the indicated file
            return progreso.get(filename); 
        }
        
        // this is the main function that is in charge of obtaining the files
	private void getFile(String filename)
			throws FileNotFoundException, IOException {

               // get torrent data
                List<PeerData> seeders=null;
                List<PeerData> leechers=null;
	        // this is to know the Id's of the peers you are already connected to (moreover,it allows 4 connections per file)
                int[] connection = new int[4];
		// is used to know the number of connections
                int k=0; 
		// obtains the information related to the file
                Torrent to= IndexServer.getTorrent(filename); 
		// Cannot find the file
                if(to==null){ 
                    System.out.println("File not found");
                    return;
                }
		
                
                
                // gets the list of seeders for the file
                seeders = IndexServer.getSeeders(filename);
                if(seeders==null){
                    System.out.println("There are not enough seeders");
                    return;
                }
		// removes the peer that wants to get the file from the list to avoid connecting to itself
                seeders.remove(p);
                if(seeders.size()<1){
                    System.out.println("There are not enough seeders");
                    return;
                }
                // creates a new progress for the file
                Progreso prg = new Progreso(filename,to.getPieces(),connection);
                if(prg.getPiecesLength()==1){
                    HiloP hilo;
		    // search to connect to the first one that accepts the connection
                    for(PeerData sd: seeders){
			// create a thread for downloading
                        hilo = new HiloP(sd,instanceName,filename);
			// connection is successful
                        if(hilo.probe()){
			    // start the thread
                            hilo.start();
                            return;
                        }
                    }
                    System.out.println("There are not enough seeders");
                    return;
                }
                
                 // add the progress to your progress list the file is bigger than 512KB
                progreso.put(filename,prg);
		// get the leechers
                leechers = IndexServer.getLeechers(filename);
		// creates a new torrent with the file name
                Torrent tmp = new Torrent(filename,to.getLength());
		// adds himself as leecher
                tmp.addLeecher(p.Id);
		// logs into the server as leecher
                IndexServer.registryLeecher(p.Id,tmp); 
                
                // threads are created
                HiloP[] hilos = new HiloP[4];
                // creates a download window for the file
                VentanaD vent = new VentanaD(this,filename,prg);  
		// the download window becomes visible
                vent.setVisible(true);
                // an iterator is used, because it will be interrupted and then continued.
                Iterator<PeerData> it = seeders.iterator();
                
                while(it.hasNext()){
                    PeerData sd = (PeerData)it.next();
                    hilos[0] = new HiloP(sd,prg,instanceName,filename);
		    // Successful connection
                    if(hilos[0].probe()){
                        System.out.println("Will connect with SD:"+sd);
                        hilos[0].start();
                        connection[k]=sd.Id;
                        k++;
			// the iteration is broken
                        break;
                    }
                }
                
                if(hilos[0]==null){// no seeders connected
                    System.out.println("Could not connect with any seeder\nThe downoload was canceled");
                    return;
                }
		// eliminates itself as a leecher (this is a precaution, in theory it should not to be on the list unless you exit the program and run it again)
                leechers.remove(p);
	        // there are leechers
                if(leechers.size()>0){
		    // search for all leechers
                    for(PeerData lch: leechers){
                        hilos[k] = new HiloP(lch,prg,instanceName,filename);
			// Successful connection
                        if(hilos[k].probe()){
                            System.out.println("Will connect with LCH:"+lch);
			    // the thread begins
                            hilos[k].start();
                            connection[k]=lch.Id;
                            k++;
			    // there are already 4 connections
                            if(k==4){
	 			// the for is interrupted
                                break;
                            }
                        }
                    }
                }
                
                if(k<4){
		    // there are still no 4 connections
                    while(it.hasNext()){
		    // continues the previously interrupted iterator (of the seeders)
                    PeerData sd = (PeerData)it.next();
                        hilos[k] = new HiloP(sd,prg,instanceName,filename);
                        if(hilos[k].probe()){
			    // successful connection
                            System.out.println("Will connect with sd:"+sd);
                            hilos[k].start();
			    // the thread begins
                            connection[k]=sd.Id;
                            k++;
			    // 4 conections
                            if(k==4){
                                break;
                            }
                        }
                    }
                }

                System.out.println("Connetions: "+k);
                
               
	}
	
	 // joins the fragments        
        public void unir(String filename,int piezas){
            
                System.out.println("The file is being joined...");
                String strFilePath = instanceName + "/" + filename;
                File tmpFile = new File(filename);
                if(frg.unirPartes(strFilePath,piezas)){
		    // joined correctly
                    System.out.println("The file was joined correctly\n>");
                    try{
                        Torrent tmp = new Torrent(filename,tmpFile.length());
			// adds himself as a seeder
                        tmp.addSeeder(p.Id);
			// registers as a seeder on the server
                        IndexServer.registrySeeder(p.Id,tmp);
                    }catch(RemoteException e){
                        System.out.println("Failed to connect to server");
                        return;
                    }
                }
                
            
            
        }
        
        public int getId() throws RemoteException{
            return p.Id;
        }
        
        @Override
        public boolean probe() throws RemoteException{
            return true;
        }
}
