package org.obarcia.qrreader.swing;

import org.obarcia.qrreader.swing.listeners.ImagePanelListener;
import com.google.zxing.ResultPoint;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JViewport;

/**
 * Clase gráfica para el muestro de la imagen.
 * 
 * @author obarcia
 */
public class ImagePanel extends JPanel
{
    /**
     * Color del fondo.
     */
    private static final Color COLOR_BACKGROUND = new Color(160, 160, 160);
    /**
     * Listgeners.
     */
    private final ArrayList<ImagePanelListener> listeners = new ArrayList<>();
    /**
     * Imagen original.
     */
    private Image originalImage = null;
    /**
     * Imagen actual.
     */
    private Image image = null;
    /**
     * Control de selección.
     */
    private boolean started = false;
    /**
     * Puntos de selección.
     */
    private Point startPoint = null, endPoint = null;
    /**
     * Puntos a marcar en un resultado.
     */
    private ResultPoint[] points = null;
    /**
     * Texto cuando no hay imagen.
     */
    private String emptyText = "";
    /**
     * Zoom actual.
     */
    private float zoom = 1f;
    
    /**
     * Constructor de la clase.
     */
    public ImagePanel()
    {
        super();
        
        initComponents();
    }
    /**
     * Inicialización de los componentes.
     */
    private void initComponents()
    {
        setBackground(COLOR_BACKGROUND);
        
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (started) {
                    endPoint = e.getPoint();
                    
                    if (endPoint.x < 0) {
                        endPoint.x = 0;
                    }
                    if (endPoint.y < 0) {
                        endPoint.y = 0;
                    }
                    if (endPoint.x > image.getWidth(null)) {
                        endPoint.x = image.getWidth(null);
                    }
                    if (endPoint.y > image.getHeight(null)) {
                        endPoint.y = image.getHeight(null);
                    }
                    
                    processScrollbar(e.getPoint());
                    
                    refresh();
                }
            }
            @Override
            public void mouseMoved(MouseEvent e) {}
        });
        addMouseWheelListener((MouseWheelEvent e) -> {
            if (originalImage != null) {
                if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                    int rotation = e.getWheelRotation() * -1;

                    zoom += (((float)rotation) / 10);
                    if (zoom < 0.5f) zoom = 0.5f;
                    if (zoom > 5f) zoom = 5f;

                    setZoom(zoom);
                }
            }
        });
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {
                if (!started && image != null) {
                    started = true;
                    startPoint = e.getPoint();
                    endPoint = null;
                    
                    if (startPoint.x < 0) {
                        startPoint.x = 0;
                    }
                    if (startPoint.y < 0) {
                        startPoint.y = 0;
                    }
                    if (startPoint.x > image.getWidth(null)) {
                        startPoint.x = image.getWidth(null);
                    }
                    if (startPoint.y > image.getHeight(null)) {
                        startPoint.y = image.getHeight(null);
                    }
                    
                    refresh();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (started) {
                    if (startPoint != null && image != null && endPoint != null) {
                        if (Math.abs(startPoint.x - endPoint.x) <= 2 || 
                            Math.abs(startPoint.y - endPoint.y) <= 2) {
                            endPoint = null;
                        }
                    }
                    
                    listeners.stream().forEach((l) -> {
                        l.selection(ImagePanel.this);
                    });
                    
                    started = false;
                    
                    refresh();
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }
    @Override
    public void paint(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        if (image != null) {
            int w = image.getWidth(null);
            int h = image.getHeight(null);
            
            Graphics2D g2d = (Graphics2D) g;
            
            // Imagen completa
            if (endPoint == null) {
                g2d.drawImage(image, 0, 0, w, h, null);
            }
            
            // Limits
            g2d.setColor(Color.blue);
            g2d.drawLine(w, h, w, h - 4);
            g2d.drawLine(w, h, w - 4, h);
            
            
            int px = 0, py = 0;
            if (startPoint != null && endPoint != null) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawImage(image, 0, 0, w, h, null);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                
                int xo = (startPoint.x < endPoint.x ? startPoint.x : endPoint.x);
                int yo = (startPoint.y < endPoint.y ? startPoint.y : endPoint.y);
                int xf = (startPoint.x > endPoint.x ? startPoint.x : endPoint.x);
                int yf = (startPoint.y > endPoint.y ? startPoint.y : endPoint.y);
                
                // Imagen marcada
                if (xo != xf && yo != yf) {
                    BufferedImage bimage = toBufferedImage(image);
                    g2d.drawImage(bimage.getSubimage(xo, yo, xf - xo, yf - yo), xo, yo, xf - xo, yf - yo, null);
                }
                
                // Rectángulo
                g.setColor(Color.red);
                g.drawRect(xo, yo, xf - xo, yf - yo);
                
                px = xo;
                py = yo;
            }
            
            if (points != null && !started) {
                g.setColor(Color.green);
                for (ResultPoint p: points) {
                    g.fillRect(px + (int)p.getX() - 2, py + (int)p.getY() - 2, 5, 5);
                }
            }
        } else if (emptyText != null && !emptyText.isEmpty()) {
            FontMetrics metrics = g.getFontMetrics(getFont());
            int x = (getWidth() - metrics.stringWidth(emptyText)) / 2;
            int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
            g.setColor(Color.black);
            g.setFont(getFont());
            g.drawString(emptyText, x, y);
        }
    }
    /**
     * Refresco del componente.
     */
    private void refresh()
    {
        repaint();
        invalidate();
    }
    /**
     * Movimiento automático del scrollbar de existir.
     * @param p Instancia con el punto.
     */
    private void processScrollbar(Point p)
    {
        if (getParent() != null && getParent() instanceof JViewport) {
            JViewport view = (JViewport)getParent();
            Rectangle r = view.getViewRect();
            Point v = view.getViewPosition();
            
            if (p.x < r.x) {
                v.x -= 10;
                if (v.x < 0) v.x = 0;
            }
            if (p.y < r.y) {
                v.y -= 10;
                if (v.y < 0) v.y = 0;
            }
            if (p.x > r.x + r.width) {
                v.x += 10;
                if (v.x > view.getViewSize().width - r.width) {
                    v.x = view.getViewSize().width - r.width;
                }
            }
            if (p.y > r.y + r.height) {
                v.y += 10;
                if (v.y > view.getViewSize().height - r.height) {
                    v.y = view.getViewSize().height - r.height;
                }
            }
            
            view.setViewPosition(v);
        }
    }
    /**
     * Asignar la imagen.
     * @param img Instancia de la imagen.
     */
    public void setImage(Image img)
    {
        zoom = 1f;
        points = null;
        startPoint = null;
        endPoint = null;
        originalImage = img;
        image = img;
        
        listeners.stream().forEach((l) -> {
            l.zoom(ImagePanel.this);
        });
        
        
        refresh();
    }
    /**
     * Devuelve el zoom actual.
     * @return Valor del zoom actual.
     */
    public float getZoom()
    {
        return zoom;
    }
    /**
     * Asignar el zoom.
     * @param z Valor del zoom-
     */
    public void setZoom(float z)
    {
        zoom = z;
        points = null;
        startPoint = null;
        endPoint = null;
        
        if (originalImage != null) {
            BufferedImage bimage = new BufferedImage((int)(originalImage.getWidth(null) * zoom), (int)(originalImage.getHeight(null) * zoom), BufferedImage.TYPE_INT_ARGB);
                    
            Graphics2D bGr = bimage.createGraphics();
            bGr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform at = new AffineTransform();
            at.scale(zoom, zoom);
            bGr.drawImage(originalImage, at, null);
            bGr.dispose();

            image = bimage;
        }
        
        listeners.stream().forEach((l) -> {
            l.selection(ImagePanel.this);
            l.zoom(ImagePanel.this);
        });
        
        refresh();
    }
    /**
     * Centrar a la vista actual.
     */
    public void centerToView()
    {
        if (originalImage != null && getParent() instanceof JViewport) {
            JViewport view = (JViewport)getParent();
            Rectangle r = view.getViewRect();
            
            int w = r.width;
            int ow = originalImage.getWidth(null);
            
            setZoom(((float)w / (float)ow));
        }
    }
    /**
     * Asignar el texto cuando no hay imagen asignada.
     * @param text Texto a mostrar.
     */
    public void setEmptyText(String text)
    {
        emptyText = text;
    }
    @Override
    public Dimension getPreferredSize()
    {
        if (image != null) {
            return new Dimension(image.getWidth(null), image.getHeight(null));
        } else {
            return new Dimension(1, 1);
        }
    }
    /**
     * Asignar los puntos de control.
     * @param p Array con los puntos a mostrar.
     */
    public void setPoints(ResultPoint[] p)
    {
        points = p;
        
        refresh();
    }
    /**
     * Devuelve la imagen asignada.
     * @return Instancia de la imagen.
     */
    public Image getImage()
    {
        return image;
    }
    /**
     * Convertir una imagen en BufferedImage.
     * @param img Instancia de la imagen.
     * @return Imagen resultante.
     */
    private static BufferedImage toBufferedImage(Image img)
    {
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = bimage.createGraphics();
        bGr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //bGr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }
    /**
     * Devuelve la imagen seleccionada.
     * @return Instancia de la imagen seleccionada.
     */
    public Image getImageSelected()
    {
        if (image != null && startPoint != null && endPoint != null) {
            int xo = (startPoint.x < endPoint.x ? startPoint.x : endPoint.x);
            int yo = (startPoint.y < endPoint.y ? startPoint.y : endPoint.y);
            int xf = (startPoint.x > endPoint.x ? startPoint.x : endPoint.x);
            int yf = (startPoint.y > endPoint.y ? startPoint.y : endPoint.y);

            BufferedImage bimage = toBufferedImage(image);

            return bimage.getSubimage(xo, yo, xf - xo, yf - yo);
        }
        
        return null;
    }
    /**
     * Añadir un listener.
     * @param l Instancia del listener.
     */
    public void addListener(ImagePanelListener l)
    {
        listeners.add(l);
    }
    /**
     * Eliminar un listener.
     * @param l Instancia del listener.
     */
    public void removeListener(ImagePanelListener l)
    {
        listeners.remove(l);
    }
}