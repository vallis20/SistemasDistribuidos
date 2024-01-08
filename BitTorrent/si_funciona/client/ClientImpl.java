
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



/**
 * Aqui se encuentra toda la funcionalidad del cliente p2p
 */
public class ClientImpl extends UnicastRemoteObject implements ClientI  {

	// peer identifier
	
        final int PIECE_LENGTH=524288-16; //es la longitud de las piezas = 512KB - 16bytes por la encriptacion
        static final String key= "Mary has one cat";

        ServerI IndexServer; //La conexion con el servidor tracker
        String instanceName; //Nombre de la instancia
        String dirname; //Nombre de la carpeta compartida
        File dir; //se usa para leer los archivos del directorio
        
        int inConnections;//numero de conexiones de entrada
        int outConnections;//numero de conexiones de salida
        
        PeerData p;//los datos del peer
        final Fragmentacion frg;//Sirve para realizar las operaciones con los archivos de lectura, escritura y fragmentacion
        
        
        ConcurrentMap<String,Progreso> progreso; //con esto los peer se pueden comunicar entre si y saber que framentos tiene cada quien de cada archivo
	
    /**
     *
     * @param IndexServer Es la conexion con el servidor
     * @param p Son los datos del peer
     * @throws RemoteException
     */
    public ClientImpl(ServerI IndexServer,PeerData p) throws RemoteException {

                super();
                progreso = new ConcurrentHashMap<String,Progreso>();
                this.IndexServer = IndexServer;
                this.p = p;
                instanceName = "Peer" + p.Id;//es el nombre de la instancia, el cual corresponde a la carpeta compartida
                dirname = instanceName; //el nombre del directorio. Se manejo con otra variable por si en un futuro se cambiaba la direccion de este
                dir = new File(dirname);
		if (!dir.exists()) {//si el directorio no existe, se crea
			System.out.println("Creating new shared directory");
			dir.mkdir();
		}
                frg=new Fragmentacion();
	}
        
    /**
     *
     * @return
     */
    public boolean run(){//el metodo principal
            // main UI loop
		int choice=0;//aqui se guarda la opcion escogida
		String s;//sirve para leer la opcion
		Scanner scan = new Scanner(System.in);//sirve para leer la opcion
		InputStreamReader stream = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(stream);//sirve para leer el nombre del archivo
		boolean loop = true;
		String filename;
		
                register(dir);
                try {
                    System.out.println("\n\n" + p);
                    System.out.println("Options:");
                    System.out.println("1 - Search for filename");
                    System.out.println("2 - Obtain filename");
                    System.out.println("3 - List files in shared directory");
                    System.out.println("4 - Exit");
                    
                    System.out.print("\n\n>");

                    s = scan.nextLine();
                    try { choice = Integer.parseInt(s.trim()); }
                    catch(NumberFormatException e) {
                        //System.out.println("\nPlease enter an integer\n");
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
                            System.out.println("\nPlease enter a number between 1 and 4\n");
                            break;
                    }

                }
                catch(IOException ex) {
                    Logger.getLogger(ClientImpl.class.getName()).log(Level.SEVERE, null, ex);
                    //System.out.println("\nPlease enter an integer\n");
                }

                return loop;
        }

