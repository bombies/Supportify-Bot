package utils.json.welcomer;

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
        channel.sendMessage(user.getAsMention()).setEmbeds(formatEmbed(user)).queue();
    }

    private MessageEmbed formatEmbed(User user) {
        final var newEmbed = new EmbedBuilder();
        if (embed.getTitle() != null)
            newEmbed.setTitle(doReplacements(user, embed.getTitle()));

        if (embed.getAuthor() != null) {
            var authorName = embed.getAuthor().getName();
            if (authorName != null)
                authorName = doReplacements(user, authorName);

            newEmbed.setAuthor(authorName, embed.getAuthor().getUrl(), embed.getAuthor().getIconUrl());
        }

        if (embed.getThumbnail() != null)
            newEmbed.setThumbnail(embed.getThumbnail().getUrl());

        if (embed.getDescription() != null)
            newEmbed.setDescription(doReplacements(user, embed.getDescription()));

        if (!embed.getFields().isEmpty()) {
            embed.getFields().forEach(field -> {
                var value = field.getValue();
                    if (value != null)
                        value = doReplacements(user, value);
                newEmbed.addField(field.getName(), value, field.isInline());
            });
        }

        if (embed.getImage() != null)
            newEmbed.setImage(embed.getImage().getUrl());

        MessageEmbed.Footer footer = embed.getFooter();
        if (footer != null) {
            String footerStr = footer.getText();
            if (footerStr != null)
                footerStr = doReplacements(user, footerStr);
            newEmbed.setFooter(footerStr, footer.getIconUrl());
        }

        if (embed.getTimestamp() != null)
            newEmbed.setTimestamp(embed.getTimestamp());

        newEmbed.setColor(embed.getColor());
        return newEmbed.build();
    }

    private String doReplacements(User user, String string) {
        return string
                .replaceAll(Pattern.quote(PlaceHolders.SERVER_NAME.toString()), guild.getName())
                .replaceAll(Pattern.quote(PlaceHolders.USER_NAME.toString()), user.getName())
                .replaceAll(Pattern.quote(PlaceHolders.USER_MENTION.toString()), user.getAsMention())
                .replaceAll(Pattern.quote(PlaceHolders.USER_TAG.toString()), user.getAsTag())
                .replaceAll(Pattern.quote(PlaceHolders.USER_DISCRIMINATOR.toString()), user.getDiscriminator())
                .replaceAll(Pattern.quote(PlaceHolders.USER_ID.toString()), user.getId());
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
