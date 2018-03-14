package org.obarcia.qrreader.swing;

import org.obarcia.qrreader.swing.models.PdfPage;
import com.google.zxing.Result;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.obarcia.qrreader.swing.models.QRResult;
import org.obarcia.qrreader.services.QRReaderService;
import org.obarcia.qrreader.swing.listeners.ImagePanelListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author obarcia
 */
public class QRReaderPanel extends JPanel
{
    /**
     * Instancia del servicio QRReader.
     */
    @Autowired
    private QRReaderService qrReader;
    
    /**
     * Resultado actual en el panel.
     */
    private QRResult currentResult = null;
    /**
     * Scroll del panel de la imagen.
     */
    private final JScrollPane scrollImage = new JScrollPane();
    /**
     * Scroll del resultado.
     */
    private final JScrollPane scrollText = new JScrollPane();
    /**
     * Panel de la imagen.
     */
    private final ImagePanel pnlImage = new ImagePanel();
    /**
     * Resultado.
     */
    private final JEditorPane txtInfo = new JEditorPane();
    /**
     * Botón de abrir un fichero.
     */
    private final JButton btnOpen = new JButton();
    /**
     * Botón de guardar un fichero.
     */
    private final JButton btnSave = new JButton();
    /**
     * Botón de volver al zoom original.
     */
    private final JButton btnOriginal = new JButton();
    /**
     * Botón de centrar la imagen.
     */
    private final JButton btnCenter = new JButton();
    /**
     * Listado de páginas.
     */
    private final JComboBox<PdfPage> cbPages = new JComboBox<>();
    /**
     * Label del resultado.
     */
    private final JLabel lblInfo = new JLabel();
    
