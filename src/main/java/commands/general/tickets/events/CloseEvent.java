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
import utils.GeneralUtils;
import utils.SupportifyEmbedUtils;
import utils.json.tickets.Ticket;
import utils.json.tickets.TicketConfig;
import utils.json.tickets.TicketLogger;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Override @SneakyThrows
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

        Ticket ticket = config.getTicket(guild.getIdLong(), channel.getIdLong());
        new TicketLogger(guild).sendLog(TicketLogger.LogType.TICKET_CLOSE, channel.getName() + " has been closed by " + closer.getAsMention() + "\n" +
                "\nTime Opened: " + GeneralUtils.getDurationString(ticket.getTotalTimeOpened()) + "\n" +
                "Messages Sent: " + (ticket.getTotalMessageCount() - 1));

        config.closeTicket(guild.getIdLong(), channel.getIdLong());
        event.replyEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "This ticket has been closed by: " + closer.getAsMention() + "\n" +
                "This channel will be deleted in " + CLOSE_DELAY + " seconds...").build())
                .queue(success -> {
                    channel.delete().queueAfter(CLOSE_DELAY, TimeUnit.SECONDS);

                    if (config.isSupportMember(guild.getIdLong(), closer.getIdLong())) {
                        config.incrementSupportMemberStats(guild.getIdLong(), closer.getIdLong(), TicketConfig.SupportStat.CLOSES, 1);
                        config.incrementSupportMemberStats(guild.getIdLong(), closer.getIdLong(), TicketConfig.SupportStat.MESSAGES, getNumOfMessagesSentByUser(channel, closer));
                    }
                });
    }

    @SneakyThrows
    private int getNumOfMessagesSentByUser(TextChannel channel, User user) {
        return channel.getIterableHistory()
                .takeAsync(1000)
                .thenApply(list -> list.stream()
                        .filter(msg -> msg.getAuthor().getIdLong() == user.getIdLong())
                        .toList()
                        .size()
                )
                .get();
    }
}
