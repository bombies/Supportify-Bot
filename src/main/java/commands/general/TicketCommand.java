package commands.general;

import commands.CommandContext;
import commands.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import utils.GeneralUtils;
import utils.component.interactions.AbstractSlashCommand;
import utils.json.tickets.TicketConfig;

import javax.script.ScriptException;
import java.util.List;

public class TicketCommand extends AbstractSlashCommand implements ICommand {
    public static String CREATOR_BUTTON_ID = "tickets:create";
    public static String CLOSE_BUTTON_ID = "tickets:close";

    @Override
    public void handle(CommandContext ctx) throws ScriptException {

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

        switch (commandPath[0]) {
            case "setup" -> event.replyEmbeds(handleSetup(guild)).setEphemeral(true).queue();
            case "blacklist" -> {
                User user = event.getOption("user").getAsUser();
                event.replyEmbeds(handleBlacklist(guild, user.getIdLong())).queue();
            }
            case "unblacklist" -> {
                User user = event.getOption("user").getAsUser();
                event.replyEmbeds(handleUnBlacklist(guild, user.getIdLong())).queue();
            }
            case "set" -> {
                switch (commandPath[1]) {
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
        final var creator = config.getCreator(guildID);

        if (creator.getChannelID() != 1L)
            return EmbedUtils.embedMessageWithTitle("Tickets", "Tickets have already been setup!").build();

        final var description = "Click the button below to create a ticket!";
        final var emoji = "📥";

        if (creator.getCategoryID() == -1L)
            guild.createCategory("TICKETS").queue(category -> {
               guild.createTextChannel("tickets", category).queue(channel -> {
                   channel.sendMessageEmbeds(EmbedUtils.embedMessageWithTitle("Tickets", description).build())
                           .setActionRow(Button.of(ButtonStyle.PRIMARY, CREATOR_BUTTON_ID, "", Emoji.fromUnicode(emoji)))
                           .queue(message -> {
                               config.createCreator(
                                       guildID,
                                       category.getIdLong(),
                                       channel.getIdLong(),
                                       message.getIdLong(),
                                       description,
                                       emoji
                               );
                           });
               });
            });

        if (config.getSupportRole(guildID) != -1L) {
            guild.createRole()
                    .setName("Support Staff")
                    .setColor(GeneralUtils.parseColor("#00FFBE"))
                    .setMentionable(false)
                    .queue(role -> config.setSupportRole(guildID, role.getIdLong()));
        }

        return EmbedUtils.embedMessageWithTitle("Tickets", "Successfully setup your ticket system!").build();
    }

    private MessageEmbed handleBlacklist(Guild guild, long uid) {
        final var config = new TicketConfig();
        final var guildID = guild.getIdLong();

        if (config.isBlackListed(guildID, uid))
            return EmbedUtils.embedMessageWithTitle("Tickets", GeneralUtils.toMention(uid, GeneralUtils.Mentioner.USER) + " is already blacklisted!").build();

        config.blackListUser(guildID, uid);
        return EmbedUtils.embedMessageWithTitle("Tickets", GeneralUtils.toMention(uid, GeneralUtils.Mentioner.USER) + " has been blacklisted!").build();
    }

    private MessageEmbed handleUnBlacklist(Guild guild, long uid) {
        final var config = new TicketConfig();
        final var guildID = guild.getIdLong();

        if (!config.isBlackListed(guildID, uid))
            return EmbedUtils.embedMessageWithTitle("Tickets", GeneralUtils.toMention(uid, GeneralUtils.Mentioner.USER) + " isn't blacklisted!").build();

        config.unBlackListUser(guildID, uid);
        return EmbedUtils.embedMessageWithTitle("Tickets", GeneralUtils.toMention(uid, GeneralUtils.Mentioner.USER) + " has been un-blacklisted!").build();
    }

    private MessageEmbed setCreatorMessage(Guild guild, String message) {
        final var config = new TicketConfig();
        config.setCreatorDescription(guild.getIdLong(), message);
        config.updateCreator(guild.getIdLong());
        return EmbedUtils.embedMessageWithTitle("Tickets", "You have set the creator description to:\n\n" + message).build();
    }

    private MessageEmbed setCreatorEmoji(Guild guild, String emoji) {
        final var config = new TicketConfig();

        try {
            Emoji.fromUnicode(emoji);
        } catch (Exception e) {
            return EmbedUtils.embedMessageWithTitle("Tickets", "`` isn't a valid emoji!").build();
        }

        config.setCreatorEmoji(guild.getIdLong(), emoji);
        config.updateCreator(guild.getIdLong());
        return EmbedUtils.embedMessageWithTitle("Tickets", "You have set the creator button emoji to: " + emoji).build();
    }

    private MessageEmbed setSupportRole(Guild guild, Role role) {
        if (role == null)
            return EmbedUtils.embedMessageWithTitle("Tickets", "Invalid role!").build();

        final var config = new TicketConfig();

        config.setSupportRole(guild.getIdLong(), role.getIdLong());
        return EmbedUtils.embedMessageWithTitle("Tickets", "The support role has been set to: " + role.getAsMention()).build();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (!event.getButton().getId().equals(CREATOR_BUTTON_ID)) return;

        final var config = new TicketConfig();
        final var guild = event.getGuild();
        final var guildID = guild.getIdLong();

        if (config.isBlackListed(guildID, event.getUser().getIdLong())) {
            event.replyEmbeds(EmbedUtils.embedMessage("You have been blacklisted from making tickets!").build())
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
                        .setEmbeds(EmbedUtils.embedMessageWithTitle("Tickets", config.getTicketMessageDescription(guild.getIdLong()).replaceAll("\\\\n", "\\n")).build())
                        .setActionRow(Button.of(ButtonStyle.DANGER, CLOSE_BUTTON_ID + ":" + channel.getIdLong(), "Close ticket", Emoji.fromUnicode("🔒")))
                        .queue(message -> {
                            config.openTicket(guildID, channel.getIdLong(), user.getIdLong());
                            event.replyEmbeds(EmbedUtils.embedMessageWithTitle("Tickets", "I've created a ticket for you in: " + channel.getAsMention()).build())
                                    .setEphemeral(true)
                                    .queue();
                        }));
    }

    private String formatTicketNumber(int num) {
        return String.format("%04d", num);
    }
}
