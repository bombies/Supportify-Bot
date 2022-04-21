package utils.json.tickets.transcripts;

import constants.TimeFormat;
import lombok.Getter;
import lombok.SneakyThrows;
import main.Supportify;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import utils.GeneralUtils;
import utils.json.tickets.Ticket;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TranscriptGenerator {

    private final Ticket ticket;
    private final Guild guild;
    private final TextChannel ticketChannel;
    private final Document document;
    private final Path transcriptPath;

    public TranscriptGenerator(Ticket ticket) {
        this.ticket = ticket;
        this.guild = Supportify.getApi().getGuildById(ticket.getGuildID());
        this.ticketChannel = guild.getTextChannelById(ticket.getChannelID());
        this.transcriptPath = Path.of("transcripts/" + ticket.getChannelID() + ".html");

        GeneralUtils.makeDir("transcripts");
        GeneralUtils.createFile(Path.of("transcripts"), ticket.getChannelID() + ".html");
        this.document = Jsoup.parse("<!DOCTYPE html>");
    }

    @SneakyThrows
    public File createTranscript() {
        final var messageHistory = ticketChannel.getIterableHistory();
        final List<MessageGroup> messageGroups = sortMessages(messageHistory);

        final var html = document.createElement("html");
        html.appendChild(handleHead(ticketChannel.getName()));

        final var body = document.createElement("body");
        body.addClass("tScript dark bg");

        final var container = document.createElement("div");
        container.appendChild(handleInfo());

        final var chatlog = document.createElement("div");
        chatlog.addClass("chatlog");

        messageGroups.forEach(messageGroup -> {
            final User author = messageGroup.getAuthor();

            final var messageGroupContainer = document.createElement("div");
            messageGroupContainer.addClass("chatlog__message-group");
            messageGroupContainer.attr("style", "border-top:0px");
            messageGroupContainer.appendChild(handleMessageAuthorInfo(author));

            final var messagesContainer = document.createElement("div");
            messagesContainer.addClass("chatlog__messages");

            final var msgSpan = document.createElement("span");
            msgSpan.addClass("chatlog__author-name");
            msgSpan.attr("title", author.getDiscriminator());
            msgSpan.attr("data-user-id", author.getId());
            msgSpan.text(author.getName());
            messagesContainer.appendChild(msgSpan);

            if (author.isBot()) {
                final var bot = document.createElement("span");
                bot.addClass("chatlog__bot-tag");
                bot.text("BOT");
                messagesContainer.appendChild(bot);
            }

            final var timeStamp = document.createElement("span");
            timeStamp.addClass("chatlog__timestamp");
            timeStamp.text(GeneralUtils.formatDate(System.currentTimeMillis(), TimeFormat.DD_M_YYYY_HH_MM_SS));
            messagesContainer.appendChild(timeStamp);

            messageGroup.getMessages().forEach(message -> {
                final var messageContainer = document.createElement("div");
                messageContainer.attr("id", "message-" + message.getId());
                messageContainer.addClass("chatlog__message");
                messageContainer.attr("data-message-id", message.getId());

                final var content = handleContent(message);
                if (content != null)
                    messageContainer.appendChild(content);

                message.getEmbeds().forEach(embed -> {
                    final var embedContainer = handleEmbeds(embed, message);
                    messageContainer.appendChild(embedContainer);
                });

                messagesContainer.appendChild(messageContainer);
                messageGroupContainer.appendChild(messagesContainer);
            });
            chatlog.appendChild(messageGroupContainer);
        });

        container.appendChild(chatlog);
        body.appendChild(container);
        html.appendChild(body);
        document.appendChild(html);

        GeneralUtils.setFileContent(transcriptPath, document.html());
        return new File(transcriptPath.toString());
    }

    private Node handleMessageAuthorInfo(User author) {
        final var avatarContainer = document.createElement("div");
        avatarContainer.addClass("chatlog__author-avatar-container");

        final var avatar = document.createElement("img");
        avatar.addClass("chatlog__author-avatar");
        avatar.attr("src", author.getEffectiveAvatarUrl());
        avatarContainer.appendChild(avatar);

        return avatarContainer;
    }

    private List<MessageGroup> sortMessages(MessagePaginationAction messages) {
        final List<MessageGroup> messageGroups = new ArrayList<>();
        for (final var d : messages) {
            if (messageGroups.size() == 0) {
                ArrayList<Message> messagesList = new ArrayList<>();
                messagesList.add(d);
                messageGroups.add(new MessageGroup(messagesList));
                continue;
            }

            final var messageGroup = messageGroups.get(messageGroups.size() -1);

            if (messageGroup.author.getIdLong() == d.getAuthor().getIdLong()) {
                ArrayList<Message> messagesList = new ArrayList<>();
                messagesList.add(d);
                messageGroups.add(new MessageGroup(messagesList));
                continue;
            }

            int MAX_TIME_BETWEEN_MESSAGES = 1000 * 60 * 10;
            if (messageGroup.firstKey + MAX_TIME_BETWEEN_MESSAGES <= TimeUnit.SECONDS.toMillis(d.getTimeCreated().toEpochSecond())) {
                ArrayList<Message> messagesList = new ArrayList<>();
                messagesList.add(d);
                messageGroups.add(new MessageGroup(messagesList));
                continue;
            }

            messageGroups.get(messageGroups.size() -1).addMessage(d);
        }
        return messageGroups;
    }

    private Node handleHead(String ticketName) {
        final var head = document.createElement("head");
        final var charset = document.createElement("meta");
        charset.attr("charset", "UTF-8");
        head.appendChild(charset);

        final var httpEquiv = document.createElement("meta");
        httpEquiv.attr("http-equiv", "X-UA-Compatible");
        httpEquiv.attr("content", "IE=edge");
        head.appendChild(httpEquiv);

        final var name = document.createElement("meta");
        name.attr("name", "viewport");
        name.attr("content", "width=device-width, initial-scale=1.0");
        head.appendChild(name);

        final var style = document.createElement("style");
        style.html(""); // TODO Set stylesheet
        head.appendChild(style);

        final var title = document.createElement("title");
        title.text(ticketName);
        head.appendChild(title);

        return head;
    }

    private Node handleInfo() {
        final var preamble = document.createElement("div");
        preamble.addClass("preamble");

        final var guildIconContainer = document.createElement("div");
        guildIconContainer.addClass("preamble__guild-icon-container");

        final var guildIcon = document.createElement("img");
        guildIcon.addClass("preamble__guild-icon");
        guildIcon.attr("src", guild.getIconUrl());
        guildIcon.attr("alt", "Guild Icon");
        guildIconContainer.appendChild(guildIcon);
        preamble.appendChild(guildIconContainer);

        final var infoContainer = document.createElement("div");
        infoContainer.addClass("preamble__entries-container");

        final var serverName = document.createElement("div");
        serverName.addClass("preamble__entry");
        serverName.text(guild.getName());
        infoContainer.appendChild(serverName);

        final var channelName = document.createElement("div");
        channelName.addClass("preamble__entry");
        channelName.text(ticketChannel.getName());
        infoContainer.appendChild(channelName);

        final var messageCountContainer = document.createElement("div");
        messageCountContainer.addClass("preamble__entry");
        messageCountContainer.text(ticket.getTotalMessageCount() + " messages");
        infoContainer.appendChild(messageCountContainer);
        preamble.appendChild(infoContainer);

        return preamble;
    }

    private Node handleEmbeds(MessageEmbed embed, Message message) {
        final var embedContainer = document.createElement("div");
        embedContainer.addClass("chatlog__embed");

        final var colorPill = document.createElement("div");
        colorPill.attr("style", "background-color:#" + Integer.toHexString(embed.getColorRaw()).substring(2));
        colorPill.addClass("chatlog__embed-color-pill");
        embedContainer.appendChild(colorPill);

        final var embedContentContainer = document.createElement("div");
        embedContentContainer.addClass("chatlog__embed-content-container");

        final var embedContent = document.createElement("div");
        embedContent.addClass("chatlog__embed-content");

        final var contentText = document.createElement("div");
        contentText.addClass("chatlog__embed-text");

        if (embed.getAuthor() != null) {
            if (embed.getAuthor().getName() != null) {
                final var embedAuthor = document.createElement("div");
                embedAuthor.addClass("chatlog__embed-author");

                final var authorName = document.createElement("span");
                authorName.addClass("chatlog__embed-author-name");
                authorName.text(embed.getAuthor().getName());
                embedAuthor.appendChild(authorName);
                contentText.appendChild(embedAuthor);
            }
        }

        if (embed.getTitle() != null) {
            final var embedTitleContainer = document.createElement("div");
            embedTitleContainer.addClass("chatlog__embed-title");

            final var embedTitle = document.createElement("span");
            embedTitle.addClass("markdown");
            embedTitle.text(handleContentString(embed.getTitle(), message));
            embedTitleContainer.appendChild(embedTitle);
            contentText.appendChild(embedTitleContainer);
        }

        if (embed.getDescription() != null) {
            final var embedDescContainer = document.createElement("div");
            embedDescContainer.addClass("chatlog__embed-description");

            final var embedDesc = document.createElement("span");
            embedDesc.addClass("markdown");
            embedDesc.text(handleContentString(embed.getDescription(), message));
            embedDescContainer.appendChild(embedDesc);
            contentText.appendChild(embedDescContainer);
        }

        final var embedFields = document.createElement("div");
        embedFields.addClass("chatlog__embed-fields");
        contentText.appendChild(embedFields);
        embedContent.appendChild(contentText);

        if (embed.getThumbnail() != null) {
            final var thumbnailContainer = document.createElement("div");
            thumbnailContainer.addClass("chatlog__embed-thumbnail-container");

            final var thumbnailLink = document.createElement("a");
            thumbnailLink.addClass("chatlog__embed-thumbnail-link");

            final var img = document.createElement("img");
            img.addClass("chatlog__embed-thumbnail");
            img.attr("src", embed.getThumbnail().getUrl());
            thumbnailLink.appendChild(img);
            thumbnailContainer.appendChild(thumbnailLink);
            embedContent.appendChild(thumbnailContainer);
        }
        embedContainer.appendChild(embedContent);

        if (embed.getFields().size() > 0) {
            final var chatLogEmedFields = document.createElement("div");
            chatLogEmedFields.addClass("chatlog__embed-fields");

            embed.getFields().forEach(field -> {
                final var fieldContainer = document.createElement("div");
                fieldContainer.addClass("chatlog__embed-field" + (field.isInline() ? "--inline" : ""));

                final var fieldName = document.createElement("div");
                fieldName.addClass("chatlog__embed-field-name");

                final var name = document.createElement("span");
                name.addClass("markdown");
                name.text(handleContentString(field.getName(), message));
                fieldName.appendChild(name);
                fieldContainer.appendChild(fieldName);

                final var fieldValue = document.createElement("div");
                fieldValue.addClass("chatlog__embed-field-value");

                final var fieldValueText = document.createElement("span");
                fieldValueText.addClass("markdown");
                fieldValueText.text(handleContentString(field.getValue(), message));
                fieldValue.appendChild(fieldValueText);
                fieldContainer.appendChild(fieldValue);
                chatLogEmedFields.appendChild(fieldContainer);

            });
            embedContentContainer.appendChild(chatLogEmedFields);
        }

        if (embed.getImage() != null) {
            final var imageContainer = document.createElement("div");
            imageContainer.addClass("chatlog__embed-image-container");

            final var imageLink = document.createElement("a");
            imageLink.addClass("chatlog__embed-image-link");

            final var img = document.createElement("img");
            img.addClass("chatlog__embed-image");
            img.attr("src", embed.getImage().getUrl());
            embedContentContainer.appendChild(imageContainer);
        }

        if (embed.getFooter() != null) {
            final var embedFooter = document.createElement("div");
            embedFooter.addClass("chatlog__embed-footer");

            final var embedFooterText = document.createElement("span");
            embedFooterText.addClass("chatlog__embed-footer-text");

            if (embed.getFooter().getText() != null && embed.getTimestamp() != null) {
                embedFooterText.text(handleContentString(embed.getFooter().getText(), message) + " • " + GeneralUtils.formatDate(TimeUnit.SECONDS.toMillis(embed.getTimestamp().toEpochSecond()), TimeFormat.DD_M_YYYY_HH_MM_SS));
            } else {
                embedFooterText.text((embed.getFooter().getText() != null ? handleContentString(embed.getFooter().getText(), message) : "") + (embed.getTimestamp() != null ? GeneralUtils.formatDate(TimeUnit.SECONDS.toMillis(embed.getTimestamp().toEpochSecond()), TimeFormat.DD_M_YYYY_HH_MM_SS) : ""));
            }
            embedFooter.appendChild(embedFooterText);
            embedContentContainer.appendChild(embedFooter);
        }
        embedContainer.appendChild(embedContentContainer);

        return embedContainer;
    }

    private Node handleContent(Message message) {
        if (message.getContentRaw().length() <= 0) return null;

        var content = handleContentReplacements(message.getContentRaw());
        final var contentContainer = document.createElement("div");
        contentContainer.addClass("chatlog__current");

        final var markdown = document.createElement("span");
        markdown.addClass("markdown");

        for (final var user : message.getMentionedMembers())
            content = content.replace("&lt;@" + user.getId() + "&gt;", "<span class=\"d-mention d-user\">@"+user.getNickname()+"</span>");
        for (final var role : message.getMentionedRoles())
            content = content.replace("&lt;@" + role.getId() + "&gt;", "<span class=\"d-mention d-role\">@"+role.getName()+"</span>");
        for (final var channel : message.getMentionedChannels())
            content = content.replace("&lt;@" + channel.getId() + "&gt;", "<span class=\"d-mention d-channel\">@"+channel.getName()+"</span>");

        markdown.html(content);
        contentContainer.appendChild(markdown);

        return contentContainer;
    }

    private final String handleContentString(String text, Message message) {
        var content = handleContentReplacements(text);

        for (final var user : message.getMentionedMembers())
            content = content.replaceAll("&lt;@" + user.getId() + "&gt;", "<span class=\"d-mention d-user\">@"+user.getNickname()+"</span>");
        for (final var role : message.getMentionedRoles())
            content = content.replaceAll("&lt;@" + role.getId() + "&gt;", "<span class=\"d-mention d-role\">@"+role.getName()+"</span>");
        for (final var channel : message.getMentionedChannels())
            content = content.replaceAll("&lt;@" + channel.getId() + "&gt;", "<span class=\"d-mention d-channel\">@"+channel.getName()+"</span>");

        return content;
    }

    private String handleContentReplacements(String text) {
        final String[] specialCharacters = new String[] {
                "\\", "!","\"","#","$","%","&","'","(",")","*",
                "+",",","-",".","/",":",";","<","=",">","?",
                "@","[","]","^","_","`","{","|","}","~"
        };

        String ret = text;
        for (final var character : specialCharacters)
            ret = ret.replace("\\" + character, "&#" + character.codePointAt(0) + ";");
        return ret;
    }

    private String handleFont(String text) {
        // TODO
        return text.replaceAll("\\*\\*[^\\*\\*]+\\*\\*", "");
    }

    private class MessageGroup {
        @Getter
        private final ArrayList<Message> messages;
        @Getter
        private final User author;
        @Getter
        private final long firstKey;

        MessageGroup(ArrayList<Message> messages) {
            this.messages = messages;
            this.author = messages.get(0).getAuthor();
            this.firstKey = TimeUnit.SECONDS.toMillis(messages.get(0).getTimeCreated().toEpochSecond());
        }

        public MessageGroup addMessage(Message message) {
            messages.add(message);
            return this;
        }
    }
}
