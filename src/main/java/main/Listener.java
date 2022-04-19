package main;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener extends ListenerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(Listener.class);

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        final var jda = event.getJDA();

        for (Guild g : jda.getGuildCache()) {
            loadNeededSlashCommands(g);
            unloadCommands(g);
        }

        logger.info("Watching {} guilds", jda.getGuildCache().size());
        Supportify.getApi().getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching("/help"));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;

        final var user = event.getAuthor();
        final var guild = event.getGuild();
        final String prefix = ">"; // TODO Change prefix to be loaded from database

        final var msg = event.getMessage();
        final var raw = msg.getContentRaw();

        if (user.isBot() || event.isWebhookMessage()) return;

        if (raw.startsWith(prefix) && raw.length() > prefix.length()) {
            try {
                // TODO Command Handling
            } catch (InsufficientPermissionException e) {
                try {
                    event.getChannel().sendMessage("""
                                            ⚠️ I don't have permission to send embeds!

                                            Please tell an admin to enable the `Embed Links` permission for my role in this channel in order for my commands to work!"""
                            )
                            .queue();
                } catch (InsufficientPermissionException ignored) {}
            }
        } else if (!msg.getMentionedMembers().isEmpty() && raw.split(" ").length == 1) {
            if (!raw.startsWith("<@!"+guild.getSelfMember().getId()+">"))
                return;

            try {

            } catch (InsufficientPermissionException e) {
                if (!e.getMessage().contains("EMBED")) return;

                msg.reply("Hey " + event.getAuthor().getAsMention() + "! Thank you for using Supportify. :)\n" +
                        "My prefix in this server is: `" + prefix + "`\n\n" +
                        "Type `" + prefix + "help` to see all the commands I offer!\n").queue();
            }
        }
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        final var guild = event.getGuild();
        loadSlashCommands(guild);

        logger.info("Joined {}", guild.getName());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        final var guild = event.getGuild();

        logger.info("Left {}", guild.getName());
    }

    public void loadSlashCommands(Guild g) {

    }

    public void loadNeededSlashCommands(Guild g) {

    }

    public void unloadCommands(Guild g) {

    }
}
