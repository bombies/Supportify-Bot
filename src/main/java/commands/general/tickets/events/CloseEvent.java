package commands.general.tickets.events;

import commands.general.tickets.TicketCommand;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utils.SupportifyEmbedUtils;
import utils.json.tickets.TicketConfig;

import java.util.concurrent.TimeUnit;

public class CloseEvent extends ListenerAdapter {
    public static final int CLOSE_DELAY = 10;

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        if (!event.isFromGuild()) return;
        if (!event.isFromType(ChannelType.TEXT)) return;

        final var channelDeleted = (TextChannel) event.getChannel();
        final var config = new TicketConfig();
        final var guild = event.getGuild();

        if (!config.isOpenedTicket(event.getGuild().getIdLong(), channelDeleted.getIdLong()))
            return;

        config.closeTicket(event.getGuild().getIdLong(), channelDeleted.getIdLong());
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (!event.getButton().getId().equals(TicketCommand.CLOSE_BUTTON_ID)) return;

        final var guild = event.getGuild();
        final var config = new TicketConfig();
        final var channel = event.getTextChannel();

        if (!config.isOpenedTicket(guild.getIdLong(), channel.getIdLong())) {
            event.replyEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "This ticket has already been closed!").build())
                    .setEphemeral(true).queue();
            return;
        }

        final var closer = event.getUser();

        config.closeTicket(guild.getIdLong(), channel.getIdLong());
        event.replyEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "This ticket has been closed by: " + closer.getAsMention() + "\n" +
                "This channel will be deleted in "+CLOSE_DELAY+" seconds...").build())
                .queue(success -> {
                    channel.delete().queueAfter(CLOSE_DELAY, TimeUnit.SECONDS);

                    if (config.isSupportMember(guild.getIdLong(), closer.getIdLong())) {
                        config.incrementSupportMemberStats(guild.getIdLong(), closer.getIdLong(), TicketConfig.SupportStat.CLOSES, 1);
                        config.incrementSupportMemberStats(guild.getIdLong(), closer.getIdLong(), TicketConfig.SupportStat.MESSAGES, (int) getNumOfMessagesSentByUser(channel, closer));
                    }
                });
    }

    @SneakyThrows
    private long getNumOfMessagesSentByUser(TextChannel channel, User user) {
        return channel.getIterableHistory()
                .takeAsync(1000)
                .thenApply(list -> list.stream()
                        .filter(msg -> msg.getAuthor().getIdLong() == user.getIdLong())
                        .count()
                )
                .get();
    }
}
