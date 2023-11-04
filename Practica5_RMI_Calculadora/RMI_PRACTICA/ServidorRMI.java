//Este archivo es como el main del sistema

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry; 

public class ServidorRMI{
    public static void main(String[] args){

    try{
        Servicio_Calcu servicio = new  Servidor_Calcu();
        LocateRegistry.createRegistry(1099);//Puerto predeterminado para RMI
        Naming.rebind("Servicio_Calcu", servicio); //Registra el servicio en el registro RMI
        System.out.println("Servidor RMI iniciado");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}