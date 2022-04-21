package commands.general.tickets.events;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utils.json.tickets.TicketConfig;
import utils.json.tickets.TicketCreator;

public class TicketCreatorCategoryDeletionEvent extends ListenerAdapter {

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        if (!event.isFromType(ChannelType.CATEGORY)) return;

        final var category = (Category) event.getChannel();
        final var config = new TicketConfig();
        final var guild = event.getGuild();

        if (!config.creatorCategoryExists(guild.getIdLong()))
            return;

        if (config.getTicketCreatorCategory(guild.getIdLong()) != category.getIdLong())
            return;

        config.removeCreatorCategory(guild.getIdLong());

        if (!config.creatorExists(guild.getIdLong()))
            return;

        TicketCreator creator = config.getCreator(guild.getIdLong());
        guild.getTextChannelById(creator.getChannelID()).delete().queue();

        if (config.getLogChannel(event.getGuild().getIdLong()) == -1L)
            return;

        guild.getTextChannelById(config.getLogChannel(event.getGuild().getIdLong())).delete().queue();
    }
}
