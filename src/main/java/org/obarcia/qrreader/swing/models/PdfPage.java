package org.obarcia.qrreader.swing.models;

/**
 * Instancia de una p�gina para el combobox.
 * 
 * @author obarcia
 */
public class PdfPage
{
    /**
     * N�mero de p�gina.
     */
    private int page;
    
    /**
     * Constructor de la clase.
     * @param p N�mero de p�gina.
     */
    public PdfPage(int p)
    {
        page = p;
    }
    @Override
    public String toString()
    {
        return "P�GINA " + page;
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