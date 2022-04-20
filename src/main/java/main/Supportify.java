package main;

import commands.SlashCommandManager;
import commands.general.tickets.events.CloseEvent;
import constants.ENV;
import lombok.Getter;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.GeneralUtils;
import utils.database.mongodb.AbstractMongoDatabase;
import utils.database.mongodb.cache.GuildDBCache;

public class Supportify {
    private static final Logger logger = LoggerFactory.getLogger(Supportify.class);

    @Getter
    private static JDA api;

    public static void main(String[] args) {
        WebUtils.setUserAgent("Mozilla/Supportify / bombies#4445");

        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(
                    Config.get(ENV.BOT_TOKEN),
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.GUILD_BANS,
                            GatewayIntent.GUILD_INVITES,
                            GatewayIntent.GUILD_MESSAGES

            )
                    .setBulkDeleteSplittingEnabled(false)
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .setMemberCachePolicy(MemberCachePolicy.NONE)
                    .addEventListeners(
                            new Listener(),
                            new CloseEvent()
                    )
                    .disableCache(
                            CacheFlag.VOICE_STATE,
                            CacheFlag.ACTIVITY,
                            CacheFlag.EMOTE,
                            CacheFlag.CLIENT_STATUS,
                            CacheFlag.ROLE_TAGS,
                            CacheFlag.ONLINE_STATUS
                    )
                    .disableIntents(
                            GatewayIntent.DIRECT_MESSAGE_TYPING,
                            GatewayIntent.GUILD_MESSAGE_TYPING,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.DIRECT_MESSAGE_REACTIONS
                    )
                    .setGatewayEncoding(GatewayEncoding.ETF)
                    .setActivity(Activity.playing("Starting up..."));

            SlashCommandManager slashCommandManager = new SlashCommandManager();
            slashCommandManager.getCommands().forEach(jdaBuilder::addEventListeners);
            slashCommandManager.getDevCommands().forEach(jdaBuilder::addEventListeners);

            AbstractMongoDatabase.initAllCaches();
            logger.info("Initialized all caches");

            GuildDBCache.getInstance().loadAllGuilds();
            logger.info("All guilds have been loaded into cache");

            GeneralUtils.setDefaultEmbed();

            api = jdaBuilder.build();
        } catch (Exception e) {
            logger.error("An unexpected error occurred!", e);
        }
    }
}
