package main;

import constants.ENV;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Config {
    private final static Logger logger = LoggerFactory.getLogger(Config.class);
    private static Dotenv dotenv = Dotenv.load();

    /**
     * Get a string value from its specific key from the .env file
     * @param key .env key to retrieve.
     * @return The string attached to the key
     */
    public static String get(ENV key) {
        return dotenv.get(key.toString().toUpperCase());
    }

    /**
     * Reload the .env file to use the new values if it was updated after compilation and execution
     */
    public static void reload() {
        dotenv = Dotenv.load();
    }

    public static String getBotToken() {
        return get(ENV.BOT_TOKEN);
    }

    public static long getOwnerID() {
        return getLong(ENV.OWNER_ID);
    }

    private static int getInt(ENV key) {
        return Integer.parseInt(get(key));
    }

    private static long getLong(ENV key) {
        return Long.parseLong(get(key));
    }
}
