import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Servidor_Calcu extends UnicastRemoteObject implements Servicio_Calcu{
    public Servidor_Calcu() throws RemoteException{
        super();
    }

    public double sumar(double a, double b) throws RemoteException{
        return a+b;  
    }

    public double restar(double a, double b) throws RemoteException{
        return a-b;  
    }

    public double multiplicar(double a, double b) throws RemoteException{
        return a*b;  
    }

    public double dividir(double a, double b) throws RemoteException{
        return a/b;  
    }
}