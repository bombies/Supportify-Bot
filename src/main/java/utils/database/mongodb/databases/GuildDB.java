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
                .addField(Field.GUILD_PREFIX, Config.get(ENV.PREFIX))
                .addField(Field.TICKET_INFO, new JSONObject()
                        .put(Field.TICKET_CREATOR_CHANNEL.toString(), -1L)
                        .put(Field.TICKET_CREATOR_MESSAGE.toString(), -1L)
                        .put(Field.TICKET_CREATOR_MESSAGE_DESCRIPTION.toString(), "")
                        .put(Field.TICKET_CREATOR_MESSAGE_EMOJI.toString(), -1L)
                        .put(Field.TICKET_MESSAGE_DESCRIPTION.toString(), "")
                        .put(Field.TICKET_SUPPORT_ROLE.toString(), -1L)
                        .put(Field.OPENED_TICKETS.toString(), new JSONArray())
                        .put(Field.TICKET_TOTAL_COUNT.toString(), 0)
                        .put(Field.TICKET_SUPPORT_TEAM_INFO.toString(), new JSONArray())
                )
                .addField(Field.PRIVATE_VOICE_CHANNELS, new JSONObject()
                        .put(Field.PVC_VC_CREATOR.toString(), -1L)
                        .put(Field.PVC_USER_CHANNELS.toString(), new JSONArray())
                )
                .build();
    }

    public static synchronized void update() {
        logger.debug("Updating Guild cache");
    }

    public enum Field implements GenericJSONField {
        GUILD_ID("server_id"),
        GUILD_PREFIX("prefix"),

        // Tickets
        TICKET_INFO("ticket_info"),
        TICKET_CREATOR_CHANNEL("ticket_creator_channel"),
        TICKET_CREATOR_MESSAGE("ticket_creator_message"),
        TICKET_CREATOR_MESSAGE_DESCRIPTION("ticket_creator_message_description"),
        TICKET_CREATOR_MESSAGE_EMOJI("ticket_creator_message_emoji"),
        TICKET_MESSAGE_DESCRIPTION("ticket_message_description"),
        TICKET_SUPPORT_ROLE("ticket_support_role"),
        OPENED_TICKETS("opened_tickets"),
        TICKET_OWNER("ticket_owner"),
        TICKET_TIME_OPENED("time_opened"),
        TICKED_ID("ticked_id"),
        TICKET_CHANNEL("ticket_channel"),
        TICKET_TOTAL_COUNT("total_ticket_count"),
        TICKET_SUPPORT_TEAM_INFO("support_team_info"),
        TICKET_SUPPORT_USER_ID("user_id"),
        TICKET_SUPPORT_CLOSES("num_of_closes"),
        TICKET_SUPPORT_MESSAGES("num_of_messages"),

        // Private Voice Channels
        PRIVATE_VOICE_CHANNELS("private_voice_channels"),
        PVC_VC_CREATOR("vc_creator"),
        PVC_USER_CHANNELS("user_channel_info"),
        PVC_CHANNEL_NAME("channel_name"),
        PVC_WAITING_ROOM_NAME("waiting_room_name");

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
