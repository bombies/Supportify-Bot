package commands.general.welcomer;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import utils.component.interactions.AbstractSlashCommand;

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
                                )
                        )
                        .build()
        );
    }

    @Override
    public String getHelp() {
        return null;
    }
}
