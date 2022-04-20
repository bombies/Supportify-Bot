package utils.json.tickets;

import lombok.Getter;

public class SupportTeamMember {
    @Getter
    final long userID, guildID;
    @Getter
    final int numOfCloses, numOfMessages;

    protected SupportTeamMember(long guildID, long userID, int numOfCloses, int numOfMessages) {
        this.guildID = guildID;
        this.userID = userID;
        this.numOfCloses = numOfCloses;
        this.numOfMessages = numOfMessages;
    }
}
