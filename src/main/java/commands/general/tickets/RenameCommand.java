package commands.general.tickets;

import commands.CommandContext;
import commands.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import utils.SupportifyEmbedUtils;
import utils.component.interactions.AbstractSlashCommand;
import utils.json.tickets.TicketConfig;
import utils.json.tickets.TicketLogger;

import java.util.concurrent.TimeUnit;

public class RenameCommand extends AbstractSlashCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {

    }

    @Override
    public String getHelp(String prefix) {
        return null;
    }

    @Override
    public String getName() {
        return "rename";
    }

    @Override
    protected void buildCommand() {
        setCommand(
                getBuilder()
                        .setName(getName())
                        .setDescription("Rename a ticket")
                        .addOptions(
                                CommandOption.of(
                                        OptionType.STRING,
                                        "name",
                                        "The name to set the ticket as",
                                        true
                                )
                        )
                        .setPermissionCheck(event -> {
                            final var config = new TicketConfig();
                            final var guild = event.getGuild();

                            if (!config.creatorExists(guild.getIdLong()))
                                return false;
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

        final var config = new TicketConfig();
        final var channel = event.getTextChannel();
        final var guild = event.getGuild();

        if (!config.isOpenedTicket(guild.getIdLong(), channel.getIdLong())) {
            event.replyEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "This command can only be executed in tickets!").build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        final var oldName = channel.getName();
        final var name = event.getOption("name").getAsString();
        channel.getManager().setName(name).queue(success -> {
            event.replyEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "The ticket has been renamed to: " + name).build())
                    .queue(msg -> {
                        new TicketLogger(guild).sendLog(TicketLogger.LogType.TICKET_RENAME,
                                event.getUser().getAsMention() + " has renamed " + oldName + " to " + name
                        );

                        msg.deleteOriginal().queueAfter(5, TimeUnit.SECONDS);
                    });
        });
    }
}
