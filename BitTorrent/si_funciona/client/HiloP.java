/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;



public class HiloP {
    
        private Timer timer;
        private TimeOutTask task;
        Progreso p,pB;
        PeerData rp;
        String instanceName;
        String filename;
        Fragmentacion frg;
        ClientI servingPeer=null;
        
        static final String key= "Mary has one cat";
        
        HiloP(PeerData rp,Progreso p,String instanceName, String filename){
            this.rp=rp;
            this.p=p;
            this.instanceName=instanceName;
            this.filename=filename;
            frg = new Fragmentacion();
        }
        
        HiloP(PeerData rp,String instanceName, String filename){
            this.rp=rp;
            this.p=null;
            this.instanceName=instanceName;
            this.filename=filename;
            frg = new Fragmentacion();
        }
        public void start() {
        //EJERCICIO
             
             if(p==null){//si el progreso es nulo, es porque desde antes ya se sabe que es un archivo pequenio
                 getSingleFile();
                 return;
             }
             timer = new Timer();
             task = new TimeOutTask();
             timer.schedule(task, 0);
        }
        
        public boolean probe(){//prueba la conexion con el peer
            boolean res=false;
            try {//obtiene el stub
                Registry registry = LocateRegistry.getRegistry(rp.host,rp.port);
                servingPeer =  (ClientI) registry.lookup("rmi://"+rp.host+":"+rp.port+"/"
                                + "Peer" + rp.Id);
                res=servingPeer.probe();
            } catch (NotBoundException e) {
                System.out.println("\nError - Invalid peer \""+rp+"\" entered.  Please enter valid peer");
            }catch (RemoteException e) {
                System.out.println("\nError - Invalid peer \""+rp+"\" entered.  Please enter valid peer");
            }
            
            return res;
        }

        public void getSingleFile(){//se usa para cuando los archivos son muy pequenios (igual o menor a 512KB)
            try{
                byte[] temp;
                Registry registry = LocateRegistry.getRegistry(rp.host,rp.port);
                servingPeer =  (ClientI) registry.lookup("rmi://"+rp.host+":"+rp.port+"/"
                            + "Peer" + rp.Id);
                temp=servingPeer.obtain(filename,0);
                if(temp==null){
                    System.out.println("Error, temp=null");
                }
                frg.write(CryptoUtils.decrypt(key, temp),instanceName+"/"+filename);
                System.out.println("Download complete");
            } catch (NotBoundException e) {
                    System.out.println("\nError - Invalid peer \""+rp+"\" entered.  Please enter valid peer");
            }catch (RemoteException e) {
                    System.out.println("\nError - RemoteException <class HiloP:start>");
            }catch (CryptoException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
            }
        }
        
        public void stop() {
            timer.cancel();
            timer.purge();
        }
        class TimeOutTask extends TimerTask {

        
            @Override
            public void run() {
                byte[] temp = null;
                int i;
                int tries=0;
                int clears=0;
                try{
                    System.out.println("Conectando con: "+rp);
                    Registry registry = LocateRegistry.getRegistry(rp.host,rp.port);
                    servingPeer =  (ClientI) registry.lookup("rmi://"+rp.host+":"+rp.port+"/" + "Peer" + rp.Id);//se conecta con el cliente al cual le pedira el archivo (cliente remoto)
                    pB=servingPeer.getProgress(filename);//obtiene el progreso del cliente remoto para el archivo a descargar
                    while(p.getValue()<100){//el bucle se cumple mientras no tenga el archivo completo
                        if((i = p.getIndexPieceNull(pB)) == -1){//obtiene el indice de la pieza a descargar, verificando que el cliente remoto tambien la tenga
                            //si el resultado es -1 significa o que el cliente remoto ya no tiene mas piezas, o que el cliente ya termino de descargar
                            Thread.sleep(500);//espera 500ms
                            tries++;//aumenta 1 a la cuenta de intentos
                            if(tries>20){//si ya son 20 intentos se limpia el progreso
                                p.clear();
                                clears++;
                                if(clears>20){
                                    break;
                                }
                            }
                            continue;//en caso contrario comienza un nuevo bucle
                        }
                        temp=servingPeer.obtain(filename,i); //descarga la pieza
                        if(temp==null){//verifica que se haya obtenido la pieza
                            System.out.println("Error: temp=null; i="+i);
                            Thread.sleep(500);//espera 500ms
                            tries++;//aumenta 1 a la cuenta de intentos
                            if(tries>20){//si ya son 20 intentos se limpia el progreso
                                p.clear();
                                clears++;
                                if(clears>20){
                                    break;
                                }
                            }
                            continue;
                        }
                        frg.write(temp,instanceName+"/"+filename,i);//escribe en disco la pieza
                        p.setPieceValue(i,(short)2);//actualiza el progreso
                    }
                    stop();
                } catch (NotBoundException e) {
                        System.out.println("\nError - Invalid peer \""+rp+"\" entered.  Please enter valid peer");
                }catch (RemoteException e) {
                        System.out.println("\nError - RemoteException <class HiloP:run>");
                } catch (InterruptedException ex) {
                    System.out.println("\nError - InterruptedException <class HiloP:run:sleep>");
                }
                    
            }
        }
    }