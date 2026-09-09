package com.github.leviberga.gtabridge.queue;

import com.github.leviberga.gtabridge.core.CallOrchestrator;
import com.github.leviberga.gtabridge.model.CallRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Serializes calls so only one plays in-game at a time. Twitch chat can fire
 * "!ligar" from several viewers within the same second;
 */
public class CallQueue {

    private static final int CALL_TIMEOUT_SECONDS = 30;

    private final BlockingQueue<CallRequest> pending = new LinkedBlockingQueue<>();
    private final CallOrchestrator orchestrator;

    private volatile CountDownLatch currentCallLatch;

    public CallQueue(CallOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public void start() {
        Thread worker = new Thread(this::runLoop, "call-queue-worker");
        worker.setDaemon(true);
        worker.start();
    }

    public void enqueue(CallRequest request) {
        pending.offer(request);
        System.out.println("[Fila] " + request.callerName() + " entrou na fila ("
                + pending.size() + " aguardando).");
    }

    public void onGtaMessage(String message) {
        if ("LIGACAO_CONCLUIDA".equals(message.trim())) {
            CountDownLatch latch = currentCallLatch;
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    private void runLoop() {
        while (true) {
            try {
                CallRequest request = pending.take(); // bloqueia até ter algo na fila

                currentCallLatch = new CountDownLatch(1);
                orchestrator.process(request);

                boolean confirmed = currentCallLatch.await(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (!confirmed) {
                    System.err.println("[Fila] GTA V não confirmou o fim da ligação de "
                            + request.callerName() + " em " + CALL_TIMEOUT_SECONDS
                            + "s — seguindo para a próxima mesmo assim.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                System.err.println("[Fila] Erro processando uma ligação: " + e.getMessage());
            }
        }
    }
}