package org.obarcia.qrreader.services;

import java.awt.image.BufferedImage;
import java.io.File;
import org.obarcia.qrreader.swing.models.QRResult;

/**
 * Interface del servicio del la lectura del QR.
 * 
 * @author obarcia
 */
public interface QRReaderService
{
    /**
     * Procesar una imagen como QR.
     * @param image Instancia de la imagen.
     * @return Resultado de la operación.
     */
    public QRResult load(BufferedImage image);
    /**
     * Procesar un fichero como QR.
     * @param file Fichero de origen.
     * @return Resultado de la operación.
     */
    public QRResult load(File file);
    /**
     * Procesar un texto como QR.
     * @param data Datos como texto.
     * @return Resultado de la operación.
     */
    public QRResult load(String data);
    /**
     * Procesar una imagen como QR.
     * @param qrreader Resultado previo
     * @param page Número de página.
     * @return Resultado de la operación.
     */
    public QRResult load(QRResult qrreader, int page);
}