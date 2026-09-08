package com.github.leviberga.gtabridge.services;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TtsService {

    private static final String API_KEY = "kokoro-local";
    private static final String OPENAI_TTS_URL = "http://localhost:8880/v1/audio/speech";

    private static final String GTA_SCRIPTS_PATH = "C:\\Program Files\\Epic Games\\GTAVEnhanced\\scripts\\twitch_audio.wav";

    public static boolean generateVoiceFile(String text) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonPayload = "{"
                    + "\"model\": \"kokoro\","
                    + "\"input\": \"" + text.replace("\"", "\\\"") + "\","
                    + "\"voice\": \"pm_alex\","
                    + "\"lang\": \"p\","
                    + "\"response_format\": \"wav\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_TTS_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            System.out.println("-> Enviando texto para a IA de Voz...");
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                // Deleta o arquivo anterior para não dar conflito de I/O no jogo
                Files.deleteIfExists(Paths.get(GTA_SCRIPTS_PATH));

                // Salva o novo arquivo de áudio direto na pasta do GTA V
                try (InputStream is = response.body();
                     FileOutputStream fos = new FileOutputStream(GTA_SCRIPTS_PATH)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                System.out.println("-> Áudio salvo com sucesso em: " + GTA_SCRIPTS_PATH);
                return true;
            } else {
                System.err.println("Erro na API de TTS. Status Code: " + response.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Falha ao gerar áudio por IA: " + e.getMessage());
            return false;
        }
    }
}
