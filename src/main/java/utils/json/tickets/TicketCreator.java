package utils.json.tickets;

import lombok.Getter;

public class TicketCreator {
    @Getter
    final long guildID, channelID, messageID, categoryID;
    @Getter
    final String messageDescription, emoji;

    protected TicketCreator(long guildID, long categoryID, long channelID, long messageID, String messageDescription, String emoji) {
        this.guildID = guildID;
        this.categoryID = categoryID;
        this.channelID = channelID;
        this.messageID = messageID;
        this.messageDescription = messageDescription;
        this.emoji = emoji;
    }
}
