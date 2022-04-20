package constants;

import main.Config;

public enum BotConstants {
    EMBED_TITLE(Config.get(ENV.BOT_NAME)),
    EMBED_LOGO("https://cdn-icons-png.flaticon.com/512/174/174872.png"); // TODO Change

    private final String str;

    BotConstants(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}
