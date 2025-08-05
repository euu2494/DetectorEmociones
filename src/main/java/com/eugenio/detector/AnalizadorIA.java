package com.eugenio.detector;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AnalizadorIA {

    private static final String API_KEY = cargarApiKey();

    private static String getApiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;
    }

    public static class ResultadoAnalisis {
        public String emocionPrincipal;
        public String textoCompleto;  // análisis detallado completo
        public String resumenGeneral; // resumen global del texto

        public ResultadoAnalisis(String emocionPrincipal, String textoCompleto, String resumenGeneral) {
            this.emocionPrincipal = emocionPrincipal;
            this.textoCompleto = textoCompleto;
            this.resumenGeneral = resumenGeneral;
        }
    }

    private static String cargarApiKey() {
        try {
            String key = new String(Files.readAllBytes(Paths.get("apikey.txt"))).trim();
            if (key.isEmpty()) {
                System.err.println("ERROR: El archivo apikey.txt está vacío.");
            } else {
                System.out.println("API key cargada correctamente.");
            }
            return key;
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo leer el archivo apikey.txt. " + e.getMessage());
            return "";
        }
    }

    // Limpia el texto eliminando símbolos raros, saltos excesivos, etc.
    private static String limpiarTexto(String texto) {
        if (texto == null) return "";
        // Reemplaza caracteres no ASCII excepto básicos (letras, números, signos comunes)
        // y elimina saltos de línea múltiples por uno solo.
        String limpio = texto.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", " ");
        limpio = limpio.replaceAll("\\s{2,}", " ").trim();
        return limpio;
    }

    public static ResultadoAnalisis analizarTextoConIA(String texto) {
        if (API_KEY.isEmpty()) {
            System.err.println("ERROR: API key no configurada. Revisa el archivo apikey.txt");
            return new ResultadoAnalisis("No disponible", "", "");
        }

        String textoLimpio = limpiarTexto(texto);

        String textoAnalisis = llamadaIAAnalisisDetallado(textoLimpio);
        String emocionPrincipal = extraerEmocionPrincipal(textoAnalisis);

        String resumen = llamadaIAResumenGeneral(textoLimpio);

        return new ResultadoAnalisis(emocionPrincipal, textoAnalisis, resumen);
    }

    private static String llamadaIAAnalisisDetallado(String texto) {
        return llamadaIA(
                "Eres un analizador de emociones experto. Analiza el siguiente texto y proporciona:\n\n" +
                        "1. Una lista con las emociones principales detectadas y su porcentaje aproximado. Usa formato tipo Markdown.\n\n" +
                        "Texto: " + texto
        );
    }

    private static String llamadaIAResumenGeneral(String texto) {
        return llamadaIA(
                "Lee el siguiente texto y redacta un resumen general del estado emocional que transmite. Sé directo y evita encabezados como 'Resumen:'.\n" +
                        "Texto: " + texto
        );
    }

    private static String llamadaIA(String prompt) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");

        String json = "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"text\": \"" + prompt.replace("\"", "\\\"") + "\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        RequestBody body = RequestBody.create(json, mediaType);

        Request request = new Request.Builder()
                .url(getApiUrl())
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String respuestaJson = response.body().string();

                JSONObject jsonObj = new JSONObject(respuestaJson);
                JSONArray candidates = jsonObj.getJSONArray("candidates");
                JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                // Aquí devolvemos solo el texto plano sin extras
                return parts.getJSONObject(0).getString("text").trim();

            } else {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                System.out.println("Error: " + response.code() + " - " + response.message());
                System.out.println("Detalle error: " + errorBody);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error al llamar a la IA: " + e.getMessage());
            return null;
        }
    }

    private static String extraerEmocionPrincipal(String textoRespuesta) {
        if (textoRespuesta == null) return "No detectada";
        String[] lineas = textoRespuesta.split("\n");
        for (String linea : lineas) {
            if (linea.matches(".*\\d+%.*")) { // Busca línea con porcentaje
                // Limpiamos la línea para devolver solo la emoción y porcentaje
                return linea.trim();
            }
        }
        return "No detectada";
    }

    // Método para imprimir resultados limpios
    public static void imprimirResultado(ResultadoAnalisis resultado) {
        System.out.println("🧠 Emoción principal detectada:\n" + resultado.emocionPrincipal + "\n");
        System.out.println("🧠 Análisis detallado:\n" + resultado.textoCompleto + "\n");
        System.out.println("📄 Resumen general:\n" + resultado.resumenGeneral + "\n");
    }
}
