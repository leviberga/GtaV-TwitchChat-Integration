package com.github.leviberga.gtabridge.twitch;

import com.github.leviberga.gtabridge.model.CallRequest;
import com.github.leviberga.gtabridge.queue.CallQueue;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

/**
 * Listens to Twitch chat and turns "!ligar <mensagem>" messages into
 * CallRequests, handing each one to the CallQueue. Knows nothing
 * about TTS or WebSockets, just Twitch and command parsing.
 */
public class TwitchChatListener {

    private static final String CALL_COMMAND = "!ligar ";

    private final CallQueue callQueue;
    private TwitchClient twitchClient;

    public TwitchChatListener(CallQueue callQueue) {
        this.callQueue = callQueue;
    }

    public void start(String oauthToken, String channelName) {
        OAuth2Credential credential = new OAuth2Credential("twitch", oauthToken);

        twitchClient = TwitchClientBuilder.builder()
                .withEnableChat(true)
                .withChatAccount(credential)
                .build();

        twitchClient.getChat().joinChannel(channelName);
        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, this::onChatMessage);

        System.out.println("-> Bot da Twitch iniciado com sucesso no canal: " + channelName);
    }

    private void onChatMessage(ChannelMessageEvent event) {
        String message = event.getMessage().trim();
        String user = event.getUser().getName();

        if (!message.startsWith(CALL_COMMAND)) {
            return;
        }

        String speechText = message.substring(CALL_COMMAND.length()).trim();
        if (speechText.isEmpty()) {
            return;
        }

        callQueue.enqueue(new CallRequest(user, speechText));
    }
}