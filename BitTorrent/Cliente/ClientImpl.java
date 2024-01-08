
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
	
        final int PIECE_LENGTH=524288-16;  // the length of the parts = 512KB - 16bytes for encryption
        static final String key= "SD_2024";

        ServerI IndexServer; // Connection with the TrackerServer
        String instanceName; // Instance's name
        String dirname;// Share Directory's name
        File dir;  // Is used to read files from the directory
        
        int inConnections;// number of input connections
        int outConnections;// number of output connections
        
        PeerData p;// the peer data
        final Fragmentacion frg;//Read, write and fragment operations with the files fragmentation
        
        
        ConcurrentMap<String,Progreso> progreso; //the peers can communicate with each other and know who has which file
	
   /**
     *
     * @param IndexServer: Connection to the server
     * @param p :These are the peer data
     * @throws RemoteException
     */
    public ClientImpl(ServerI IndexServer,PeerData p) throws RemoteException {

                super();
                progreso = new ConcurrentHashMap<String,Progreso>();
                this.IndexServer = IndexServer;
                this.p = p;
                instanceName = "Peer " + p.Id;// is the name of the instance, which corresponds to the shared folder
                dirname = instanceName; // the name of the directory. It was handled with another variable in case in 	                                       //the future the address of the directory was changed.
                dir = new File(dirname);
		if (!dir.exists()) {// if the directory does not exist, it is created
			System.out.println("Creating new shared directory");
			dir.mkdir();
		}
                frg=new Fragmentacion();
	}
        
    /**
     *
     * @return
     */
	// the main method
    public boolean run(){
            // main UI loop
		int choice=0;// here you save the chosen option
		String s;// Variable to read the option
		Scanner scan = new Scanner(System.in);// Read the option
		InputStreamReader stream = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(stream);// Read the file name
		boolean loop = true;
		String filename;
		
                register(dir);
                try {
                    System.out.println("\n\n" + p);
                    System.out.println("------Menú-------");
                    System.out.println("1 - Search for file");
                    System.out.println("2 - Download file");
                    System.out.println("3 - List files in shared directory");
                    System.out.println("4 - Exit");
                    
                    System.out.print("\n>");

                    s = scan.nextLine();
                    try { choice = Integer.parseInt(s.trim()); }
                    catch(NumberFormatException e) {
                        //System.out.println("\nPlease enter an integer\n");
                    }

                    switch (choice) {


                        case 1:
                            System.out.print("Enter Filename: ");
                            filename = in.readLine();
                            System.out.print("\n");
                            search(filename);
                            break;

                        case 2:
                            System.out.print("Enter Filename: ");
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

        @Override
	public byte[] obtain(String file, int piece) {// is a remote function, it returns an array of bytes

		if(inConnections<15)inConnections++;// check the limit of connections, since 15 is the maximum.
                else return null;
                
                byte[] bytes = null;
                
		String pathfile = instanceName + "/" + file+"."+piece+".bin";

		
		File readfile = new File(pathfile);
		if (!readfile.exists()) { // The fragment does not exist
                    String pathfile2frag=instanceName + "/" + file; 
                    File file2frag = new File(pathfile2frag); // search for the complete file
                    if (!file2frag.exists()) {// does not exist returns null
                        inConnections--;//if it does not exist, returns null
                        return null;
                    }
                    if(piece==0){// means that it is the first piece
                        int size = (int) file2frag.length();
                        if(size<=PIECE_LENGTH){// File is smaller than 512KB
                            bytes = frg.read(file2frag);// If yes, it reads the file and returns it.
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
                    frg.fragmentar(pathfile2frag,instanceName+"/",PIECE_LENGTH);// in case the file is larger than 512KB, it fragments it.
		}
                bytes = frg.read(readfile); // Read the fragment to return it
		inConnections--;
		return bytes;  //returns the bytes

	}

	 //Method for clients to list files currently in their shared director
	
        public void list() {// shows the complete files being shared
		File[] sharedfiles = dir.listFiles();
		System.out.println("\n\nFiles in shared directory:");
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

		double startTime=System.nanoTime();// shows the complete files being shared
		double endTime;
                
		try {
			List<Integer> peers =  IndexServer.searchFile(filename); // gets a list of peer IDs
			
			if (peers == null) {// No ones have the file
				System.out.println("\n\nThere are no peers with the file "+ filename + "\n\n");
				return;
			}

			// 1 or more peers have the file
			System.out.print("The peers ");
			
                        for(Integer pId : peers){// shows the id's of each peer
                            System.out.println(""+pId + ",");
                        }
                        
			System.out.print(" they have the file: " + filename+ "\n");

		} finally {
			endTime=System.nanoTime();
		}
		
                double duration = (endTime - startTime)/1000000000.0;
                DecimalFormat df = new DecimalFormat("#.##");
                System.out.println("Search Time: "+ df.format(duration) + "s");
		
	}


	private void register(File dir){  // records all files in the directory

		// go through shared directory and register filenames with index server
		File[] sharedfiles = dir.listFiles();
                int totalSF=0;
                Torrent tmp;
		System.out.println(dir.getPath());
		// no files
		if (sharedfiles.length == 0) { 
                    System.out.println("No existen archivos");
                    return;
		}
                
                try{
                    IndexServer.registryPeer(p); // the peer is registered in case for some reason its registration was lost on the server side.
                }catch(RemoteException em){
                    System.out.println("\nError: Could not connect to the server.\nWill try again in: 2s;");
                    try {
                        Thread.sleep(2000);
                        register(dir);// basically makes a loop that ends when it gets a response from the server.
                        return;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                try{
                    // log all files
                    for (int i = 0; i < sharedfiles.length; i++) {

                        if(! sharedfiles[i].getName().endsWith(".bin")){ // makes a filter to avoid recording fragments
                            tmp = new Torrent(sharedfiles[i].getName(),sharedfiles[i].length());  // create a new torrent file
                            tmp.addSeeder(p.Id); // adds himself as a seeder
                            IndexServer.registrySeeder(p.Id,tmp); // registers it on the server
                            Progreso prg = new Progreso(tmp.getName(),tmp.getPieces()); //Create a progress object
                            prg.setFull(); // because it is your file, marks progress as complete
                            progreso.put(sharedfiles[i].getName(),prg);// adds the object to its progress list
                            totalSF++; // adds 1 to the counter of registered files
                        }
                    }
                }catch(RemoteException e){
                    System.out.println("Filed to connect to server");
                    return;
                }
                System.out.println("Registred files:" + totalSF);

	}

        
	// Method to set up connection with peer, call obtain() to get file, write file to local shared directory and register file with Index Server
        public Progreso getProgress(String filename){
            return progreso.get(filename); // return the progress related to the indicated file
        }
        
        
	private void getFile(String filename)// this is the main function that is in charge of obtaining the files
			throws FileNotFoundException, IOException {

                // get torrent data
                List<PeerData> seeders=null;
                List<PeerData> leechers=null;
                int[] connection = new int[4];// this is to know the Id's of the peers you are already connected to (moreover,it allows 4 connections per file)
                int k=0; // is used to know the number of connections
                Torrent to= IndexServer.getTorrent(filename); // obtains the information related to the file
                if(to==null){ // Cannot find the file
                    System.out.println("File not found");
                    return;
                }
		
                
                
                
                seeders = IndexServer.getSeeders(filename);// gets the list of seeders for the file
                if(seeders==null){
                    System.out.println("There are not enough seeders");
                    return;
                }
                seeders.remove(p);// removes the peer that wants to get the file from the list to avoid connecting to itself
                if(seeders.size()<1){
                    System.out.println("There are not enough seeders");
                    return;
                }
                
                Progreso prg = new Progreso(filename,to.getPieces(),connection);// creates a new progress for the file
                if(prg.getPiecesLength()==1){
                    HiloP hilo;
                    for(PeerData sd: seeders){// seeks to connect to the first one that accepts the connection
                        hilo = new HiloP(sd,instanceName,filename);// create a thread for downloading
                        if(hilo.probe()){// connection is successful
                            hilo.start();// start the thread
                            return;
                        }
                    }
                    System.out.println("There are not enough seeders");
                    return;
                }
                
                
                progreso.put(filename,prg);  // add the progress to your progress list the file is bigger than 512KB
                leechers = IndexServer.getLeechers(filename);// get the leechers
                Torrent tmp = new Torrent(filename,to.getLength());// creates a new torrent with the file name
                tmp.addLeecher(p.Id);// adds himself as leecher
                IndexServer.registryLeecher(p.Id,tmp); // logs into the server as leecher
                
                
                HiloP[] hilos = new HiloP[4];// threads are created
                
                VentanaD vent = new VentanaD(this,filename,prg); // creates a download window for the file
                vent.setVisible(true);// the download window becomes visible
                
                Iterator<PeerData> it = seeders.iterator();// an iterator is used, because it will be interrupted and then continued.
                
                while(it.hasNext()){
                    PeerData sd = (PeerData)it.next();
                    hilos[0] = new HiloP(sd,prg,instanceName,filename);
                    if(hilos[0].probe()){// Successful connection
                        System.out.println("Will connect with SD:"+sd);
                        hilos[0].start();
                        connection[k]=sd.Id;
                        k++;
                        break;// here the iteration is broken
                    }
                }
                
                if(hilos[0]==null){// no seeders connected
                    System.out.println("Could not connect with any seeder\nThe downoload was canceled");
                    return;
                }
                leechers.remove(p);// eliminates itself as a leecher (this is a precaution, in theory it should not to be on the list unless you exit the program and run it again)
                if(leechers.size()>0){// there are leechers
                    for(PeerData lch: leechers){// search for all leechers
                        hilos[k] = new HiloP(lch,prg,instanceName,filename);
                        if(hilos[k].probe()){// Successful connection
                            System.out.println("Will connect with LCH:"+lch);
                            hilos[k].start();// the thread begins
                            connection[k]=lch.Id;
                            k++;
                            if(k==4){// there are already 4 connections
                                break;// the for is interrupted
                            }
                        }
                    }
                }
                
                if(k<4){// there are still no 4 connections
                    while(it.hasNext()){// continues the previously interrupted iterator (of the seeders)
                    PeerData sd = (PeerData)it.next();
                        hilos[k] = new HiloP(sd,prg,instanceName,filename);
                        if(hilos[k].probe()){// successful connection
                            System.out.println("Will connect with sd:"+sd);
                            hilos[k].start();// the thread begins
                            connection[k]=sd.Id;
                            k++;
                            if(k==4){// 4 conections
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
                if(frg.unirPartes(strFilePath,piezas)){// joined correctly
                    System.out.println("he file was joined correctly.\n>");
                    try{
                        Torrent tmp = new Torrent(filename,tmpFile.length());
                        tmp.addSeeder(p.Id);// adds himself as a seeder
                        IndexServer.registrySeeder(p.Id,tmp);// registers as a seeder on the server
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
