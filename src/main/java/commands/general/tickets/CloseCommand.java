package commands.general.tickets;

import main.Supportify;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import utils.GeneralUtils;
import utils.SupportifyEmbedUtils;
import utils.component.interactions.AbstractSlashCommand;
import utils.json.tickets.Ticket;
import utils.json.tickets.TicketConfig;
import utils.json.tickets.TicketLogger;
import utils.json.tickets.transcripts.TranscriptGenerator;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static commands.general.tickets.events.CloseEvent.CLOSE_DELAY;
import static commands.general.tickets.events.CloseEvent.getNumOfMessagesSentByUser;

public class CloseCommand extends AbstractSlashCommand {
    @Override
    protected void buildCommand() {
        setCommand(
                getBuilder()
                        .setName("close")
                        .setDescription("Close a ticket")
                        .setPermissionCheck(event -> {
                            final var config = new TicketConfig();
                            final var guild = event.getGuild();
                            return config.isSupportMember(guild.getIdLong(), event.getUser().getIdLong());
                        })
                        .setBotRequiredPermissions(Permission.MANAGE_CHANNEL)
                        .build()
        );
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!checks(event)) return;

        final var guild = event.getGuild();
        final var config = new TicketConfig();
        final var channel = event.getTextChannel();

        if (!config.isOpenedTicket(guild.getIdLong(), channel.getIdLong())) {
            event.replyEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "This command can only be executed in a ticket!").build())
                    .setEphemeral(true)
                    .queue();
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

        File transcript = new TranscriptGenerator(ticket).createTranscript();
        Supportify.getApi().getUserById(ticket.getOwner()).openPrivateChannel().queue(privChannel -> {
            privChannel.sendMessageEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "Your ticket (" + channel.getName() + ") has been closed by " + closer.getAsMention() + "\n" +
                    "\nTime Opened: " + GeneralUtils.getDurationString(ticket.getTotalTimeOpened()) + "\n" +
                    "Messages Sent: " + (ticket.getTotalMessageCount() - 1) + "\n\nYour transcript can be found below.").build())
                    .addFile(transcript)
                    .queue(success -> transcript.delete());
        }, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, ignored -> {}));
    }
}
