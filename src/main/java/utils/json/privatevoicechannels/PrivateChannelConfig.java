package utils.json.privatevoicechannels;

import lombok.SneakyThrows;
import org.json.JSONObject;
import utils.database.mongodb.databases.GuildDB;
import utils.json.AbstractGuildConfig;

import java.rmi.UnexpectedException;

public class PrivateChannelConfig extends AbstractGuildConfig {

    public void setCreator(long gid, long cid) {
        final var obj = getGuildObject(gid);
        final var pvcObj = obj.getJSONObject(GuildDB.Field.PrivateChannels.PRIVATE_VOICE_CHANNELS.toString());
        pvcObj.put(GuildDB.Field.PrivateChannels.VC_CREATOR.toString(), cid);

        getCache().setField(gid, GuildDB.Field.PrivateChannels.PRIVATE_VOICE_CHANNELS, pvcObj);
    }

    public boolean creatorIsSet(long gid) {
        final var obj = getGuildObject(gid);
        final var pvcObj = obj.getJSONObject(GuildDB.Field.PrivateChannels.PRIVATE_VOICE_CHANNELS.toString());
        return pvcObj.getLong(GuildDB.Field.PrivateChannels.VC_CREATOR.toString()) != -1L;
    }

    public long getCreator(long gid) {
        if (!creatorIsSet(gid))
            throw new IllegalStateException("The private voice channel creator has not been set for this guild!");

        final var obj = getGuildObject(gid);
        final var pvcObj = obj.getJSONObject(GuildDB.Field.PrivateChannels.PRIVATE_VOICE_CHANNELS.toString());
        return pvcObj.getLong(GuildDB.Field.PrivateChannels.VC_CREATOR.toString());
    }

    public void removeCreator(long gid) {
        setCreator(gid, -1L);
    }

    public void addChannel(long gid, long uid, String roomName, String waitingRoomName) {
        if (hasChannelInfo(gid, uid))
            throw new IllegalArgumentException("This user already has channel information in this guild!");

        final var obj = getGuildObject(gid);
        final var pvcObj = obj.getJSONObject(GuildDB.Field.PrivateChannels.PRIVATE_VOICE_CHANNELS.toString());
        final var userChannelInfoArr = pvcObj.getJSONArray(GuildDB.Field.PrivateChannels.USER_CHANNELS.toString());

        userChannelInfoArr.put(new JSONObject()
                .put(GuildDB.Field.PrivateChannels.USER_ID.toString(), uid)
                .put(GuildDB.Field.PrivateChannels.CHANNEL_NAME.toString(), roomName)
                .put(GuildDB.Field.PrivateChannels.WAITING_ROOM_NAME.toString(), waitingRoomName)
        );

        getCache().setField(gid, GuildDB.Field.PrivateChannels.PRIVATE_VOICE_CHANNELS, pvcObj);
    }

    public boolean hasChannelInfo(long gid, long uid) {
        final var obj = getGuildObject(gid);
        final var pvcObj = obj.getJSONObject(GuildDB.Field.PrivateChannels.PRIVATE_VOICE_CHANNELS.toString());
        final var userChannelInfoArr = pvcObj.getJSONArray(GuildDB.Field.PrivateChannels.USER_CHANNELS.toString());
        return arrayHasObject(userChannelInfoArr, GuildDB.Field.PrivateChannels.USER_ID, uid);
    }

    @SneakyThrows
    public void updateChannelInfo(long gid, long uid, RoomType roomType, String name) {
        final var obj = getGuildObject(gid);
        final var pvcObj = obj.getJSONObject(GuildDB.Field.PrivateChannels.PRIVATE_VOICE_CHANNELS.toString());
        final var userChannelInfoArr = pvcObj.getJSONArray(GuildDB.Field.PrivateChannels.USER_CHANNELS.toString());
        final var userInfoObj = userChannelInfoArr.getJSONObject(getIndexOfObjectInArray(userChannelInfoArr, GuildDB.Field.PrivateChannels.USER_ID, uid));

        GuildDB.Field.PrivateChannels fieldToUse;
        switch (roomType) {
            case MAIN_ROOM -> fieldToUse = GuildDB.Field.PrivateChannels.CHANNEL_NAME;
            case WAITING_ROOM -> fieldToUse = GuildDB.Field.PrivateChannels.WAITING_ROOM_NAME;
            default -> throw new UnexpectedException("An unexpected error has occurred!");
        }

        userInfoObj.put(fieldToUse.toString(), name);
        getCache().setField(gid, GuildDB.Field.PrivateChannels.PRIVATE_VOICE_CHANNELS, pvcObj);
    }

    public PrivateVoiceChannel getChannelInfo(long gid, long uid) {
        final var obj = getGuildObject(gid);
        final var pvcObj = obj.getJSONObject(GuildDB.Field.PrivateChannels.PRIVATE_VOICE_CHANNELS.toString());
        final var userChannelInfoArr = pvcObj.getJSONArray(GuildDB.Field.PrivateChannels.USER_CHANNELS.toString());
        final var userInfoObj = userChannelInfoArr.getJSONObject(getIndexOfObjectInArray(userChannelInfoArr, GuildDB.Field.PrivateChannels.USER_ID, uid));
        return new PrivateVoiceChannel(
                gid,
                uid,
                userInfoObj.getString(GuildDB.Field.PrivateChannels.CHANNEL_NAME.toString()),
                userInfoObj.getString(GuildDB.Field.PrivateChannels.WAITING_ROOM_NAME.toString())
        );
    }

    public enum RoomType {
        MAIN_ROOM,
        WAITING_ROOM
    }

    @Override
    public void update(long gid) {

    }
}
