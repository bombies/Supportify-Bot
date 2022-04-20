package constants;

public enum ENV {
    BOT_TOKEN("bot_token"),
    OWNER_ID("owner_id"),
    DEFAULT_PREFIX("prefix"),
    BOT_COLOR("bot_color"),
    BOT_NAME("bot_name"),
    MONGO_DATABASE_NAME("mongo_database_name"),
    MONGO_CLUSTER_NAME("mongo_cluster_name"),
    MONGO_USERNAME("mongo_username"),
    MONGO_PASSWORD("mongo_password"),
    MONGO_HOSTNAME("mongo_hostname");

    private final String str;

    ENV(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}
