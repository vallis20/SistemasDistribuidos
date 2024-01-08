import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;


public class VentanaD extends JFrame{
    
    JProgressBar pbFile;
    
    VentanaD(ClientImpl c,String title, Progreso p){
	// add title
        super("Downloading");
	// create progress bar
        pbFile = new JProgressBar(); 
	// set the maximun
        pbFile.setMaximum(100);
        pbFile.setStringPainted(true);
        pbFile.setBorder(BorderFactory.createTitledBorder("Downloading: "+title));
        
        Container contentPane = this.getContentPane();
        contentPane.add(pbFile, BorderLayout.SOUTH);
        // create a new thread, which will be used to update the window
        HiloV hv= new HiloV(c,pbFile,p);
	// start the thread
        hv.start();
        setSize(310, 90);
        setLocationRelativeTo(null);
    }
    

        
}
