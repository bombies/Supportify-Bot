package commands.general.tickets.privatevoicechannels;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import utils.SupportifyEmbedUtils;
import utils.component.interactions.AbstractSlashCommand;
import utils.json.privatevoicechannels.PrivateChannelConfig;

public class PrivateChannelCommand extends AbstractSlashCommand {
    @Override
    protected void buildCommand() {
        setCommand(
                getBuilder()
                        .setName("setupprivatechannels")
                        .setDescription("Setup the private channel creator!")
                        .setPermissionCheck(event -> event.getMember().hasPermission(Permission.ADMINISTRATOR))
                        .setBotRequiredPermissions(Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)
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
        final var config = new PrivateChannelConfig();

        if (config.creatorIsSet(guild.getIdLong())) {
            event.replyEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Private Channels", "The private channel creator has already been setup!").build())
                    .setEphemeral(true)
                    .queue();

            return;
        }

        guild.createVoiceChannel("🔊 Create Private Channel").queue(voiceChannel -> {
            config.setCreator(event.getGuild().getIdLong(), voiceChannel.getIdLong());
            event.replyEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Private Channels", "You have successfully setup the private channel creator in " + voiceChannel.getAsMention()).build())
                    .queue();
        });
    }
}
