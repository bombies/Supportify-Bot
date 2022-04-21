package commands.general.privatevoicechannels.events;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utils.json.privatevoicechannels.PrivateChannelConfig;

public class PrivateChannelCreatorDeletionEvent extends ListenerAdapter {

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        if (!event.isFromType(ChannelType.VOICE)) return;

        final var config = new PrivateChannelConfig();
        final var guild = event.getGuild();

        if (!config.creatorIsSet(guild.getIdLong()))
            return;

        final var channel = (VoiceChannel) event.getChannel();

        if (config.getCreator(guild.getIdLong()) != channel.getIdLong())
            return;

        config.removeCreator(guild.getIdLong());
    }
}
