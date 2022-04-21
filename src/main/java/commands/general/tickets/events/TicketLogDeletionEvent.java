package commands.general.tickets.events;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utils.json.tickets.TicketConfig;

public class TicketLogDeletionEvent extends ListenerAdapter {

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) return;

        final var config = new TicketConfig();
        final var guild = event.getGuild();
        final var channel = (TextChannel) event.getChannel();

        if (config.getLogChannel(guild.getIdLong()) == -1L)
            return;

        if (config.getLogChannel(guild.getIdLong()) != channel.getIdLong())
            return;

        config.setLogChannel(guild.getIdLong(), -1L);
    }
}
