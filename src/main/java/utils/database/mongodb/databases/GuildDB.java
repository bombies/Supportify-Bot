package utils.database.mongodb.databases;

import constants.Database;
import constants.ENV;
import main.Config;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
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
                .addField(Field.GUILD_PREFIX, Config.get(ENV.DEFAULT_PREFIX))
                .addField(Field.Tickets.INFO, new JSONObject()
                        .put(Field.Tickets.CREATOR_CHANNEL.toString(), -1L)
                        .put(Field.Tickets.CREATOR_MESSAGE.toString(), -1L)
                        .put(Field.Tickets.CREATOR_MESSAGE_DESCRIPTION.toString(), "")
                        .put(Field.Tickets.CREATOR_MESSAGE_EMOJI.toString(), -1L)
                        .put(Field.Tickets.MESSAGE_DESCRIPTION.toString(), "")
                        .put(Field.Tickets.SUPPORT_ROLE.toString(), -1L)
                        .put(Field.Tickets.LOG_CHANNEL.toString(), -1L)
                        .put(Field.Tickets.OPENED_TICKETS.toString(), new JSONArray())
                        .put(Field.Tickets.TOTAL_COUNT.toString(), 0)
                        .put(Field.Tickets.SUPPORT_TEAM_INFO.toString(), new JSONArray())
                        .put(Field.Tickets.BLACKLISTED_USERS.toString(), new JSONArray())
                )
                .addField(Field.PrivateChannels.PRIVATE_VOICE_CHANNELS, new JSONObject()
                        .put(Field.PrivateChannels.VC_CREATOR.toString(), -1L)
                        .put(Field.PrivateChannels.USER_CHANNELS.toString(), new JSONArray())
                )
                .build();
    }

    public static synchronized void update() {
        logger.debug("Updating Guild cache");
    }

    public enum Field implements GuildField {
        GUILD_ID("server_id"),
        GUILD_PREFIX("prefix");

        public enum Tickets implements GuildField {
            INFO("ticket_info"),
            CREATOR_CATEGORY("ticket_creator_category"),
            CREATOR_CHANNEL("ticket_creator_channel"),
            CREATOR_MESSAGE("ticket_creator_message"),
            CREATOR_MESSAGE_DESCRIPTION("ticket_creator_message_description"),
            CREATOR_MESSAGE_EMOJI("ticket_creator_message_emoji"),
            MESSAGE_DESCRIPTION("ticket_message_description"),
            SUPPORT_ROLE("ticket_support_role"),
            OPENED_TICKETS("opened_tickets"),
            OWNER("ticket_owner"),
            TIME_OPENED("time_opened"),
            ID("ticked_id"),
            CHANNEL("ticket_channel"),
            TOTAL_COUNT("total_ticket_count"),
            LOG_CHANNEL("log_channel"),
            SUPPORT_TEAM_INFO("support_team_info"),
            SUPPORT_USER_ID("user_id"),
            SUPPORT_CLOSES("num_of_closes"),
            SUPPORT_MESSAGES("num_of_messages"),
            BLACKLISTED_USERS("blacklisted_users");

            private final String str;

            Tickets(String str) {
                this.str = str;
            }

            @Override
            public String toString() {
                return str;
            }
        }

        public enum PrivateChannels implements GuildField {
            PRIVATE_VOICE_CHANNELS("private_voice_channels"),
            VC_CREATOR("vc_creator"),
            USER_CHANNELS("user_channel_info"),
            PVC_CHANNEL_NAME("channel_name"),
            PVC_WAITING_ROOM_NAME("waiting_room_name");

            private final String str;

            PrivateChannels(String str) {
                this.str = str;
            }

            @Override
            public String toString() {
                return str;
            }
        }

        private final String str;

        Field(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    public interface GuildField extends GenericJSONField { }
}
