package utils.json.tickets.transcripts;

import constants.TimeFormat;
import lombok.Getter;
import lombok.SneakyThrows;
import main.Supportify;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import utils.GeneralUtils;
import utils.json.tickets.Ticket;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
        this.document = new Document("");
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
            timeStamp.text(GeneralUtils.formatDate(System.currentTimeMillis(), TimeFormat.MMM_DD_YYYY_HH_MM_SS));
            messagesContainer.appendChild(timeStamp);

            ArrayList<Message> messages = messageGroup.getMessages();
            Collections.reverse(messages);
            messages.forEach(message -> {
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

        GeneralUtils.setFileContent(transcriptPath, "<!DOCTYPE html>\n" + document.html());
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

            final var messageGroup = messageGroups.get(messageGroups.size() - 1);

            if (messageGroup.author.getIdLong() != d.getAuthor().getIdLong()) {
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

            messageGroups.get(messageGroups.size() - 1).addMessage(d);
        }
        Collections.reverse(messageGroups);
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
        style.html("/* General */@font-face {    font-family: Whitney;    src: url(https://discordapp.com/assets/6c6374bad0b0b6d204d8d6dc4a18d820.woff);    font-weight: 300;}@font-face {    font-family: Whitney;    src: url(https://discordapp.com/assets/e8acd7d9bf6207f99350ca9f9e23b168.woff);    font-weight: 400;}@font-face {    font-family: Whitney;    src: url(https://discordapp.com/assets/3bdef1251a424500c1b3a78dea9b7e57.woff);    font-weight: 500;}@font-face {    font-family: Whitney;    src: url(https://discordapp.com/assets/be0060dafb7a0e31d2a1ca17c0708636.woff);    font-weight: 600;}@font-face {    font-family: Whitney;    src: url(https://discordapp.com/assets/8e12fb4f14d9c4592eb8ec9f22337b04.woff);    font-weight: 700;}.tScript {    font-family: \"Whitney\", \"Helvetica Neue\", Helvetica, Arial, sans-serif;    font-size: 17px;}.tScript a {    text-decoration: none;}.tScript a:hover {    text-decoration: underline;}.tScript img {    object-fit: contain;}.tScript .markdown {    max-width: 100%;    white-space: pre-wrap;    line-height: 1.3;    overflow-wrap: break-word;}.tScript .d-spoiler {    width: fit-content;}.tScript .spoiler--hidden {    cursor: pointer;}.tScript .d-spoiler {    border-radius: 3px;}.tScript .spoiler--hidden .spoiler-text {    color: rgba(0, 0, 0, 0);}.tScript .spoiler--hidden .spoiler-text::selection {    color: rgba(0, 0, 0, 0);}.tScript .spoiler-image {    position: relative;    overflow: hidden;    border-radius: 3px;}.tScript .spoiler--hidden .spoiler-image {    box-shadow: 0 0 1px 1px rgba(0, 0, 0, 0.1);}.tScript .spoiler--hidden .spoiler-image img {    filter: blur(44px);}.tScript .spoiler--hidden .spoiler-image:after {    content: \"SPOILER\";    color: #dcddde;    background-color: rgba(0, 0, 0, 0.6);    position: absolute;    left: 50%;    top: 50%;    transform: translate(-50%, -50%);    font-weight: 600;    padding: 0.5em 0.7em;    border-radius: 20px;    letter-spacing: 0.05em;    font-size: 0.9em;}.tScript .spoiler--hidden:hover .spoiler-image:after {    color: #fff;    background-color: rgba(0, 0, 0, 0.9);}.tScript blockquote {    margin: 0.1em 0;    padding-left: 0.6em;    border-left: 4px solid;    border-radius: 3px;}.tScript code {    font-family: \"Consolas\", \"Courier New\", Courier, monospace;}.tScript pre {    font-family: \"Consolas\", \"Courier New\", Courier, monospace;    margin-top: 0.25em;    padding: 0.5em;    border: 2px solid;    border-radius: 5px;}.tScript .pre--multiline {    margin-top: 0.25em;    padding: 0.5em;    border: 2px solid;    border-radius: 5px;}.tScript .pre--inline {    padding: 2px;    border-radius: 3px;    font-size: 0.85em;}.tScript .d-mention {    border-radius: 3px;    padding: 0 2px;    color: #7289da;    background: rgba(114, 137, 218, .1);    font-weight: 500;}.tScript .emoji,.tScript .d-emoji {    width: 1.25em;    height: 1.25em;    margin: 0 0.06em;    vertical-align: -0.4em;}.tScript .emoji--small {    width: 1em;    height: 1em;}.tScript .chatlog__reaction>.emoji {    width: 1em;    height: 1em;}.tScript .emoji--large {    width: 2.8em;    height: 2.8em;}/* Preamble */.tScript .preamble {    display: grid;    margin: 0 0.3em 0.6em 0.3em;    max-width: 100%;    grid-template-columns: auto 1fr;}.tScript .preamble__guild-icon-container {    grid-column: 1;}.tScript .preamble__guild-icon {    max-width: 88px;    max-height: 88px;}.tScript .preamble__entries-container {    grid-column: 2;    margin-left: 0.6em;}.tScript .preamble__entry {    font-size: 1.4em;}.tScript .preamble__entry--small {    font-size: 1em;}/* Chatlog */.tScript .chatlog {    max-width: 100%;}.tScript .chatlog__message-group {    display: grid;    margin: 0 0.6em;    padding: 0.9em 0;    border-top: 1px solid;    grid-template-columns: auto 1fr;}.tScript .chatlog__author-avatar-container {    grid-column: 1;    width: 40px;    height: 40px;}.tScript .chatlog__author-avatar {    border-radius: 50%;    height: 40px;    width: 40px;}.tScript .chatlog__messages {    grid-column: 2;    margin-left: 1.2em;    min-width: 50%;}.tScript .chatlog__author-name {    font-weight: 500;}.tScript .chatlog__timestamp {    margin-left: 0.3em;    font-size: 0.75em;}.tScript .chatlog__message {    padding: 0.1em 0.3em;    margin: 0 -0.3em;    background-color: transparent;    transition: background-color 1s ease;}.tScript .chatlog__content {    font-size: 0.95em;    word-wrap: break-word;}.tScript .chatlog__edited-timestamp {    margin-left: 0.15em;    font-size: 0.8em;}.tScript .chatlog__attachment {    margin-top: 0.3em;}.tScript .chatlog__attachment-thumbnail {    vertical-align: top;    max-width: 45vw;    max-height: 500px;    border-radius: 3px;}.tScript .chatlog__embed {    display: flex;    margin-top: 0.3em;    max-width: 520px;}.tScript .chatlog__embed-color-pill {    flex-shrink: 0;    width: 0.25em;    border-top-left-radius: 3px;    border-bottom-left-radius: 3px;}.tScript .chatlog__embed-content-container {    display: flex;    flex-direction: column;    padding: 0.5em 0.6em;    border: 1px solid;    border-top-right-radius: 3px;    border-bottom-right-radius: 3px;}.tScript .chatlog__embed-content {    display: flex;    width: 100%;}.tScript .chatlog__embed-text {    flex: 1;}.tScript .chatlog__embed-author {    display: flex;    margin-bottom: 0.3em;    align-items: center;}.tScript .chatlog__embed-author-icon {    margin-right: 0.5em;    width: 20px;    height: 20px;    border-radius: 50%;}.tScript .chatlog__embed-author-name {    font-size: 0.875em;    font-weight: 600;}.tScript .chatlog__embed-title {    margin-bottom: 0.2em;    font-size: 0.875em;    font-weight: 600;}.tScript .chatlog__embed-description {    font-weight: 500;    font-size: 0.85em;}.tScript .chatlog__embed-fields {    display: flex;    flex-wrap: wrap;}.tScript .chatlog__embed-field {    flex: 0;    min-width: 100%;    max-width: 506px;    padding-top: 0.6em;    font-size: 0.875em;}.tScript .chatlog__embed-field--inline {    flex: 1;    flex-basis: auto;    min-width: 150px;}.tScript .chatlog__embed-field-name {    margin-bottom: 0.2em;    font-weight: 600;}.tScript .chatlog__embed-field-value {    font-weight: 500;}.tScript .chatlog__embed-thumbnail {    flex: 0;    margin-left: 1.2em;    max-width: 80px;    max-height: 80px;    border-radius: 3px;}.tScript .chatlog__embed-image-container {    margin-top: 0.6em;}.tScript .chatlog__embed-image {    width: 100%;    max-width: 500px;    max-height: 400px;    border-radius: 3px;}.tScript .chatlog__embed-footer {    margin-top: 0.6em;}.tScript .chatlog__embed-footer-icon {    margin-right: 0.2em;    width: 20px;    height: 20px;    border-radius: 50%;    vertical-align: middle;}.tScript .chatlog__embed-footer-text {    font-size: 0.75em;    font-weight: 500;}.tScript .chatlog__reactions {    display: flex;}.tScript .chatlog__reaction {    display: flex;    align-items: center;    margin: 0.35em 0.1em 0.1em 0.1em;    padding: 0.2em 0.35em;    border-radius: 3px;}.tScript .chatlog__reaction-count {    min-width: 9px;    margin-left: 0.35em;    font-size: 0.875em;}.tScript .chatlog__bot-tag {    position: relative;    top: -.2em;    margin-left: 0.3em;    padding: 0.05em 0.3em;    border-radius: 3px;    vertical-align: middle;    line-height: 1.3;    background: #7289da;    color: #ffffff;    font-size: 0.625em;    font-weight: 500;}/* Postamble */.tScript .postamble {    margin: 1.4em 0.3em 0.6em 0.3em;    padding: 1em;    border-top: 1px solid;}/* General */.tScript.dark.bg {    background-color: #36393e;    color: #dcddde;}.tScript.dark a {    color: #0096cf;}.tScript.dark .d-spoiler {    background-color: rgba(255, 255, 255, 0.1);}.tScript.dark .spoiler--hidden .spoiler-text {    background-color: #202225;}.tScript.dark .spoiler--hidden:hover .spoiler-text {    background-color: rgba(32, 34, 37, 0.8);}.tScript.dark blockquote {    border-color: #4f545c;}.tScript.dark .attachmentLine>img,.tScript.dark .attachmentLine>span {    display: inline-block;}.tScript.dark .attachment-size {    color: #97998a !important;}.tScript.dark .attachment {    background-color: #2f3136 !important;    border-color: #282b30 !important;    color: #b9bbbe !important;    margin-top: 0.25em;    padding: 0.5em;    border: 2px solid;    border-radius: 2px;}.tScript.dark pre {    background-color: #2f3136 !important;    border-color: #282b30 !important;    color: #b9bbbe !important;}.tScript code {    background-color: #2f3136 !important;}.tScript.dark .pre--multiline {    border-color: #282b30 !important;    color: #b9bbbe !important;}/* === Preamble === */.tScript.dark .preamble__entry {    color: #ffffff;}/* Chatlog */.tScript.dark .chatlog__message-group {    border-color: rgba(255, 255, 255, 0.1);}.tScript.dark .chatlog__author-name {    color: #ffffff;}.tScript.dark .chatlog__timestamp {    color: rgba(255, 255, 255, 0.2);}.tScript.dark .chatlog__message--highlighted {    background-color: rgba(114, 137, 218, 0.2) !important;}.tScript.dark .chatlog__message--pinned {    background-color: rgba(249, 168, 37, 0.05);}.tScript.dark .chatlog__edited-timestamp {    color: rgba(255, 255, 255, 0.2);}.tScript.dark .chatlog__embed-color-pill--default {    background-color: rgba(79, 84, 92, 1);}.tScript.dark .chatlog__embed-content-container {    background-color: rgba(46, 48, 54, 0.3);    border-color: rgba(46, 48, 54, 0.6);}.tScript.dark .chatlog__embed-author-name {    color: #ffffff;}.tScript.dark .chatlog__embed-author-name-link {    color: #ffffff;}.tScript.dark .chatlog__embed-title {    color: #ffffff;}.tScript.dark .chatlog__embed-description {    color: rgba(255, 255, 255, 0.6);}.tScript.dark .chatlog__embed-field-name {    color: #ffffff;}.tScript.dark .chatlog__embed-field-value {    color: rgba(255, 255, 255, 0.6);}.tScript.dark .chatlog__embed-footer {    color: rgba(255, 255, 255, 0.6);}.tScript.dark .chatlog__reaction {    background-color: rgba(255, 255, 255, 0.05);}.tScript.dark .chatlog__reaction-count {    color: rgba(255, 255, 255, 0.3);}/* Postamble */.tScript.dark .postamble {    border-color: rgba(255, 255, 255, 0.1);}.tScript.dark .postamble__entry {    color: #ffffff;}");
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
        guildIcon.attr("src", guild.getIconUrl() == null ? "https://polybit-apps.s3.amazonaws.com/stdlib/users/discord/profile/image.png?1621007833204" : guild.getIconUrl());
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
        colorPill.attr("style", "background-color:#" + Integer.toHexString(embed.getColorRaw()));
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
        embedContentContainer.appendChild(embedContent);

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
                embedFooterText.text(handleContentString(embed.getFooter().getText(), message) + " • " + GeneralUtils.formatDate(TimeUnit.SECONDS.toMillis(embed.getTimestamp().toEpochSecond()), TimeFormat.MMM_DD_YYYY_HH_MM_SS));
            } else {
                embedFooterText.text((embed.getFooter().getText() != null ? handleContentString(embed.getFooter().getText(), message) : "") + (embed.getTimestamp() != null ? GeneralUtils.formatDate(TimeUnit.SECONDS.toMillis(embed.getTimestamp().toEpochSecond()), TimeFormat.MMM_DD_YYYY_HH_MM_SS) : ""));
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
        contentContainer.addClass("chatlog__content");

        final var markdown = document.createElement("span");
        markdown.addClass("markdown");

        for (final var user : message.getMentionedUsers())
            content = content.replaceAll("&lt;@" + user.getId() + "&gt;", "<span class=\"d-mention d-user\">@" + user.getName() + "</span>")
                    .replaceAll("<@" + user.getId() + ">", "<span class=\"d-mention d-user\">@" + user.getName() + "</span>");
        for (final var role : message.getMentionedRoles())
            content = content.replaceAll("&lt;@" + role.getId() + "&gt;", "<span class=\"d-mention d-role\">@"+role.getName()+"</span>")
                            .replaceAll("<@" + role.getId() + ">", "<span class=\"d-mention d-role\">@"+role.getName()+"</span>");
        for (final var channel : message.getMentionedChannels())
            content = content.replaceAll("&lt;@" + channel.getId() + "&gt;", "<span class=\"d-mention d-channel\">@"+channel.getName()+"</span>")
                            .replaceAll("<@" + channel.getId() + ">", "<span class=\"d-mention d-channel\">@"+channel.getName()+"</span>");

        markdown.html(content);
        contentContainer.appendChild(markdown);

        return contentContainer;
    }

    private String handleContentString(String text, Message message) {
        var content = handleContentReplacements(text);

        for (final var user : message.getMentionedUsers())
            content = content.replaceAll("&lt;@" + user.getId() + "&gt;", "<span class=\"d-mention d-user\">@" + user.getName() + "</span>")
                    .replaceAll("<@" + user.getId() + ">", "<span class=\"d-mention d-user\">@" + user.getName() + "</span>");
        for (final var role : message.getMentionedRoles())
            content = content.replaceAll("&lt;@" + role.getId() + "&gt;", "<span class=\"d-mention d-role\">@"+role.getName()+"</span>")
                    .replaceAll("<@" + role.getId() + ">", "<span class=\"d-mention d-role\">@"+role.getName()+"</span>");
        for (final var channel : message.getMentionedChannels())
            content = content.replace("&lt;@" + channel.getId() + "&gt;", "<span class=\"d-mention d-channel\">@"+channel.getName()+"</span>")
                    .replaceAll("<@" + channel.getId() + ">", "<span class=\"d-mention d-channel\">@"+channel.getName()+"</span>");

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
