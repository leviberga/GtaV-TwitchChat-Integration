package com.github.leviberga.gtabridge;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class TwitchGtaBridge {

    private static TwitchClient twitchClient;
    private static GtaWebSocketClient gtaClient;

    public static void main(String[] args) {
        String twitchToken = "oauth:SECRET_TWITCH_TOKEN";
        String channelName = "leviberga";

        OAuth2Credential credential = new OAuth2Credential("twitch", twitchToken);

        twitchClient = TwitchClientBuilder.builder()
                .withEnableChat(true)
                .withChatAccount(credential)
                .build();
        try {
            gtaClient = new GtaWebSocketClient(new URI("ws://localhost:8080/gta"));
            gtaClient.connect();
        } catch (Exception e) {
            System.err.println("Não foi possível conectar ao GTA V. O jogo está aberto com o mod? " + e.getMessage());
        }
        twitchClient.getChat().joinChannel(channelName);
        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, TwitchGtaBridge::onChatMessage);

        System.out.println("-> Bot da Twitch iniciado com sucesso no canal: " + channelName);
    }

    private static void onChatMessage(ChannelMessageEvent event) {
        String message = event.getMessage().trim();
        String user = event.getUser().getName();

        if (message.startsWith("!ligar ")) {
            String speechText = message.substring(7);
            System.out.println("\n[Twitch] Nova ligação de " + user + ": " + speechText);

            boolean audioGerado = com.github.leviberga.gtabridge.services.TtsService.generateVoiceFile(speechText);

            if (audioGerado && gtaClient != null && gtaClient.isOpen()) {
                gtaClient.send("TOCAR_LIGACAO|" + user);
            }
        }
    }

    // Subclasse para gerenciar o cliente WebSocket que fala com o GTA V
    private static class GtaWebSocketClient extends WebSocketClient {
        public GtaWebSocketClient(URI serverUri) { super(serverUri); }
        @Override public void onOpen(ServerHandshake handshakedata) { System.out.println("-> Conectado com sucesso ao GTA V!"); }
        @Override public void onMessage(String message) { /* Respostas do jogo se necessário */ }
        @Override public void onClose(int code, String reason, boolean remote) { System.out.println("-> Conexão com o GTA V perdida."); }
        @Override public void onError(Exception ex) { ex.printStackTrace(); }
    }
}
