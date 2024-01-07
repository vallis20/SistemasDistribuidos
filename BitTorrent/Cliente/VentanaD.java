
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class VentanaD extends JFrame {

    JProgressBar pbFile;

    VentanaD(ClientImpl c, String title, Progreso p) {
        super("Downloading");// add title
        pbFile = new JProgressBar(); // create progress bar
        pbFile.setMaximum(100);// set the maximun
        pbFile.setStringPainted(true);
        pbFile.setBorder(BorderFactory.createTitledBorder("Downloading: " + title));

        Container contentPane = this.getContentPane();
        contentPane.add(pbFile, BorderLayout.SOUTH);

        HiloV hv = new HiloV(c, pbFile, p);// create a new thread, which will be used to update the window
        hv.start();// start the thread
        setSize(300, 100);
        setLocationRelativeTo(null);
    }

}
