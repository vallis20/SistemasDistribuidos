import java.rmi.Naming;

public class ClienteRMI {
    public static void main(String[] args){
        try{
            Servicio_Calcu servicio = (Servicio_Calcu) Naming.lookup("rmi://localhost/Servicio_Calcu");
            double a=50.5;
            double b=11.25;
            double sumar = servicio.sumar(a,b);
            System.out.println("El resultado de la suma de "+a+" y " + b+ " es:" + sumar);

            double restar = servicio.restar(a,b);
            System.out.println("El resultado de la resta de "+a+" y " + b+ " es:" + restar);

            double multiplicar = servicio.multiplicar(a,b);
            System.out.println("El resultado de la multiplicacion de "+a+" y " + b+ " es:" + multiplicar);

            double dividir = servicio.dividir(a,b);
            System.out.println("El resultado de la division de "+a+" y " + b+ " es:" + dividir);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
