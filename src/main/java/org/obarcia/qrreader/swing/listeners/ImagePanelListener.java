package org.obarcia.qrreader.swing.listeners;

import org.obarcia.qrreader.swing.ImagePanel;

/**
 * Listener para el ImagePanelListener.
 * 
 * @author obarcia
 */
public interface ImagePanelListener
{
    /**
     * Selección de parte de la imagen.
     * @param source Instancia al objeto que lanza el evento.
     */
    public void selection(ImagePanel source);
    /**
     * Zoom aplicado.
     * @param source Instancia al objeto que lanza el evento.
     */
    public void zoom(ImagePanel source);
}
