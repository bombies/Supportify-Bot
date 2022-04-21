package commands.general.privatevoicechannels.events;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import utils.SupportifyEmbedUtils;
import utils.json.privatevoicechannels.PrivateChannelConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PrivateChannelCreatorEvents extends ListenerAdapter {
    private final static Map<Long, Pair<Long, Long>> privateVcs = new HashMap<>();

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {

        final var config = new PrivateChannelConfig();
        final var guild = event.getGuild();

        if (!config.creatorIsSet(guild.getIdLong())) return;
        if (event.getChannelJoined().getIdLong() != config.getCreator(guild.getIdLong())) return;

        Category parentCategory = ((VoiceChannel) event.getChannelJoined()).getParentCategory();
        channelCreation(event, parentCategory);
    }

    protected void channelCreation(GenericGuildVoiceEvent event, Category cat) {
        if (cat == null) {
            final var member = event.getMember();
            member.getUser().openPrivateChannel().queue(channel -> channel.sendMessageEmbeds(
                    SupportifyEmbedUtils.embedMessageWithAuthor("Private Channels",
                            "Hey " + member.getAsMention() + ",\n\n" +
                                    "Unfortunately I was not able to create a private voice channel for you. " +
                                    "Please inform a server admin that the private voice channel creator **needs** " +
                                    "to be in a category before it can be used.\n\n" +
                                    "Thank you."
                    ).build()
            ).queue());
            return;
        }

        final Guild guild = event.getGuild();
        final Member target = event.getMember();
        final var config = new PrivateChannelConfig();
        final var channel = config.getChannelInfo(event.getGuild().getIdLong(), target.getIdLong());

        guild.createVoiceChannel(channel.getChannelName(), cat).queue(privateVc -> {
            guild.createVoiceChannel(channel.getWaitingRoomName(), cat).queue(waitingRoom ->{
                final Consumer<ErrorResponseException> restErrorHandling = e -> {
                    privateVc.delete().queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_CHANNEL)
                    );
                    waitingRoom.delete().queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_CHANNEL)
                    );
                };

                privateVc.upsertPermissionOverride(target)
                        .setAllow(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS,
                                Permission.VOICE_SPEAK, Permission.VOICE_STREAM, Permission.MANAGE_CHANNEL)
                        .queue(null, new ErrorHandler()
                                .handle(ErrorResponse.UNKNOWN_CHANNEL, restErrorHandling)
                        );

                privateVc.upsertPermissionOverride(guild.getPublicRole())
                        .setDeny(Permission.VOICE_CONNECT)
                        .queue(null, new ErrorHandler()
                                .handle(ErrorResponse.UNKNOWN_CHANNEL, restErrorHandling)
                        );

                waitingRoom.upsertPermissionOverride(target)
                        .setAllow(Permission.VIEW_CHANNEL, Permission.VOICE_MOVE_OTHERS)
                        .setDeny(Permission.VOICE_CONNECT)
                        .queue(null, new ErrorHandler()
                                .handle(ErrorResponse.UNKNOWN_CHANNEL, restErrorHandling)
                        );

                if (privateVcs.containsKey(target.getIdLong()))
                    if (privateVcs.get(target.getIdLong()) != null)
                        channelDeletion(target.getGuild(), privateVcs.get(target.getIdLong()), target.getIdLong());
                privateVcs.put(target.getIdLong(), Pair.of(privateVc.getIdLong(), waitingRoom.getIdLong()));

                guild.moveVoiceMember(target, privateVc)
                        .queue(null, new ErrorHandler()
                                .handle(ErrorResponse.USER_NOT_CONNECTED, (e) -> {
                                    guild.getVoiceChannelById(privateVcs.get(target.getIdLong()).getLeft()).delete().queue();
                                    guild.getVoiceChannelById(privateVcs.get(target.getIdLong()).getRight()).delete().queue();
                                    privateVcs.remove(target.getId());
                                })
                        );
            });
        });
    }

    protected void deletion(GenericGuildVoiceUpdateEvent event) {
        final var config = new PrivateChannelConfig();
        if (config.creatorIsSet(event.getGuild().getIdLong())) {
            boolean privateVc = false;
            Long ownerID = null;
            Pair<Long, Long> channelIds = null;

            for (var entry : privateVcs.entrySet()) {
                if (event.getChannelLeft().getIdLong() == entry.getValue().getLeft()) {
                    privateVc = true;
                    ownerID = entry.getKey();
                    channelIds = entry.getValue();
                    break;
                }
            }

            try {
                channelDeletion(event, privateVc, channelIds, ownerID);
            } catch (NullPointerException ignored) { }
        }
    }

    protected static void channelDeletion(Guild guild, Pair<Long, Long> channelIds, Long ownerID) {
        guild.getVoiceChannelById(channelIds.getLeft()).delete().queue(null, new ErrorHandler()
                .ignore(ErrorResponse.UNKNOWN_CHANNEL)
        );
        guild.getVoiceChannelById(channelIds.getRight()).delete().queue(null, new ErrorHandler()
                .ignore(ErrorResponse.UNKNOWN_CHANNEL)
        );
        privateVcs.remove(ownerID);
    }

    protected void channelDeletion(GenericGuildVoiceUpdateEvent event, boolean privateVc, Pair<Long, Long> channelIds, long ownerID) {
        if (!(event.getChannelLeft().getType().equals(ChannelType.VOICE))) return;

        if (privateVc && event.getChannelLeft().getMembers().size() == 0) {
            final Guild guild = event.getGuild();
            guild.getVoiceChannelById(channelIds.getLeft()).delete().queue(null, new ErrorHandler()
                    .handle(ErrorResponse.UNKNOWN_CHANNEL, ignored -> {}));
            guild.getVoiceChannelById(channelIds.getRight()).delete().queue(null, new ErrorHandler()
                    .handle(ErrorResponse.UNKNOWN_CHANNEL, ignored -> {}));
            privateVcs.remove(ownerID);
        } else if (!privateVc && event.getChannelJoined().getIdLong() == new PrivateChannelConfig().getCreator(event.getGuild().getIdLong())) {
            Category cat = ((VoiceChannel) event.getChannelLeft()).getParentCategory();
            channelCreation(event, cat);
        }
    }

    public boolean channelIsPrivateChannel(long id) {
        for (var key : privateVcs.keySet())
            if (privateVcs.get(key).getLeft() == id)
                return true;
        return false;
    }

    public boolean channelIsWaitingRoom(long id) {
        for (var key : privateVcs.keySet())
            if (privateVcs.get(key).getRight() == id)
                return true;
        return false;
    }

    public boolean isPrivateRoom(long id) {
        return channelIsPrivateChannel(id) || channelIsWaitingRoom(id);
    }

    public long getPrivateRoomOwner(long channelId) {
        if (!isPrivateRoom(channelId))
            throw new IllegalArgumentException("That ID doesn't belong to a valid private room!");

        for (var key : privateVcs.keySet()) {
            final var channels = privateVcs.get(key);
            if (channels.getLeft() == channelId || channels.getRight() == channelId)
                return key;
        }

        return -1;
    }
}
