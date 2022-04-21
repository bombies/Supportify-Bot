package commands.misc;

import main.Supportify;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import utils.SupportifyEmbedUtils;
import utils.component.interactions.AbstractSlashCommand;

public class PingCommand extends AbstractSlashCommand {
    @Override
    protected void buildCommand() {
        setCommand(
                getBuilder()
                        .setName("Ping")
                        .setDescription("Check the ping of the bot to discord's servers!")
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

        Supportify.getApi().getRestPing().queue(
                (ping) -> {
                    EmbedBuilder eb = SupportifyEmbedUtils.embedMessage("🏓 Pong!\n\n" +
                            "REST Ping: **"+ping+"ms**\n" +
                            "Websocket Ping: **"+Supportify.getApi().getGatewayPing()+"ms**");
                    event.replyEmbeds(eb.build()).queue();
                }
        );
    }
}
