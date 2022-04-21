package utils.json.privatevoicechannels;

import lombok.Getter;

public class PrivateVoiceChannel {
    @Getter
    private final long guildID, ownerID;
    @Getter
    private final String channelName, waitingRoomName;

    public PrivateVoiceChannel(long guildID, long ownerID, String channelName, String waitingRoomName) {
        this.guildID = guildID;
        this.ownerID = ownerID;
        this.channelName = channelName;
        this.waitingRoomName = waitingRoomName;
    }
}
