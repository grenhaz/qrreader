package org.obarcia.qrreader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class.
 * 
 * @author obarcia
 */
public class QRReader
{
    public static void main(String[] args)
    {
        // Contexto
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml");
        // Cierre
        ((ClassPathXmlApplicationContext) (ctx)).close();
    }
}
