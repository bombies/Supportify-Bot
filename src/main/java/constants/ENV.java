package constants;

public enum ENV {
    BOT_TOKEN("bot_token"),
    OWNER_ID("owner_id"),
    BOT_COLOR("bot_color"),
    BOT_NAME("bot_name");

    private final String str;

    ENV(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}
