package commands.general.tickets.events;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utils.json.tickets.TicketConfig;

public class TicketCreatorDeletionEvent extends ListenerAdapter {

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) return;

        final var config = new TicketConfig();
        final var channel = (TextChannel) event.getChannel();
        final var guild = event.getGuild();

        if (!config.creatorExists(guild.getIdLong()))
            return;

        if (config.getCreator(guild.getIdLong()).getChannelID() != channel.getIdLong())
            return;

        config.removeCreator(guild.getIdLong());
    }


}
