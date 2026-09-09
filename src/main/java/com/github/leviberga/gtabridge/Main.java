package com.github.leviberga.gtabridge;

import com.github.leviberga.gtabridge.client.GtaWebSocketClient;
import com.github.leviberga.gtabridge.config.AppConfig;
import com.github.leviberga.gtabridge.core.CallOrchestrator;
import com.github.leviberga.gtabridge.model.CallRequest;
import com.github.leviberga.gtabridge.queue.CallQueue;
import com.github.leviberga.gtabridge.twitch.TwitchChatListener;

/**
 * Entry point. Connects once to the GTA V mod, wires up the call queue, then
 * either runs a single local mock call (java -jar app.jar --mock) or starts
 * the real Twitch chat listener (default). Both paths enqueue through the
 * same CallQueue, so mock and real behave identically.
 */
public class Main {

    private static final String GTA_WS_URI = "ws://127.0.0.1:8080";

    public static void main(String[] args) throws Exception {
        GtaWebSocketClient gtaClient = GtaWebSocketClient.connectTo(GTA_WS_URI);

        System.out.println("Aguardando handshake com o GTA V...");
        Thread.sleep(2000);

        if (!gtaClient.isOpen()) {
            System.err.println("O canal com o GTA V não abriu a tempo. O jogo está aberto com o mod?");
            return;
        }

        CallOrchestrator orchestrator = new CallOrchestrator(gtaClient);
        CallQueue callQueue = new CallQueue(orchestrator);
        gtaClient.setMessageListener(callQueue::onGtaMessage);
        callQueue.start();

        boolean mockMode = args.length > 0 && args[0].equals("--mock");

        if (mockMode) {
            runMockCall(callQueue);
            Thread.sleep(20000);
        } else {
            startTwitchListener(callQueue);
        }
    }

    private static void startTwitchListener(CallQueue callQueue) {
        String twitchToken;
        String channelName;

        try {
            twitchToken = AppConfig.twitchOAuthToken();
            channelName = AppConfig.twitchChannel();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            return;
        }

        new TwitchChatListener(callQueue).start(twitchToken, channelName);
    }

    private static void runMockCall(CallQueue callQueue) {
        String textoDeTeste = "Essa é a primeira ligação!";
        callQueue.enqueue(new CallRequest("TesteLocal", textoDeTeste));
        String textoDeTeste2 = "Essa é a segunda ligação!";
        callQueue.enqueue(new CallRequest("TesteLocal", textoDeTeste2));
        String textoDeTeste3 = "Essa é a terceira ligação!";
        callQueue.enqueue(new CallRequest("TesteLocal", textoDeTeste3));
    }
}