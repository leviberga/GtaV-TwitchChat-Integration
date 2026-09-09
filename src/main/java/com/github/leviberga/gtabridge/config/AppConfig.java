package com.github.leviberga.gtabridge.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Central place to read configuration. Checks a real OS environment variable
 * first, and falls back to the local .env file otherwise.
 */
public class AppConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing() // não quebra se não houver .env (ex.: variáveis já vêm do SO)
            .load();

    public static String twitchOAuthToken() {
        return require("TWITCH_OAUTH_TOKEN");
    }

    public static String twitchChannel() {
        return require("TWITCH_CHANNEL");
    }

    private static String require(String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = dotenv.get(key);
        }
        if (value == null) {
            throw new IllegalStateException("Variável obrigatória não definida: " + key
                    + " (defina no .env ou como variável de ambiente)");
        }
        return value;
    }
}