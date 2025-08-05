package com.eugenio.detector;

import java.io.*;
import java.nio.file.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.*;

public class ExtractorTextoArchivo {
	
	public static String extraerTexto(File archivo) {
		String nombre = archivo.getName().toLowerCase();
			
			try {
				if (nombre.endsWith(".txt")) {
					return extraerDeTxt(archivo);
				}else if (nombre.endsWith(".pdf")) {
					return extraerDePdf(archivo);
				}else if (nombre.endsWith(".docx")) {
					return extraerDeDocx(archivo);
				}else {
					return "❌ Tipo de archivo no soportado todavía.";
				}
			}catch (Exception e) {
				return "❌ Error al extraer texto: " + e.getMessage();
			}
	}

	private static String extraerDeTxt(File archivo) throws IOException {
		return Files.readString(archivo.toPath());
	}

	private static String extraerDePdf(File archivo) throws IOException {
		try (PDDocument document = PDDocument.load(archivo)) {
			PDFTextStripper stripper = new PDFTextStripper();
			return stripper.getText(document);
		}
	}

	private static String extraerDeDocx(File archivo) throws IOException {
		try (FileInputStream fis = new FileInputStream(archivo);
			 XWPFDocument doc = new XWPFDocument(fis)) {
			StringBuilder sb = new StringBuilder();
			for (XWPFParagraph p : doc.getParagraphs()) {
				sb.append(p.getText()).append("\n");
			}
			return sb.toString();
		}
	}
}
