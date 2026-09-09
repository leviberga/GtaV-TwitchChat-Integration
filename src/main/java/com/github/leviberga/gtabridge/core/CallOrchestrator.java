package com.github.leviberga.gtabridge.core;

import com.github.leviberga.gtabridge.client.GtaWebSocketClient;
import com.github.leviberga.gtabridge.model.CallRequest;
import com.github.leviberga.gtabridge.tts.TtsService;

/**
 * The one place that knows how to turn a CallRequest into an actual in-game
 * call: generate the voice audio, then tell the GTA V mod to play it.
 */
public class CallOrchestrator {

    private final GtaWebSocketClient gtaClient;

    public CallOrchestrator(GtaWebSocketClient gtaClient) {
        this.gtaClient = gtaClient;
    }

    public void process(CallRequest request) {
        System.out.println("[Ligação] " + request.callerName() + ": " + request.text());

        boolean audioGerado = TtsService.generateVoiceFile(request.text());

        if (!audioGerado) {
            System.err.println("-> Áudio não foi gerado, ligação cancelada.");
            return;
        }

        gtaClient.sendCallRequest(request.callerName(), request.text());
    }
}