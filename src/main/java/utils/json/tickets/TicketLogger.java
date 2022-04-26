package utils.json.tickets;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import utils.GeneralUtils;
import utils.SupportifyEmbedUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.time.Instant;

public class TicketLogger {
    final Guild guild;
    final TicketConfig config;

    public TicketLogger(Guild guild) {
        this.guild = guild;
        this.config = new TicketConfig();
    }

    public TextChannel getChannel() {
        if (!channelIsSet())
            return null;
        return guild.getTextChannelById(config.getLogChannel(guild.getIdLong()));
    }

    public void sendLog(LogType logType, String log) {
        sendLog(logType, log, null);
    }

    public void sendLog(LogType logType, String log, @Nullable File file) {
        TextChannel channel = getChannel();
        if (channel != null) {
            MessageAction messageAction = channel.sendMessageEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor(
                                    logType.getEmoji().getName() + " " + logType.getName(),
                                    log
                            )
                            .setColor(logType.getColor())
                            .setTimestamp(Instant.now())
                            .build()
            );

            if (file != null)
                messageAction = messageAction.addFile(file);

            messageAction.queue();
        }
    }

    public boolean channelIsSet() {
        return config.getLogChannel(guild.getIdLong()) != -1L;
    }

    public enum LogType {
        TICKET_CREATION("Ticket Created", Emoji.fromUnicode("🆕"), GeneralUtils.parseColor("#32D200")),
        TICKET_RENAME("Ticket Renamed", Emoji.fromUnicode("📛"), GeneralUtils.parseColor("#3C00D2")),
        TICKET_ADDITION("User Added To Ticket", Emoji.fromUnicode("🧑"), GeneralUtils.parseColor("#00B5D2")),
        TICKET_CLOSE("Ticket Closed", Emoji.fromUnicode("🔒"), GeneralUtils.parseColor("#D20000"));

        @Getter
        private final String name;
        @Getter
        private final Emoji emoji;
        @Getter
        private final Color color;

        LogType(String name, Emoji emoji, Color color) {
            this.name = name;
            this.emoji = emoji;
            this.color = color;
        }
    }
}
