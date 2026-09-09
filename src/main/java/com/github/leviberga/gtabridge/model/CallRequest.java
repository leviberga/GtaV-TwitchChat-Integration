package com.github.leviberga.gtabridge.model;

/**
 * A single phone call to trigger in-game: who it's "from" and what they said.
 * Kept as a plain record so both the Twitch listener and any local/manual
 * trigger (Main's mock mode) produce the exact same shape of data.
 */
public record CallRequest(String callerName, String text) {
}