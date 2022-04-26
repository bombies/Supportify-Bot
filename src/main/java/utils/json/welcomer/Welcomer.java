package utils.json.welcomer;

import commands.general.welcomer.WelcomerConfigurator;
import lombok.Getter;
import main.Supportify;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.regex.Pattern;

public class Welcomer {
    @Getter
    private final boolean isEnabled;
    @Getter
    private final long channelID;
    @Getter
    private final TextChannel channel;
    @Getter
    private final long guildID;
    @Getter
    private final Guild guild;
    private final MessageEmbed embed;

    protected Welcomer(boolean isEnabled, long channelID, long guildID, MessageEmbed embed) {
        this.isEnabled = isEnabled;
        this.channelID = channelID;
        this.channel = Supportify.getApi().getTextChannelById(channelID);
        this.guildID = guildID;
        this.guild = Supportify.getApi().getGuildById(guildID);
        this.embed = embed;
    }

    public void sendMessage(User user) {
        channel.sendMessage(user.getAsMention()).setEmbeds(WelcomerConfigurator.formatEmbed(guild, user, embed)).queue();
    }

    public enum PlaceHolders {
        SERVER_NAME("{server}"),
        USER_NAME("{user_name}"),
        USER_MENTION("{user_mention}"),
        USER_TAG("{user_tag}"),
        USER_DISCRIMINATOR("{user_discriminator}"),
        USER_ID("{user_id}");

        private final String str;

        PlaceHolders(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }
}
