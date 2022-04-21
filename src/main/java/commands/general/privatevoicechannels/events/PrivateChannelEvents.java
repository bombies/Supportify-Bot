package commands.general.privatevoicechannels.events;

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utils.json.privatevoicechannels.PrivateChannelConfig;

public class PrivateChannelEvents extends ListenerAdapter {

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        new PrivateChannelCreatorEvents().deletion(event);
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        new PrivateChannelCreatorEvents().deletion(event);

        final var config = new PrivateChannelConfig();
        final var guild = event.getGuild();

        if (!config.creatorIsSet(guild.getIdLong())) return;
        if (event.getChannelJoined().getIdLong() != config.getCreator(guild.getIdLong())) return;

        new PrivateChannelCreatorEvents().channelCreation(event, ((VoiceChannel)event.getChannelJoined()).getParentCategory());
    }

    @Override
    public void onChannelUpdateName(@NotNull ChannelUpdateNameEvent event) {
        final var config = new PrivateChannelConfig();
        final var guild = event.getGuild();
        final var channel = event.getChannel();
        final var creatorEvents = new PrivateChannelCreatorEvents();

        if (!config.creatorIsSet(guild.getIdLong())) return;
        if (!creatorEvents.isPrivateRoom(channel.getIdLong())) return;

        final String newName = event.getNewValue();
        final var channelOwner = creatorEvents.getPrivateRoomOwner(channel.getIdLong());
        final var oldChannel = config.getChannelInfo(guild.getIdLong(), channelOwner);

        if (creatorEvents.channelIsPrivateChannel(channel.getIdLong()))
            config.updateChannelInfo(guild.getIdLong(), channelOwner, PrivateChannelConfig.RoomType.MAIN_ROOM, newName);
        else
            config.updateChannelInfo(guild.getIdLong(), channelOwner, PrivateChannelConfig.RoomType.WAITING_ROOM, newName);

    }
}
