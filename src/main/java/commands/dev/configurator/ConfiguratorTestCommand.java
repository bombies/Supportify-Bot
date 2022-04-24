package commands.dev.configurator;

import main.Supportify;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import utils.component.interactions.AbstractSlashCommand;

public class ConfiguratorTestCommand extends AbstractSlashCommand {
    @Override
    protected void buildCommand() {
        setCommand(
                getBuilder()
                        .setName("configuratortest")
                        .setDescription("This is a test for the configurator command")
                        .setDevCommand()
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

        TestConfigurator testConfigurator = Supportify.getTestConfigurator();
        testConfigurator.sendMessage(event);
    }
}
