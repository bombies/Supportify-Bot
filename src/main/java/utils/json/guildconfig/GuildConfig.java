package utils.json.guildconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.database.mongodb.databases.GuildDB;
import utils.json.AbstractGuildConfig;

public class GuildConfig extends AbstractGuildConfig {
    private final static Logger logger = LoggerFactory.getLogger(GuildConfig.class);

    public void addGuild(long gid) {
        if (guildHasInfo(gid))
            throw new IllegalArgumentException("This guild is already added!");

        getDatabase().addGuild(gid);
    }

    public void removeGuild(long gid) {
        getDatabase().removeGuild(gid);
        if (!guildHasInfo(gid))
            logger.warn("There is no information for guild with ID {} in the cache.", gid);
        else
            unloadGuild(gid);
    }

    public String getPrefix(long gid) {
        if (!guildHasInfo(gid))
            loadGuild(gid);

        return (String) getCache().getField(gid, GuildDB.Field.GUILD_PREFIX);
    }

    public void setPrefix(long gid, String prefix) {
        if (!guildHasInfo(gid))
            loadGuild(gid);

        if (prefix.length() > 4)
            throw new IllegalArgumentException("The prefix must be 4 or less characters!");

        getCache().setField(gid, GuildDB.Field.GUILD_PREFIX, prefix);
    }

    @Override
    public void update(long gid) {

    }
}
