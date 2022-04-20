package utils.json.tickets;

import lombok.Getter;

public class TicketCreator {
    @Getter
    final long guildID, channelID, messageID;
    @Getter
    final String messageDescription, emoji;

    protected TicketCreator(long guildID, long channelID, long messageID, String messageDescription, String emoji) {
        this.guildID = guildID;
        this.channelID = channelID;
        this.messageID = messageID;
        this.messageDescription = messageDescription;
        this.emoji = emoji;
    }
}
