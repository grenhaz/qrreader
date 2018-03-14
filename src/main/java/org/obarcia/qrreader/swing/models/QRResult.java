package org.obarcia.qrreader.swing.models;

import com.google.zxing.Result;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 *
 * @author obarcia
 */
public class QRResult
{
    /**
     * Imagen.
     */
    private BufferedImage image = null;
    /**
     * Resultado del QR.
     */
    private Result result = null;
    /**
     * Fichero de origen.
     */
    private File source = null;
    /**
     * Número de páginas.
     */
    private int pages = 0;
    /**
     * Tipo de origen.
     */
    private String type = "";

    // ************************************************
    // GETTER & SETTER
    // ************************************************
    public BufferedImage getImage()
    {
        return image;
    }
    public void setImage(BufferedImage value)
    {
        image = value;
    }
    public File getSource()
    {
        return source;
    }
    public void setSource(File value)
    {
        source = value;
    }
    public Result getResult()
    {
        return result;
    }
    public void setResult(Result value)
    {
        result = value;
    }
    public int getPages()
    {
        return pages;
    }
    public void setPages(int value)
    {
        pages = value;
    }
    public String getType()
    {
        return type;
    }
    public void setType(String value)
    {
        type = value;
    }
}