    /**
     * Inicialización.
     */
    public void init()
    {
        setLayout(new BorderLayout());
        
        // Drop file
        setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent evt)
            {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    if (evt.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> droppedFiles = (List<File>)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        // Solamente se usa el primero
                        if (droppedFiles.size() > 0) {
                            File file = droppedFiles.get(0);

                            load(qrReader.load(file));
                        } else {
                            reset();
                        }
                    } else if (evt.getTransferable().isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        String str = (String)evt.getTransferable().getTransferData(DataFlavor.stringFlavor);
                        if (str != null && !str.isEmpty()) {
                            load(qrReader.load(str));
                        } else {
                            reset();
                        }
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    error(ex.toString());
                }
            }
        });
        
        // Menú
        // Panel superior
        JPanel pnlTop = new JPanel(new BorderLayout());
        add(pnlTop, BorderLayout.NORTH);
        
        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        pnlTop.add(toolbar, BorderLayout.WEST);
        
        // Botón de abrir
        btnOpen.setToolTipText("Abrir");
        btnOpen.setIcon(new ImageIcon(QRReaderPanel.class.getResource("/images/open.png")));
        btnOpen.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser(getLastFolder());
            
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                saveLastFolder(file);
                load(qrReader.load(file));
            }
        });
        toolbar.add(btnOpen);
        
        // Botón de guardar
        btnSave.setToolTipText("Guardar");
        btnSave.setIcon(new ImageIcon(QRReaderPanel.class.getResource("/images/save.png")));
        btnSave.addActionListener((ActionEvent e) -> {
            try {
                if (currentResult != null) {
                    BufferedImage img = currentResult.getImage();
                    if (img != null) {
                        JFileChooser fc = new JFileChooser(getLastFolder());
                        fc.setDialogType(JFileChooser.SAVE_DIALOG);
                        fc.setFileFilter(new FileNameExtensionFilter("PNG Image", "png"));

                        int returnVal = fc.showSaveDialog(null);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File outputfile = fc.getSelectedFile();
                            saveLastFolder(outputfile);
                            if (!outputfile.exists() && !outputfile.toString().endsWith(".png")) {
                                outputfile = new File(outputfile.getAbsoluteFile() + ".png");
                            }
                            ImageIO.write(img, "png", outputfile);
                        }
                    }
                }
            } catch(HeadlessException | IOException ex) {
                //ex.printStackTrace();
            }
        });
        toolbar.add(btnSave);
        
        toolbar.addSeparator();
        
        // Botón de tamaño original
        btnOriginal.setToolTipText("Tamaño original");
        btnOriginal.setIcon(new ImageIcon(QRReaderPanel.class.getResource("/images/original.png")));
        btnOriginal.addActionListener((ActionEvent e) -> {
            pnlImage.setZoom(1f);
        });
        toolbar.add(btnOriginal);
        
        // Botón de centrar
        btnCenter.setToolTipText("Centrar a pantalla");
        btnCenter.setIcon(new ImageIcon(QRReaderPanel.class.getResource("/images/center.png")));
        btnCenter.addActionListener((ActionEvent e) -> {
            pnlImage.centerToView();
        });
        toolbar.add(btnCenter);
        
        toolbar.addSeparator();
        
        // Páginas
        cbPages.setVisible(false);
        cbPages.addItemListener((ItemEvent event) -> {
            if (currentResult != null && 
                cbPages.isVisible() &&
                event.getStateChange() == ItemEvent.SELECTED) {
                PdfPage item = (PdfPage)event.getItem();

                currentResult = qrReader.load(currentResult, item.getPage());
                pnlImage.setImage(currentResult.getImage());
                applyResult(currentResult.getResult());
            }
        });
        toolbar.add(cbPages);
        
        // Panel para la imagen
        pnlImage.setEmptyText("Arrastre la imagen o el PDF aquí o péguela/o desde el portapapeles.");
        scrollImage.setViewportView(pnlImage);
        pnlImage.addListener(new ImagePanelListener() {
            @Override
            public void selection(ImagePanel source)
            {
                Image selected = source.getImageSelected();
                if (selected == null) {
                    selected = source.getImage();
                }
                
                QRResult result = qrReader.load(toBufferedImage(selected));
                applyResult(result.getResult());
            }
            @Override
            public void zoom(ImagePanel source) {}
        });
        add(scrollImage, BorderLayout.CENTER);
        
        // Panel inferior
        JPanel pnlBottom = new JPanel(new BorderLayout());
        add(pnlBottom, BorderLayout.SOUTH);
        
        // Datos del resultado
        lblInfo.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        pnlBottom.add(lblInfo, BorderLayout.NORTH);
        
        // Panel para el resultado
        scrollText.setPreferredSize(new Dimension(Integer.MAX_VALUE, 140));
        scrollText.setViewportView(txtInfo);
        txtInfo.setEditable(false);
        pnlBottom.add(scrollText, BorderLayout.CENTER);
        
        // CTRL + V
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), "ActionPaste");
        getActionMap().put("ActionPaste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doPaste();
            }
        });
        
        reset();
    }
    /**
     * Convertir una Image en un BufferedImage.
     * @param img Instancia del Image.
     * @return BufferedImage resultante.
     */
    public static BufferedImage toBufferedImage(Image img)
    {
        if (img == null) {
            return null;
        }
        
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }
    /**
     * Operación de pegar.
     */
    public void doPaste()
    {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable t = c.getContents(this);
        if (t == null) return;
        
        try {
            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                List<File> droppedFiles = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
                if (droppedFiles.size() > 0) {
                    File file = droppedFiles.get(0);

                    load(qrReader.load(file));
                } else {
                    reset();
                }
            } else if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image image = (Image)t.getTransferData(DataFlavor.imageFlavor);
                load(qrReader.load(toBufferedImage(image)));
            } else if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                load(qrReader.load((String)t.getTransferData(DataFlavor.stringFlavor)));
            }
        } catch (UnsupportedFlavorException | IOException e){
            error(e.toString());
        }
    }
    /**
     * Reset del panel.
     */
    private void reset()
    {
        // Reset current
        currentResult = null;
        
        // Reset panel image
        lblInfo.setForeground(Color.black);
        lblInfo.setText("");
        cbPages.removeAllItems();
        cbPages.setVisible(false);
        btnSave.setEnabled(false);
        txtInfo.setText("");
        pnlImage.setImage(null);
        pnlImage.setPoints(null);
        btnOriginal.setEnabled(false);
        btnCenter.setEnabled(false);
    }
    /**
     * Procesar un resultado.
     * @param result Instancia del resultado.
     */
    private void load(QRResult result)
    {
        if (result != null) {
            reset();

            // Botones
            btnOriginal.setEnabled(true);
            btnCenter.setEnabled(true);
            btnSave.setEnabled(true);
            
            currentResult = result;
            BufferedImage image = result.getImage();
            pnlImage.setImage(image);
            if (result.getPages() > 1) {
                for (int i = 1; i <= result.getPages(); i ++) {
                    cbPages.addItem(new PdfPage(i));
                }
            }
            cbPages.setVisible(result.getPages() > 1);
            applyResult(result.getResult());
            if (image == null) {
                error("Formato de imagen no soportado");
            }
        } else {
            error("No se pudo generar un resultado");
        }
    }
    /**
     * Aplicar un resultado.
     * @param result Instancia del resultado.
     */
    private void applyResult(Result result)
    {
        if (result != null) {
            lblInfo.setText(result.getBarcodeFormat().name());
            txtInfo.setText(result.getText());
            pnlImage.setPoints(result.getResultPoints());
        } else {
            txtInfo.setText("");
            pnlImage.setPoints(null);
            lblInfo.setForeground(Color.red);
            lblInfo.setText("No se ha encontrado un código en la imagen");
        }
    }
    /**
     * Procesamiento de error.
     * @param text Texto de error.
     */
    private void error(String text)
    {
        reset();
        
        // Mostrar el error
        lblInfo.setForeground(Color.red);
        lblInfo.setText(text);
    }
    /**
     * Obtener el último directorio abierto.
     * @return último directorio abierto o el actual.
     */
    protected String getLastFolder()
    {
        try {
            Preferences prefs = Preferences.userRoot().node(getClass().getName());
            prefs.sync();
            return prefs.get("LastFolder", new File(".").getAbsolutePath());
        } catch (Exception e) {}
        
        return new File(".").getAbsolutePath();
    }
    /**
     * Guardado del último directorio abierto.
     * @param file Fichero de ubicación.
     */
    protected void saveLastFolder(File file)
    {
        try {
            Preferences prefs = Preferences.userRoot().node(getClass().getName());
            prefs.put("LastFolder", file.getParent());
            prefs.flush();
        } catch (Exception e) {}
    }
}