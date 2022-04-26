package commands.general.welcomer;

import main.Supportify;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import utils.SupportifyEmbedUtils;
import utils.component.interactions.AbstractSlashCommand;
import utils.json.welcomer.WelcomerConfig;

import java.util.List;

public class WelcomerCommand extends AbstractSlashCommand {
    @Override
    protected void buildCommand() {
        setCommand(
                getBuilder()
                        .setName("welcomer")
                        .setDescription("Configure the welcomer for your server")
                        .addSubCommands(
                                SubCommand.of(
                                        "toggle",
                                        "Toggle the welcomer on or off"
                                ),
                                SubCommand.of(
                                        "setchannel",
                                        "Set the channel for the welcomer to send messages to!",
                                        List.of(
                                                CommandOption.of(
                                                        OptionType.CHANNEL,
                                                        "channel",
                                                        "The channel to set",
                                                        true
                                                )
                                        )
                                ),
                                SubCommand.of(
                                        "edit",
                                        "Edit the welcomer"
                                )
                        )
                        .checkForPermissions(Permission.ADMINISTRATOR)
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

        switch (event.getSubcommandName()) {
            case "toggle" -> {
                final var guild = event.getGuild();
                final var config = new WelcomerConfig();

                if (!config.channelIsSet(guild.getIdLong())) {
                    event.replyEmbeds(SupportifyEmbedUtils.embedMessage("The welcomer channel must be set before" +
                                    " interacting with the toggler!").build())
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                if (config.isEnabled(guild.getIdLong())) {
                    config.setEnabled(guild.getIdLong(), false);
                    event.replyEmbeds(SupportifyEmbedUtils.embedMessage("You have toggled the welcomer **OFF**").build())
                            .queue();
                } else {
                    config.setEnabled(guild.getIdLong(), true);
                    event.replyEmbeds(SupportifyEmbedUtils.embedMessage("You have toggled the welcomer **ON**").build())
                            .queue();
                }
            }
            case "setchannel" -> {
                final var channel = event.getOption("channel").getAsTextChannel();
                final var config = new WelcomerConfig();
                config.setChannel(event.getGuild().getIdLong(), channel.getIdLong());
                event.replyEmbeds(SupportifyEmbedUtils.embedMessage("You have set the welcomer channel to: " + channel.getAsMention())
                                .build())
                        .queue();
            }
            case "edit" -> Supportify.getWelcomerConfigurator().sendMessage(event);
        }
    }
}
