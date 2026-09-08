package com.github.leviberga;

import com.github.leviberga.gtabridge.services.TtsService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class Main {

    // 1. Declara a variável global do cliente de rede que estava faltando
    private static GtaWebSocketClient gtaClient;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== INICIANDO TESTE DO SISTEMA DE ÁUDIO ===");

        String textoDeTeste = "Estou testando o audio de diferentes formas meu parceiro";

        // 2. Gera o arquivo de voz primeiro
        boolean sucesso = TtsService.generateVoiceFile(textoDeTeste);

        if (sucesso) {
            System.out.println("Áudio gerado. Tentando conectar ao GTA V...");

            // 3. Tenta abrir a conexão WebSocket
            try {
                gtaClient = new GtaWebSocketClient(new URI("ws://127.0.0.1:8080"));
                gtaClient.connect();

                // Aguarda 2 segundos para o handshake de rede estabilizar com o jogo
                System.out.println("Aguardando 2 segundos para o handshake de rede...");
                Thread.sleep(2000);

                if (gtaClient.isOpen()) {
                    System.out.println("Conexão aberta com sucesso! Disparando gatilho de chamada...");
                    gtaClient.send("TOCAR_LIGACAO|TesteLocal|" + textoDeTeste); // Nome do contato e o texto
                } else {
                    System.err.println("O canal de rede não abriu a tempo. O jogo está com o servidor ativo e atualizado?");
                }

            } catch (Exception e) {
                System.err.println("Erro de conexão: " + e.getMessage());
            }

            System.out.println("-> Aguardando encerramento do ciclo...");
            Thread.sleep(10000);
        }
    }

    // 4. Subclasse de rede necessária para o WebSocket funcionar neste arquivo
    private static class GtaWebSocketClient extends WebSocketClient {
        public GtaWebSocketClient(URI serverUri) {
            super(serverUri);
        }
        @Override
        public void onOpen(ServerHandshake handshakedata) {
            System.out.println("-> Conectado com sucesso ao GTA V!");
        }
        @Override
        public void onMessage(String message) {
            // Respostas do jogo se necessário
        }
        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("-> Conexão com o GTA V perdida.");
        }
        @Override
        public void onError(Exception ex) {
            ex.printStackTrace();
        }
    }
}
