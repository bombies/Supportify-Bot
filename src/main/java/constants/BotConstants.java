package constants;

import main.Config;

public enum BotConstants {
    EMBED_TITLE(Config.get(ENV.BOT_NAME)),
    EMBED_LOGO(Config.get(ENV.BOT_LOGO));

    private final String str;

    BotConstants(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}