        @Override
	public byte[] obtain(String file, int piece) {//es una funcion remota, devuelve un arreglo de bytes

		if(inConnections<15)inConnections++;//comprueba el limite de conexiones, puesto que 15 es el maximo
                else return null;
                
                byte[] bytes = null;
                
		String pathfile = instanceName + "/" + file+"."+piece+".bin";

		
		File readfile = new File(pathfile);
		if (!readfile.exists()) { // No existe el fragmento
                    String pathfile2frag=instanceName + "/" + file; 
                    File file2frag = new File(pathfile2frag); //busca el archivo completo
                    if (!file2frag.exists()) {//no existe retorna null
                        inConnections--;//si no existe retorna null
                        return null;
                    }
                    if(piece==0){//significa que es la primera pieza
                        int size = (int) file2frag.length();
                        if(size<=PIECE_LENGTH){// El archivo es mas pequenio que 512KB
                            bytes = frg.read(file2frag);//En caso de que si, lee el archivo y lo retorna
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
                    frg.fragmentar(pathfile2frag,instanceName+"/",PIECE_LENGTH);//en caso de que el archivo sea mas grande de 512KB, lo fragmenta
		}
                bytes = frg.read(readfile); //ahora si, lee el fragmento para retornarlo
		inConnections--;
		return bytes; //retorna los bytes

	}

	/**
	 * Method for clients to list files currently in their shared directory
	 */
	
        public void list() {//muesta los archivos completos que se estan compartiendo
		File[] sharedfiles = dir.listFiles();
		System.out.println("\n\nFiles in shared directory: ");
                for (int i = 0; i < sharedfiles.length; i++) {
                    if(! sharedfiles[i].getName().endsWith(".bin")){
                        System.out.println(sharedfiles[i].getName());
                    }
                    
		}
		System.out.print("\n\n");

	}

	/**
	 * Le pregunta al servidor si existe el archivo senialado
	 * file
	 */
	private void search(String filename)
			throws RemoteException {

		double startTime=System.nanoTime();//sirve para contar el tiempo de respuesta
		double endTime;
                
		try {
			List<Integer> peers =  IndexServer.searchFile(filename); //obtiene una lista con los ID de los peer

			
			if (peers == null) {// Nadie tiene el archivo
				System.out.println("\n\nNo se han encontrado peers con el archivo ("+ filename + ")\n\n");
				return;
			}

			// 1 o mas peers tienen el archivo
			System.out.print("Los siguientes Peers tienen el archivo (" + filename+ ") :\n");
			
                        for(Integer pId : peers){//muestra los id's de cada peer
                            System.out.println(pId);
                        }
                        
			System.out.print("\n\n");

		} finally {
			endTime=System.nanoTime();
		}
		
                double duration = (endTime - startTime)/1000000000.0;
                DecimalFormat df = new DecimalFormat("#.##");
                System.out.println("Download Response time: "+ df.format(duration) + "s");
		
	}


	private void register(File dir){ //registra todos los archivos del directorio

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
                    IndexServer.registryPeer(p); //se registra el peer por si por alguna razon se perdio su registro en el lado del servidor
                }catch(RemoteException em){
                    System.out.println("\nError - No se pudo conectar con el servidor.\nSe intentara denuevo en 5s;");
                    try {
                        Thread.sleep(5000);
                        register(dir);//basicamente hace un bucle que termina cuando obtiene respuesta del servidor
                        return;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                try{
                    // registra todos los archivos
                    for (int i = 0; i < sharedfiles.length; i++) {

                        if(! sharedfiles[i].getName().endsWith(".bin")){ //hace un filtro para evitar registrar los fragmentos
                            tmp = new Torrent(sharedfiles[i].getName(),sharedfiles[i].length()); //crea un nuevo archivo tipo torrent
                            tmp.addSeeder(p.Id); //se aniade a si mismo como seeder
                            IndexServer.registrySeeder(p.Id,tmp); //lo registra en el servidor
                            Progreso prg = new Progreso(tmp.getName(),tmp.getPieces()); //se crea un objeto de progreso
                            prg.setFull(); //debido a que es su archivo, marca el progreso como completo
                            progreso.put(sharedfiles[i].getName(),prg); //aniade el objeto a su lista de progresos
                            totalSF++; //suma 1 al contador de archivos registrados
                        }
                    }
                }catch(RemoteException e){
                    System.out.println("Error al conectar con el servidor");
                    return;
                }
                System.out.println("# of files registered: " + totalSF);

	}

        
	/**
	 * Method to set up connection with peer, call obtain() to get file, write
	 * file to local shared directory and register file with Index Server
	 * 
	 */
        
        public Progreso getProgress(String filename){
            return progreso.get(filename); //revuelve el progreso relacionado al archivo indicado
        }
        
        
	private void getFile(String filename)//esta es la funcion principal que se encarga de obtener los archivos
			throws FileNotFoundException, IOException {

                //obtiene los datos del torrent
                List<PeerData> seeders=null;
                List<PeerData> leechers=null;
                int[] connection = new int[4];//esto es para saber los Id de los peer con los que ya se conecto (ademas, permite 4 conexiones por archivo)
                int k=0; //se usa para saber el numero de conexiones
                Torrent to= IndexServer.getTorrent(filename); //obtiene la informacion relacionada con el archivo
                if(to==null){ //No encuentra el archivo
                    System.out.println("File not found");
                    return;
                }
		
                
                
                
                seeders = IndexServer.getSeeders(filename);//obtiene la lista de seeders para el archivo
                if(seeders==null){
                    System.out.println("There are not enough seeders");
                    return;
                }
                seeders.remove(p);//quita al peer que quiere obtener el archivo de la lista para evitar conectarse consigo mismo
                if(seeders.size()<1){
                    System.out.println("There are not enough seeders");
                    return;
                }
                
                Progreso prg = new Progreso(filename,to.getPieces(),connection);//crea un nuevo progreso para el archivo
                if(prg.getPiecesLength()==1){//es de 512KB o menor
                    HiloP hilo;
                    for(PeerData sd: seeders){//busca conectarse con el primero que le acepte la conexion
                        hilo = new HiloP(sd,instanceName,filename);//crea un hilo para la descarga
                        if(hilo.probe()){//la conexion es exitosa
                            hilo.start();//inicia el hilo
                            return;
                        }
                    }
                    System.out.println("There are not enough seeders");
                    return;
                }
                
                
                progreso.put(filename,prg); //aniade el progreso a su lista de progresos
                //el archivo es mas grande que 512KB
                leechers = IndexServer.getLeechers(filename);//obtiene los leechers
                Torrent tmp = new Torrent(filename,to.getLength());//crea un nuevo torrent con el nombre del archivo
                tmp.addLeecher(p.Id);//se aniade a si mismo como leecher
                IndexServer.registryLeecher(p.Id,tmp); //se registra en el servidor como leecher
                
                
                HiloP[] hilos = new HiloP[4];//se crean los hilos
                
                VentanaD vent = new VentanaD(this,filename,prg); //crea una ventana de descarga para el archivo
                vent.setVisible(true);//se pone visible la ventana de descarga
                
                Iterator<PeerData> it = seeders.iterator();//se usa un iterador, porque se va a interrumpir y despues se continuara
                
                while(it.hasNext()){
                    PeerData sd = (PeerData)it.next();
                    hilos[0] = new HiloP(sd,prg,instanceName,filename);
                    if(hilos[0].probe()){//la conexion es exitosa
                        System.out.println("Se conectara con SD:"+sd);
                        hilos[0].start();
                        connection[k]=sd.Id;
                        k++;
                        break;//aqui se rompe la iteracion
                    }
                }
                
                if(hilos[0]==null){//no hay seeders conectados
                    System.out.println("No fue posible conectarse con ningun seeder\nSe ha cancelado la descarga");
                    return;
                }
                leechers.remove(p);//se elimina a si mismo como leecher (es por precaucion, en teoria no deberia de estar en la lista a menos que se salga del programa y se vuelva a correr)
                if(leechers.size()>0){//si hay leechers
                    for(PeerData lch: leechers){//busca a todos los leechers
                        hilos[k] = new HiloP(lch,prg,instanceName,filename);
                        if(hilos[k].probe()){//la conexion es exitosa
                            System.out.println("Se conectara con LCH:"+lch);
                            hilos[k].start();//comienza el hilo
                            connection[k]=lch.Id;
                            k++;
                            if(k==4){//ya hay 4 conexiones
                                break;//se interrumpe el for
                            }
                        }
                    }
                }
                
                if(k<4){//aun no hay 4 conexiones
                    while(it.hasNext()){//continua el iterador anteriormente interrumpido (de los seeders)
                    PeerData sd = (PeerData)it.next();
                        hilos[k] = new HiloP(sd,prg,instanceName,filename);
                        if(hilos[k].probe()){//exiro en la conexion
                            System.out.println("Se conectara con sd:"+sd);
                            hilos[k].start();//comienza el hilo
                            connection[k]=sd.Id;
                            k++;
                            if(k==4){//4 conexiones
                                break;
                            }
                        }
                    }
                }

                System.out.println("Numero de conexiones: "+k);
                
               
	}
        
        public void unir(String filename,int piezas){//une los fragmentos
            
                System.out.println("Uniendo archivo...");
                String strFilePath = instanceName + "/" + filename;
                File tmpFile = new File(filename);
                if(frg.unirPartes(strFilePath,piezas)){//si se unio correctamente
                    System.out.println("Archivo unido exitosamente.!\n>");
                    try{
                        Torrent tmp = new Torrent(filename,tmpFile.length());
                        tmp.addSeeder(p.Id);//se aniade a si mismo como seeder
                        IndexServer.registrySeeder(p.Id,tmp);//se registra como seeder en el servidor
                    }catch(RemoteException e){
                        System.out.println("Error al conectar con el servidor");
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
