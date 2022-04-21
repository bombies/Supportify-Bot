package commands.general.tickets;

import commands.CommandContext;
import commands.ICommand;
import main.Supportify;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import utils.GeneralUtils;
import utils.SupportifyEmbedUtils;
import utils.component.interactions.AbstractSlashCommand;
import utils.json.tickets.TicketConfig;
import utils.json.tickets.TicketCreator;
import utils.json.tickets.TicketLogger;

import java.util.Arrays;
import java.util.List;

public class TicketCommand extends AbstractSlashCommand implements ICommand {
    public static String CREATOR_BUTTON_ID = "tickets:create";
    public static String CLOSE_BUTTON_ID = "tickets:close";

    @Override
    public void handle(CommandContext ctx) {

    }

    @Override
    public String getName() {
        return "tickets";
    }

    @Override
    public List<String> getAliases() {
        return List.of("ticket", "t");
    }

    @Override
    public String getHelp(String prefix) {
        return null;
    }

    @Override
    protected void buildCommand() {
        setCommand(
                getBuilder()
                        .setName(getName())
                        .setDescription("Manage the ticket system!")
                        .addSubCommands(
                                SubCommand.of(
                                        "setup",
                                        "Setup the ticket system"
                                ),
                                SubCommand.of(
                                        "setuplogs",
                                        "Setup the ticket logging channel"
                                ),
                                SubCommand.of(
                                        "blacklist",
                                        "Blacklist a user from making tickets",
                                        List.of(
                                                CommandOption.of(
                                                        OptionType.USER,
                                                        "user",
                                                        "The user to blacklist",
                                                        true
                                                )
                                        )
                                ),
                                SubCommand.of(
                                        "unblacklist",
                                        "Un-blacklist a user from making tickets",
                                        List.of(
                                                CommandOption.of(
                                                        OptionType.USER,
                                                        "user",
                                                        "The user to un-blacklist",
                                                        true
                                                )
                                        )
                                )
                        )
                        .addSubCommandGroups(
                                SubCommandGroup.of(
                                        "set",
                                        "Configure certain values for tickets",
                                        List.of(
                                              SubCommand.of(
                                                      "creatormessage",
                                                      "Edit the creator message",
                                                      List.of(
                                                              CommandOption.of(
                                                                      OptionType.STRING,
                                                                      "desc",
                                                                      "The new desription",
                                                                      true
                                                              )
                                                      )
                                              ),
                                              SubCommand.of(
                                                      "creatoremoji",
                                                      "Edit the creator button emoji",
                                                      List.of(
                                                              CommandOption.of(
                                                                      OptionType.STRING,
                                                                      "emoji",
                                                                      "The new button emoji",
                                                                      true
                                                              )
                                                      )
                                              ),
                                              SubCommand.of(
                                                      "supportrole",
                                                      "Edit the support team role",
                                                      List.of(
                                                              CommandOption.of(
                                                                      OptionType.ROLE,
                                                                      "role",
                                                                      "The new support role",
                                                                      true
                                                              )
                                                      )
                                              )
                                        )
                                )
                        )
                        .setPermissionCheck(event -> event.getMember().hasPermission(Permission.ADMINISTRATOR))
                        .setBotRequiredPermissions(Permission.MANAGE_PERMISSIONS, Permission.MESSAGE_MANAGE, Permission.MANAGE_CHANNEL)
                        .build()
        );
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (!checks(event)) return;

        String[] commandPath = event.getCommandPath().split("/");
        final Guild guild = event.getGuild();

        switch (commandPath[1]) {
            case "setup" -> event.replyEmbeds(handleSetup(guild)).setEphemeral(true).queue();
            case "setuplogs" -> event.replyEmbeds(handleLogSetup(guild)).setEphemeral(true).queue();
            case "blacklist" -> {
                User user = event.getOption("user").getAsUser();
                event.replyEmbeds(handleBlacklist(guild, user.getIdLong())).queue();
            }
            case "unblacklist" -> {
                User user = event.getOption("user").getAsUser();
                event.replyEmbeds(handleUnBlacklist(guild, user.getIdLong())).queue();
            }
            case "set" -> {
                switch (commandPath[2]) {
                    case "creatormessage" -> {
                        String desc = event.getOption("desc").getAsString();
                        event.replyEmbeds(setCreatorMessage(guild, desc)).setEphemeral(true).queue();
                    }
                    case "creatoremoji" -> {
                        String emoji = event.getOption("emoji").getAsString();
                        event.replyEmbeds(setCreatorEmoji(guild, emoji)).setEphemeral(true).queue();
                    }
                    case "supportrole" -> {
                        Role role = event.getOption("role").getAsRole();
                        event.replyEmbeds(setSupportRole(guild, role)).setEphemeral(true).queue();
                    }
                }
            }
        }
    }

