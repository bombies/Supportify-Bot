package utils.database.mongodb.databases;

import constants.Database;
import constants.ENV;
import main.Config;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.database.mongodb.AbstractMongoDatabase;
import utils.database.mongodb.DocumentBuilder;
import utils.json.GenericJSONField;

public class GuildDB extends AbstractMongoDatabase {
    private final static Logger logger = LoggerFactory.getLogger(GuildDB.class);
    private static GuildDB INSTANCE;

    private GuildDB() {
        super(Database.Mongo.SUPPORTIFY_DATABASE, Database.Mongo.SUPPORTIFY_GUILDS);
    }

    @Override
    public synchronized void init() {

    }

    public synchronized void addGuild(long gid) {
        addDocument(getGuildDocument(gid));
    }

    public synchronized void removeGuild(long gid) {
        removeDocument(findSpecificDocument(Field.GUILD_ID, gid));
    }

    public static synchronized GuildDB ins() {
        if (INSTANCE == null)
            INSTANCE = new GuildDB();
        return INSTANCE;
    }

    public static Document getGuildDocument(long gid) {
        return DocumentBuilder.create()
                .addField(Field.GUILD_ID, gid)
                .addField(Field.GUILD_PREFIX, Config.get(ENV.PREFIX))
                .build();
    }

    public static synchronized void update() {
        logger.debug("Updating Guild cache");
    }

    public enum Field implements GenericJSONField {
        GUILD_ID("server_id"),
        GUILD_PREFIX("prefix");

        private final String str;

        Field(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }
}
