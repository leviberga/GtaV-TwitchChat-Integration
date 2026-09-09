package com.github.leviberga.gtabridge.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * The single WebSocket connection to the GTA V mod (TwitchPhoneMod.cs).
 * There should only ever be one instance per process , Main creates it once
 * and hands it to whatever needs to send a call (CallOrchestrator) and to
 * whatever needs to react to messages coming back (CallQueue).
 */
public class GtaWebSocketClient extends WebSocketClient {

    private Consumer<String> messageListener;

    public GtaWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public static GtaWebSocketClient connectTo(String uri) throws URISyntaxException {
        GtaWebSocketClient client = new GtaWebSocketClient(new URI(uri));
        client.connect();
        return client;
    }

    /** Called for every message the mod sends back (e.g. "LIGACAO_CONCLUIDA"). */
    public void setMessageListener(Consumer<String> messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Sends a "trigger a call" command to the mod. This is the ONLY message
     * format TwitchPhoneMod.cs understands: TOCAR_LIGACAO|<callerName>|<text>.
     * '|' is stripped from both fields since the C# side splits the message on it.
     */
    public void sendCallRequest(String callerName, String text) {
        if (!isOpen()) {
            System.err.println("-> Tentei mandar uma ligação, mas a conexão com o GTA V não está aberta.");
            return;
        }

        String safeCaller = callerName.replace("|", "");
        String safeText = text.replace("|", "");

        send("TOCAR_LIGACAO|" + safeCaller + "|" + safeText);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("-> Conectado com sucesso ao GTA V!");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("<- Mensagem do GTA V: " + message);
        if (messageListener != null) {
            messageListener.accept(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("-> Conexão com o GTA V perdida: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}