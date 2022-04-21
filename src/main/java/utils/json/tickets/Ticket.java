package utils.json.tickets;

import lombok.Getter;
import lombok.SneakyThrows;
import main.Supportify;

import java.util.List;

public class Ticket {
    @Getter
    final long owner, timeOpened, channelID, guildID;
    @Getter
    final int id;

    protected Ticket(long guildID, long owner, long timeOpened, int id, long channelID) {
        this.guildID = guildID;
        this.owner = owner;
        this.timeOpened = timeOpened;
        this.id = id;
        this.channelID = channelID;
    }

    public boolean isOwner(long id) {
        return owner == id;
    }

    public long getTotalTimeOpened() {
        return System.currentTimeMillis() - timeOpened;
    }

    @SneakyThrows
    public int getTotalMessageCount() {
        return Supportify.getApi().getTextChannelById(channelID)
                .getIterableHistory().takeAsync(1000)
                .thenApply(List::size)
                .get();
    }
}
