package commands.general.tickets.events;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utils.GeneralUtils;
import utils.json.tickets.Ticket;
import utils.json.tickets.TicketConfig;
import utils.json.tickets.TicketLogger;

public class TicketDeletionEvent extends ListenerAdapter {

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) return;

        final var config = new TicketConfig();
        final var channel = (TextChannel) event.getChannel();
        final var guild = event.getGuild();

        if (!config.isOpenedTicket(guild.getIdLong(), channel.getIdLong()))
            return;

        Ticket ticket = config.getTicket(guild.getIdLong(), channel.getIdLong());
        new TicketLogger(guild).sendLog(TicketLogger.LogType.TICKET_CLOSE, channel.getName() + " has been closed by an unknown closer\n" +
                "\nTime Opened: " + GeneralUtils.getDurationString(ticket.getTotalTimeOpened()) + "\n" +
                "Messages Sent: " + (ticket.getTotalMessageCount() - 1));
        config.closeTicket(guild.getIdLong(), channel.getIdLong());
    }
}
