package utils.database.mongodb.cache;

import lombok.Getter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.database.mongodb.databases.BotDB;

public class BotDBCache extends AbstractMongoCache {
    private final static Logger logger = LoggerFactory.getLogger(BotDBCache.class);
    @Getter
    private static BotDBCache instance;

    private BotDBCache() {
        super(BotDB.ins());
        this.init();
        updateCache();
        logger.debug("Done instantiating Bot Info cache");
    }

    public void setLastStartup(long time) {
        JSONObject jsonObject = getDocument();
        jsonObject.put(BotDB.Fields.LAST_BOOTED.toString(), time);
        update(jsonObject);
    }

    public long getLastStartup() {
        return getDocument().getLong(BotDB.Fields.LAST_BOOTED.toString());
    }

    private JSONObject getDocument() {
        return getCache().getJSONObject(0);
    }

    private void update(JSONObject jsonObject) {
        updateCache(jsonObject, "identifier", "robertify_main_config");
    }

    public static void initCache() {
        logger.debug("Instantiating new Bot Info cache");
        instance = new BotDBCache();
        logger.debug("BOT INFO CACHE = {}", instance.getCache());
    }

    public String getJSON(boolean indented) {
        return indented ? getCache().toString(4) : getCache().toString();
    }
}
