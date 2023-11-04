import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Servicio_Calcu extends Remote{
    public double sumar(double a, double b) throws RemoteException;

    public double restar(double a, double b) throws RemoteException;

    public double multiplicar(double a, double b) throws RemoteException;

    public double dividir(double a, double b) throws RemoteException;
}