    private MessageEmbed handleSetup(Guild guild) {
        final var config = new TicketConfig();
        final var guildID = guild.getIdLong();

        if (config.creatorExists(guildID))
            return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "Tickets have already been setup!").build();

        final var description = "Click the button below to create a ticket!";
        final var emoji = "📥";

        if (!config.creatorCategoryExists(guildID))
            guild.createCategory("TICKETS")
                    .queue(category -> createCreatorChannel(guild, category, config, description, emoji));
        else {
            Category category = guild.getCategoryById(config.getTicketCreatorCategory(guildID));
            createCreatorChannel(guild, category, config, description, emoji);
        }

        if (config.getSupportRole(guildID) == -1L) {
            guild.createRole()
                    .setName("Support Staff")
                    .setColor(GeneralUtils.parseColor("#00FFBE"))
                    .setMentionable(false)
                    .queue(role -> config.setSupportRole(guildID, role.getIdLong()));
        }

        config.setTicketMessageDescription(guildID, "Welcome to your ticket!" +
                "\nPlease be patient as a support team member will be available shortly to assist you.");

        return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "Successfully setup your ticket system!").build();
    }

    private MessageEmbed handleLogSetup(Guild guild) {
        final var config = new TicketConfig();

        if (!config.creatorCategoryExists(guild.getIdLong()))
            return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "The log channel can't be setup until tickets have been setup!" +
                    "\nRun the `setup` command to setup tickets.").build();

        if (!config.supportRoleIsSet(guild.getIdLong()))
            return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "The log channel can't be setup since a support role doesn't exist!\n" +
                    "Run the `set supportrole` command to se a support role for this server.").build();

        if (config.getLogChannel(guild.getIdLong()) != -1L)
            return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "The log channel has already been setup!").build();

        guild.getCategoryById(config.getTicketCreatorCategory(guild.getIdLong()))
                .createTextChannel("ticket-logs")
                .addPermissionOverride(guild.getPublicRole(),
                        List.of(),
                        List.of(Permission.VIEW_CHANNEL)
                )
                .addRolePermissionOverride(config.getSupportRole(guild.getIdLong()),
                        List.of(Permission.VIEW_CHANNEL),
                        List.of(Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION, Permission.CREATE_PUBLIC_THREADS, Permission.CREATE_PUBLIC_THREADS)
                )
                .queue(channel -> config.setLogChannel(guild.getIdLong(), channel.getIdLong()));

        return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "I have successfully created the ticket logs channel!").build();
    }

    private void createCreatorChannel(Guild guild, Category category, TicketConfig config, String description, String emoji) {
        guild.createTextChannel("tickets", category).queue(channel -> {
            channel.sendMessageEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", description).build())
                    .setActionRow(Button.of(ButtonStyle.PRIMARY, CREATOR_BUTTON_ID, "", Emoji.fromUnicode(emoji)))
                    .queue(message -> {
                        config.createCreator(
                                guild.getIdLong(),
                                category.getIdLong(),
                                channel.getIdLong(),
                                message.getIdLong(),
                                description,
                                emoji
                        );
                    });
        });
    }

    private MessageEmbed handleBlacklist(Guild guild, long uid) {
        final var config = new TicketConfig();
        final var guildID = guild.getIdLong();

        if (config.isBlackListed(guildID, uid))
            return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", GeneralUtils.toMention(uid, GeneralUtils.Mentioner.USER) + " is already blacklisted!").build();

        config.blackListUser(guildID, uid);
        return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", GeneralUtils.toMention(uid, GeneralUtils.Mentioner.USER) + " has been blacklisted!").build();
    }

    private MessageEmbed handleUnBlacklist(Guild guild, long uid) {
        final var config = new TicketConfig();
        final var guildID = guild.getIdLong();

        if (!config.isBlackListed(guildID, uid))
            return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", GeneralUtils.toMention(uid, GeneralUtils.Mentioner.USER) + " isn't blacklisted!").build();

        config.unBlackListUser(guildID, uid);
        return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", GeneralUtils.toMention(uid, GeneralUtils.Mentioner.USER) + " has been un-blacklisted!").build();
    }

    private MessageEmbed setCreatorMessage(Guild guild, String message) {
        final var config = new TicketConfig();

        if (!config.creatorExists(guild.getIdLong()))
            return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "The ticket creator hasn't been setup!" +
                    "\nPlease setup tickets by running the `setup` command.").build();

        config.setCreatorDescription(guild.getIdLong(), message);
        config.updateCreator(guild.getIdLong());
        return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "You have set the creator description to:\n\n" + message).build();
    }

    private MessageEmbed setCreatorEmoji(Guild guild, String emoji) {
        final var config = new TicketConfig();

        if (!config.creatorExists(guild.getIdLong()))
            return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "The ticket creator hasn't been setup!" +
                    "\nPlease setup tickets by running the `setup` command.").build();

        TicketCreator creator = config.getCreator(guild.getIdLong());
        Message msg = Supportify.getApi().getGuildById(guild.getIdLong())
                .getTextChannelById(creator.getChannelID())
                .retrieveMessageById(creator.getMessageID())
                .complete();

        try {
            msg.editMessageEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", creator.getMessageDescription()).build())
                    .setActionRow(Button.of(ButtonStyle.PRIMARY, CREATOR_BUTTON_ID, "", Emoji.fromUnicode(emoji)))
                    .complete();
        } catch (Exception e) {
            return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "Invalid emoji!").build();
        }

        config.setCreatorEmoji(guild.getIdLong(), emoji);
        return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "You have set the creator button emoji to: " + emoji).build();
    }

    private MessageEmbed setSupportRole(Guild guild, Role role) {
        if (role == null)
            return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "Invalid role!").build();

        final var config = new TicketConfig();

        config.setSupportRole(guild.getIdLong(), role.getIdLong());
        return SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "The support role has been set to: " + role.getAsMention()).build();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (!event.getButton().getId().equals(CREATOR_BUTTON_ID)) return;

        final var config = new TicketConfig();
        final var guild = event.getGuild();
        final var guildID = guild.getIdLong();

        if (config.isBlackListed(guildID, event.getUser().getIdLong())) {
            event.replyEmbeds(SupportifyEmbedUtils.embedMessage("You have been blacklisted from making tickets!").build())
                    .setEphemeral(true).queue();
            return;
        }

        final var user = event.getUser();
        final var creator = config.getCreator(guildID);
        final var category = guild.getCategoryById(creator.getCategoryID());
        guild.createTextChannel("ticket-" + formatTicketNumber(config.getTicketCount(guild.getIdLong()) + 1), category)
                .addRolePermissionOverride(
                        guild.getPublicRole().getIdLong(),
                        List.of(),
                        List.of(Permission.VIEW_CHANNEL)
                )
                .addRolePermissionOverride(
                        config.getSupportRole(guild.getIdLong()),
                        List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_MANAGE),
                        List.of(Permission.MANAGE_CHANNEL, Permission.CREATE_INSTANT_INVITE)
                )
                .addMemberPermissionOverride(
                        user.getIdLong(),
                        List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND),
                        List.of(Permission.MESSAGE_MANAGE, Permission.CREATE_INSTANT_INVITE)
                )
                .queue(channel -> channel.sendMessage(user.getAsMention())
                        .setEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", config.getTicketMessageDescription(guild.getIdLong()).replaceAll("\\\\n", "\\n")).build())
                        .setActionRow(Button.of(ButtonStyle.DANGER, CLOSE_BUTTON_ID, "Close ticket", Emoji.fromUnicode("🔒")))
                        .queue(message -> {
                            config.openTicket(guildID, channel.getIdLong(), user.getIdLong());
                            new TicketLogger(guild).sendLog(TicketLogger.LogType.TICKET_CREATION, user.getAsMention() + " has created " + channel.getName());
                            event.replyEmbeds(SupportifyEmbedUtils.embedMessageWithAuthor("Tickets", "I've created a ticket for you in: " + channel.getAsMention()).build())
                                    .setEphemeral(true)
                                    .queue();
                        }), new ErrorHandler().handle(ErrorResponse.INVALID_FORM_BODY, e -> {
                            event.replyEmbeds(SupportifyEmbedUtils.embedMessage("There was an issue creating a ticket for you...\n" +
                                    "Tell an admin to ensure that a support role has been set!").build())
                                    .setEphemeral(true)
                                    .queue();
                }));
    }

    private String formatTicketNumber(int num) {
        return String.format("%04d", num);
    }
}
