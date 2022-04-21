package commands.utility;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import utils.GeneralUtils;
import utils.SupportifyEmbedUtils;
import utils.component.interactions.AbstractSlashCommand;
import utils.database.mongodb.cache.BotDBCache;

public class UptimeCommand extends AbstractSlashCommand {
    @Override
    protected void buildCommand() {
        setCommand(
                getBuilder()
                        .setName("uptime")
                        .setDescription("Get how long the bot has been online")
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

        event.replyEmbeds(
                SupportifyEmbedUtils.embedMessage(
                        GeneralUtils.getDurationString(System.currentTimeMillis() - BotDBCache.getInstance().getLastStartup())
                ).build()
        ).setEphemeral(false).queue();
    }
}
