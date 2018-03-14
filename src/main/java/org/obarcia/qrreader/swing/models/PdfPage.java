package org.obarcia.qrreader.swing.models;

/**
 * Instancia de una página para el combobox.
 * 
 * @author obarcia
 */
public class PdfPage
{
    /**
     * Número de página.
     */
    private int page;
    
    /**
     * Constructor de la clase.
     * @param p Número de página.
     */
    public PdfPage(int p)
    {
        page = p;
    }
    @Override
    public String toString()
    {
        return "PÁGINA " + page;
    }
    // *******************************************
    // GETTER & SETTER
    // *******************************************
    public int getPage()
    {
        return page;
    }
    public void setPage(int page)
    {
        this.page = page;
    }
}