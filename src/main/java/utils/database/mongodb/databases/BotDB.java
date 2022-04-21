package utils.database.mongodb.databases;

import constants.Database;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.database.mongodb.AbstractMongoDatabase;
import utils.database.mongodb.DocumentBuilder;
import utils.database.mongodb.cache.BotDBCache;
import utils.json.GenericJSONField;

public class BotDB extends AbstractMongoDatabase {
    private final static Logger logger = LoggerFactory.getLogger(BotDB.class);
    private final static BotDB INSTANCE = new BotDB();

    private BotDB() {
        super(Database.Mongo.SUPPORTIFY_DATABASE, Database.Mongo.SUPPORTIFY_BOT_DB);
    }

    @Override
    public synchronized void init() {
        if (getCollection().countDocuments() == 0) {
            addDocument(
                    DocumentBuilder.create()
                            .addField("identifier", "robertify_main_config")
                            .addField(Fields.LAST_BOOTED, 0L)
                            .build()
            );
        }
    }

    public static void update() {
        logger.debug("Updating Bot Info cache");
        var cache = BotDBCache.getInstance();

        for (var obj : cache.getCache()) {
            final JSONObject jsonObject = (JSONObject) obj;
            boolean changesMade = false;

            for (var fields : Fields.values()) {
                if (jsonObject.has(fields.toString()))
                    continue;

                changesMade = true;
                switch (fields) {
                    case LAST_BOOTED -> jsonObject.put(Fields.LAST_BOOTED.toString(), System.currentTimeMillis());
                }
            }

            if (changesMade) cache.updateCache(Document.parse(jsonObject.toString()));
        }
    }

    public static BotDB ins() {
        return INSTANCE;
    }

    public enum Fields implements GenericJSONField {
        LAST_BOOTED("last_booted");

        private final String str;

        Fields(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }
}
