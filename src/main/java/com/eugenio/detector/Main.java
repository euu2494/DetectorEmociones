package com.eugenio.detector;

import java.util.Scanner;
import java.nio.file.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {

    private static final String HISTORIAL_DIR = "historial";
    private static final String HISTORIAL_FILE = HISTORIAL_DIR + "/registro.txt";
    private static Map<String, Integer> estadisticas = new HashMap<>();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        crearDirectorioHistorial();
        cargarEstadisticasDesdeHistorial();  // Carga las estadísticas desde el historial guardado

        while (true) {
            System.out.println("\n==============================");
            System.out.println("1. Analizar texto manual con IA (Gemini)");
            System.out.println("2. Analizar archivo (.txt, .pdf, .docx)");
            System.out.println("3. Analizar imagen (.jpg, .png)");
            System.out.println("4. Ver historial");
            System.out.println("5. Borrar historial");
            System.out.println("6. Ver estadísticas");
            System.out.println("7. Borrar estadísticas");
            System.out.println("8. Salir");
            System.out.print("Elige una opción: ");
            String opcion = sc.nextLine().trim();

            switch (opcion) {
                case "1":
                    System.out.print("Introduce el texto para analizar con IA: ");
                    String texto = sc.nextLine();

                    AnalizadorIA.ResultadoAnalisis resultado = AnalizadorIA.analizarTextoConIA(texto);

                    if (resultado != null) {
                        System.out.println("🧠 Emoción principal: " + resultado.emocionPrincipal);
                        System.out.println("\n🧠 Resultado completo:\n" + resultado.textoCompleto);
                        System.out.println("\n📄 Resumen general:\n" + resultado.resumenGeneral);

                        guardarHistorial(texto, resultado.emocionPrincipal, resultado.resumenGeneral);

                        actualizarEstadisticas(resultado.emocionPrincipal);
                    } else {
                        System.out.println("No se pudo obtener resultado.");
                    }
                    break;
                case "2": {
                    System.out.print("Introduce la ruta del archivo a analizar (.txt, .pdf, .docx): ");
                    String rutaArchivo = sc.nextLine();
                    File archivo = new File(rutaArchivo);

                    if (!archivo.exists() || !archivo.isFile()) {
                        System.out.println("❌ Archivo no encontrado o ruta inválida.");
                        break;
                    }

                    String textoExtraido = ExtractorTextoArchivo.extraerTexto(archivo);

                    if (textoExtraido == null || textoExtraido.isBlank()) {
                        System.out.println("❌ No se pudo extraer texto del archivo.");
                        break;
                    }

                    System.out.println("\n📄 Texto extraído del archivo:\n" + textoExtraido);

                    AnalizadorIA.ResultadoAnalisis resultadoArchivo = AnalizadorIA.analizarTextoConIA(textoExtraido);

                    if (resultadoArchivo != null) {
                        System.out.println("🧠 Emoción principal: " + resultadoArchivo.emocionPrincipal);
                        System.out.println("\n🧠 Resultado completo:\n" + resultadoArchivo.textoCompleto);
                        System.out.println("\n📄 Resumen general:\n" + resultadoArchivo.resumenGeneral);

                        guardarHistorial("[Archivo: " + archivo.getName() + "] " + textoExtraido, resultadoArchivo.emocionPrincipal, resultadoArchivo.resumenGeneral);

                        actualizarEstadisticas(resultadoArchivo.emocionPrincipal);
                    } else {
                        System.out.println("No se pudo obtener resultado.");
                    }
                    break;
                }

                case "3": {
                    System.out.print("Introduce la ruta de la imagen (.jpg, .png): ");
                    String rutaImagen = sc.nextLine();
                    File imagen = new File(rutaImagen);
                    if (!imagen.exists() || !imagen.isFile()) {
                        System.out.println("❌ Archivo no encontrado o ruta inválida.");
                        break;
                    }

                    String textoExtraido = ExtractorTextoImagen.extraerTextoDeImagen(imagen);
                    if (textoExtraido == null || textoExtraido.isBlank()) {
                        System.out.println("❌ No se pudo extraer texto de la imagen.");
                        break;
                    }

                    System.out.println("📝 Texto extraído:\n" + textoExtraido);

                    AnalizadorIA.ResultadoAnalisis resultadoImagen = AnalizadorIA.analizarTextoConIA(textoExtraido);

                    if (resultadoImagen != null) {
                        System.out.println("🧠 Emoción principal: " + resultadoImagen.emocionPrincipal);
                        System.out.println("\n🧠 Resultado completo:\n" + resultadoImagen.textoCompleto);
                        System.out.println("\n📄 Resumen general:\n" + resultadoImagen.resumenGeneral);

                        guardarHistorial(textoExtraido, resultadoImagen.emocionPrincipal, resultadoImagen.resumenGeneral);

                        actualizarEstadisticas(resultadoImagen.emocionPrincipal);
                    } else {
                        System.out.println("No se pudo obtener resultado.");
                    }
                    break;
                }

                case "4":
                    mostrarHistorial();
                    break;

                case "5":
                    borrarHistorial();
                    break;

                case "6":
                    mostrarEstadisticas();
                    break;

                case "7":
                    borrarEstadisticas();
                    break;

                case "8":
                    System.out.println("Saliendo...");
                    sc.close();
                    return;

                default:
                    System.out.println("Opción no válida. Intenta de nuevo.");
            }
        }
    }

    private static void guardarHistorial(String textoOriginal, String emocion, String resumen) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORIAL_FILE, true))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String ahora = LocalDateTime.now().format(formatter);
            writer.write("[" + ahora + "] Texto: \"" + textoOriginal + "\" → Emoción: " + emocion);
            writer.newLine();
            if (resumen != null && !resumen.isEmpty()) {
                writer.write("Resumen: " + resumen);
                writer.newLine();
            }
            writer.newLine();
        } catch (IOException e) {
            System.out.println("⚠ No se pudo guardar el historial: " + e.getMessage());
        }
    }

    private static void mostrarHistorial() {
        Path rutaHistorial = Paths.get(HISTORIAL_FILE);
        if (!Files.exists(rutaHistorial)) {
            System.out.println("⚠ No hay historial guardado aún.");
            return;
        }

        System.out.println("\n--- Historial de análisis ---");
        try {
            Files.lines(rutaHistorial).forEach(System.out::println);
        } catch (IOException e) {
            System.out.println("⚠ Error al leer el historial: " + e.getMessage());
        }
        System.out.println("--- Fin del historial ---\n");
    }

    private static void borrarHistorial() {
        File archivoHistorial = new File(HISTORIAL_FILE);
        if (archivoHistorial.exists()) {
            if (archivoHistorial.delete()) {
                System.out.println("✅ Historial borrado correctamente.");
                estadisticas.clear(); // Limpiar estadísticas también porque el historial se borró
            } else {
                System.out.println("⚠ No se pudo borrar el historial.");
            }
        } else {
            System.out.println("⚠ No hay historial para borrar.");
        }
    }

    public static List<String> leerHistorial() {
        List<String> lineas = new ArrayList<>();
        File archivo = new File(HISTORIAL_FILE);

        if (archivo.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    if (!linea.trim().isEmpty()) {
                        lineas.add(linea);
                    }
                }
            } catch (IOException e) {
                System.out.println("❌ Error al leer el historial: " + e.getMessage());
            }
        }

        return lineas;
    }

    public static void mostrarEstadisticas() {
        if (estadisticas.isEmpty()) {
            System.out.println("No hay datos para mostrar estadísticas.");
            return;
        }

        int totalAnalisis = estadisticas.values().stream().mapToInt(Integer::intValue).sum();

        System.out.println("📊 Estadísticas Generales:");
        System.out.println("Total de análisis realizados: " + totalAnalisis);
        System.out.println("\nFrecuencia de emociones principales:");

        String emocionMasComun = null;
        int maxFrecuencia = 0;

        for (Map.Entry<String, Integer> entry : estadisticas.entrySet()) {
            String emocion = entry.getKey();
            int conteo = entry.getValue();
            double porcentaje = (conteo * 100.0) / totalAnalisis;

            System.out.printf("- %s: %d veces (%.2f%%)%n", emocion, conteo, porcentaje);

            if (conteo > maxFrecuencia) {
                maxFrecuencia = conteo;
                emocionMasComun = emocion;
            }
        }

        System.out.println("\nEmoción más común: " + emocionMasComun + " (" + maxFrecuencia + " veces)");
    }

    private static void borrarEstadisticas() {
        estadisticas.clear();
        System.out.println("✅ Estadísticas en memoria borradas.");
    }

    private static void crearDirectorioHistorial() {
        File dir = new File(HISTORIAL_DIR);
        if (!dir.exists()) {
            boolean creado = dir.mkdir();
            if (!creado) {
                System.out.println("⚠ No se pudo crear el directorio de historial.");
            }
        }
    }

    private static void actualizarEstadisticas(String emocionCompleta) {
        if (emocionCompleta == null || emocionCompleta.isBlank()) return;

        // Extraemos la emoción antes de los dos puntos (":")
        String emocion = emocionCompleta.split(":")[0].trim();

        estadisticas.put(emocion, estadisticas.getOrDefault(emocion, 0) + 1);
    }

    private static void cargarEstadisticasDesdeHistorial() {
        List<String> lineas = leerHistorial();
        estadisticas.clear();
        for (String linea : lineas) {
            if (linea.contains("→ Emoción:")) {
                String emocion = linea.split("→ Emoción:")[1].trim();
                // En caso de que la emoción venga con texto después de ':', solo cogemos la parte antes
                emocion = emocion.split(":")[0].trim();
                estadisticas.put(emocion, estadisticas.getOrDefault(emocion, 0) + 1);
            }
        }
    }
}
