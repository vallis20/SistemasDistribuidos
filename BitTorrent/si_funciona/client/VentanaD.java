/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;


public class VentanaD extends JFrame{
    
    JProgressBar pbFile;
    
    VentanaD(ClientImpl c,String title, Progreso p){
        super("Downloading");//aniade el titulo
        pbFile = new JProgressBar(); //crea la barra de progreso
        pbFile.setMaximum(100);//establece el maximo
        pbFile.setStringPainted(true);
        pbFile.setBorder(BorderFactory.createTitledBorder("Downloading: "+title));
        
        Container contentPane = this.getContentPane();
        contentPane.add(pbFile, BorderLayout.SOUTH);
        
        HiloV hv= new HiloV(c,pbFile,p);//crea un nuevo hilo, el cual servira para actualizar la ventana
        hv.start();//inicia el hilo
        setSize(300, 100);
        setLocationRelativeTo(null);
    }
    

        
}
