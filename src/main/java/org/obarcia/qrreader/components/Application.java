package org.obarcia.qrreader.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.obarcia.qrreader.swing.QRReaderPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Clase para la ventana de la aplicación.
 * 
 * @author obarcia
 */
@Component
public class Application extends JFrame
{
    /**
     * Instancia del Panel principal
     */
    @Autowired
    private QRReaderPanel qrPanel;
    
    /**
     * Inicialización del componente.
     */
    public void init()
    {
        // Asignar los valores de la ventana.
        setTitle("QR Reader");
        setIconImage(new ImageIcon(Application.class.getResource("/images/app.png")).getImage());
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(qrPanel);
        setVisible(true);
    }
}