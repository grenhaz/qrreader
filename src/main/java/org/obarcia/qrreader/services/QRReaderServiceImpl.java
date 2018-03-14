package org.obarcia.qrreader.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.obarcia.qrreader.swing.models.QRResult;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio del la lectura del QR.
 * 
 * @author obarcia
 */
@Service
public class QRReaderServiceImpl implements QRReaderService
{
    /**
     * Procesar la imagen y obtener el resultado.
     * @param binaryMap Instancia de la imagen.
     * @return Resultado del QR.
     */
    private Result getResult(BinaryBitmap binaryMap)
    {
        try {
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
            //hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            hints.put(DecodeHintType.TRY_HARDER, true);
            
            Result qrCodeResult = new MultiFormatReader().decode(binaryMap, hints);
            return qrCodeResult;
        } catch(Exception ex) {
            Logger.getLogger(QRReaderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    /**
     * Obtener el resultado de una imagen en el QR.
     * @param image Instancia de la imagen.
     * @return Resultado del QR.
     */
    private Result getResultFromImage(BufferedImage image)
    {
        LuminanceSource src = new BufferedImageLuminanceSource(image);
        BinaryBitmap binaryMap = new BinaryBitmap(new HybridBinarizer(src));

        return getResult(binaryMap);
    }
    /**
     * Procesado de un PDF.
     * @param qrr Resultado previo y devuelto.
     * @param file Instancia del PDF.
     * @param page Página a utilizar
     * @throws IOException 
     */
    private void loadFromPdf(QRResult qrr, File file, int page) throws IOException
    {
        PDDocument document = PDDocument.load(file);
        if (!document.isEncrypted()) {
            List<PDPage> pdPages = document.getDocumentCatalog().getAllPages();
            
            BufferedImage image = pdPages.get(page).convertToImage(BufferedImage.TYPE_INT_RGB, 300);
            
            document.close();
            
            qrr.setImage(image);
            qrr.setPages(pdPages.size());
            qrr.setResult(getResultFromImage(image));
        }
    }
    @Override
    public QRResult load(BufferedImage image)
    {
        QRResult qrr = new QRResult();
        qrr.setType("iamge");
        qrr.setImage(image);
        qrr.setResult(getResultFromImage(image));
        qrr.setPages(1);
        
        return qrr;
    }
    @Override
    public QRResult load(File file)
    {
        QRResult qrr = new QRResult();
        qrr.setSource(file);
        
        try {
            //Comprobar si es un PDF
            if (file.toString().endsWith(".pdf")) {
                // Como un PDF
                qrr.setType("pdf");
                loadFromPdf(qrr, file, 0);
            } else {
                // Como una imagen
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    qrr.setType("image");
                    qrr.setImage(image);
                    qrr.setResult(getResultFromImage(image));
                    qrr.setPages(1);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(QRReaderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return qrr;
    }
    @Override
    public QRResult load(String data)
    {
        // Si es un fichero se procesa
        File file = new File(data);
        if (file.exists()) {
            return load(file);
        }
        
        QRResult qrr = new QRResult();
        return qrr;
    }
    @Override
    public QRResult load(QRResult qrreader, int page)
    {
        if (qrreader != null) {
            if (page > 0 && page <= qrreader.getPages()) {
                if (qrreader.getType().equals("pdf")) {
                    try {
                        // PDF
                        loadFromPdf(qrreader, qrreader.getSource(), page - 1);
                    } catch (IOException ex) {
                        Logger.getLogger(QRReaderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (qrreader.getImage() != null) {
                    // Image
                    QRResult nr = load(qrreader.getImage());
                    qrreader.setResult(nr.getResult());
                }
            } else {
                Logger.getLogger(QRReaderServiceImpl.class.getName()).log(Level.SEVERE, "Page not found");
            }
        }
        
        return qrreader;
    }
}