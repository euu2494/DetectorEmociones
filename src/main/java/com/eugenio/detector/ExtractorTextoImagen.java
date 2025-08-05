package com.eugenio.detector;

import net.sourceforge.tess4j.*;
import java.io.File;

public class ExtractorTextoImagen {

    private static final String TESSERACT_PATH = "C:\\Program Files\\Tesseract-OCR\\tesseract.exe";  // cámbialo si está en otra ruta

    public static String extraerTextoDeImagen(File imagen) {
        ITesseract tesseract = new Tesseract();
        System.setProperty("TESSDATA_PREFIX", "C:\\Program Files\\Tesseract-OCR\\");
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        tesseract.setLanguage("spa");  // español

        try {
            return tesseract.doOCR(imagen);
        } catch (TesseractException e) {
            return "❌ Error al extraer texto de la imagen: " + e.getMessage();
        }
    }
}
