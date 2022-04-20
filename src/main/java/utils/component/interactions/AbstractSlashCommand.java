package utils.component.interactions;

import commands.SlashCommandManager;
import lombok.Getter;
import lombok.SneakyThrows;
import main.Config;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import utils.GeneralUtils;
import utils.SupportifyEmbedUtils;
import utils.component.AbstractInteraction;
import utils.component.InvalidBuilderException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class AbstractSlashCommand extends AbstractInteraction {
    private Command command = null;

    public String getName() {
        if (command == null)
            buildCommand();
        return command.name;
    }

    public String getDescription() {
        if (command == null)
            buildCommand();
        return command.description;
    }

    public List<SubCommand> getSubCommands() {
        if (command == null)
            buildCommand();
        return command.subCommands;
    }

    public List<SubCommandGroup> getSubCommandGroups() {
        if (command == null)
            buildCommand();
        return command.subCommandGroups;
    }

    public List<CommandOption> getOptions() {
        if (command == null)
            buildCommand();
        return command.getOptions();
    }

    public List<net.dv8tion.jda.api.Permission> getBotRequiredPermissions() {
        if (command == null)
            buildCommand();
        return command.botRequiredPermissions;
    }

    private SlashCommandData getCommandData() {
        if (command == null)
            buildCommand();

        SlashCommandData commandData = Commands.slash(
                command.name, command.description
        );

        // Adding subcommands
        if (!command.getSubCommands().isEmpty() || !command.getSubCommandGroups().isEmpty()) {
            if (!command.getSubCommands().isEmpty()) {
                for (SubCommand subCommand : command.getSubCommands()) {
                    var subCommandData = new SubcommandData(subCommand.getName(), subCommand.getDescription());

                    // Adding options for subcommands
                    for (CommandOption options : subCommand.getOptions()) {
                        OptionData optionData = new OptionData(options.getType(), options.getName(), options.getDescription(), options.isRequired());
                        if (options.getChoices() != null)
                            for (String choices : options.getChoices())
                                optionData.addChoice(choices, choices);

                        subCommandData.addOptions(optionData);
                    }
                    commandData.addSubcommands(subCommandData);
                }
            }

            if (!command.getSubCommandGroups().isEmpty())
                for (var subCommandGroup : command.getSubCommandGroups())
                    commandData.addSubcommandGroups(subCommandGroup.build());
        } else {
            // Adding options for the main command
            for (CommandOption options : command.getOptions()) {
                OptionData optionData = new OptionData(options.getType(), options.getName(), options.getDescription(), options.isRequired());

                if (options.getChoices() != null)
                    for (String choices : options.getChoices())
                        optionData.addChoice(choices, choices);
                commandData.addOptions(optionData);
            }
        }

        return commandData;
    }


    @SneakyThrows
    public void loadCommand(Guild g) {
        buildCommand();

        if (command == null)
            throw new IllegalStateException("The command is null! Cannot load into guild.");

        // Initial request builder
        CommandCreateAction commandCreateAction = g.upsertCommand(command.getName(), command.getDescription());

        // Adding subcommands
        if (!command.getSubCommands().isEmpty() || !command.getSubCommandGroups().isEmpty()) {
            if (!command.getSubCommands().isEmpty()) {
                for (SubCommand subCommand : command.getSubCommands()) {
                    var subCommandData = new SubcommandData(subCommand.getName(), subCommand.getDescription());

                    // Adding options for subcommands
                    for (CommandOption options : subCommand.getOptions()) {
                        OptionData optionData = new OptionData(options.getType(), options.getName(), options.getDescription(), options.isRequired());
                        if (options.getChoices() != null)
                            for (String choices : options.getChoices())
                                optionData.addChoice(choices, choices);

                        subCommandData.addOptions(optionData);
                    }
                    commandCreateAction = commandCreateAction.addSubcommands(subCommandData);
                }
            }

            if (!command.getSubCommandGroups().isEmpty())
                for (var subCommandGroup : command.getSubCommandGroups())
                    commandCreateAction = commandCreateAction.addSubcommandGroups(subCommandGroup.build());
        } else {
            // Adding options for the main command
            for (CommandOption options : command.getOptions()) {
                OptionData optionData = new OptionData(options.getType(), options.getName(), options.getDescription(), options.isRequired());

                if (options.getChoices() != null)
                    for (String choices : options.getChoices())
                        optionData.addChoice(choices, choices);
                commandCreateAction = commandCreateAction.addOptions(optionData);
            }
        }

        commandCreateAction.queueAfter(1, TimeUnit.SECONDS, null, new ErrorHandler()
                .handle(ErrorResponse.MISSING_ACCESS, e -> {}));
    }

    public void unload(Guild g) {
        g.retrieveCommands().queue(commands -> {
            final net.dv8tion.jda.api.interactions.commands.Command matchedCommand = commands.stream()
                    .filter(command -> command.getName().equals(this.getName()))
                    .findFirst()
                    .orElse(null);

            if (matchedCommand == null) return;

            g.deleteCommandById(matchedCommand.getIdLong()).queue();
        });
    }

    public static void loadAllCommands(Guild g) {
        SlashCommandManager slashCommandManager = new SlashCommandManager();
        List<AbstractSlashCommand> commands = slashCommandManager.getCommands();
        List<AbstractSlashCommand> devCommands = slashCommandManager.getDevCommands();
        CommandListUpdateAction commandListUpdateAction = g.updateCommands();

        for (var cmd : commands)
            commandListUpdateAction = commandListUpdateAction.addCommands(
                    cmd.getCommandData()
            );

        if (g.getOwnerIdLong() == Config.getOwnerID()) {
            for (var cmd : devCommands)
                commandListUpdateAction = commandListUpdateAction.addCommands(
                        cmd.getCommandData().setDefaultEnabled(false)
                );
        }

        commandListUpdateAction.queueAfter(1, TimeUnit.SECONDS, e -> {
                    for (var createdCommand : e) {
                        if (!slashCommandManager.isDevCommand(createdCommand.getName())) continue;

                        createdCommand.updatePrivileges(g, CommandPrivilege.enableUser(Config.getOwnerID()))
                                .queue();
                    }
                }, new ErrorHandler().handle(ErrorResponse.fromCode(30034), e -> g.retrieveOwner().queue(
                        owner -> owner.getUser()
                                .openPrivateChannel().queue(channel -> {
                                    channel.sendMessageEmbeds(SupportifyEmbedUtils.embedMessage("Hey, I could not create slash commands in **"+g.getName()+"**" +
                                                    " due to being re-invited too many times. Try inviting me again tomorrow to fix this issue.").build())
                                            .queue(null, new ErrorHandler()
                                                    .handle(ErrorResponse.CANNOT_SEND_TO_USER, ex2 -> {}));
                                })
                ))
        );
    }

    protected void setCommand(Command command) {
        this.command = command;
    }

    protected Command getCommand() {
        if (command == null)
            buildCommand();
        return command;
    }

    protected boolean checks(SlashCommandInteractionEvent event) {
        if (!nameCheck(event)) return false;
        if (!botEmbedCheck(event)) return false;
        if (!botPermsCheck(event)) return false;
        return predicateCheck(event);
    }

    protected boolean nameCheck(SlashCommandInteractionEvent event) {
        if (command == null)
            buildCommand();
        return command.getName().equals(event.getName());
    }

    protected boolean predicateCheck(SlashCommandInteractionEvent event) {
        if (command == null)
            buildCommand();
        if (command.getCheckPermission() == null)
            return true;
        boolean res = command.getCheckPermission().test(event);
        if (!res)
            event.replyEmbeds(SupportifyEmbedUtils.embedMessage("You do not have enough permissions to run this command!").build())
                    .setEphemeral(true)
                    .queue();
        return res;
    }

    protected boolean botPermsCheck(SlashCommandInteractionEvent event) {
        if (command == null)
            buildCommand();
        if (command.botRequiredPermissions.isEmpty())
            return true;

        final Guild guild = event.getGuild();
        final var self = guild.getSelfMember();
        if (!self.hasPermission(command.botRequiredPermissions)) {
            event.replyEmbeds(SupportifyEmbedUtils.embedMessage("I do not have enough permissions to do this\n" +
                            "Please give my role the following permission(s):\n\n" +
                            "`"+GeneralUtils.listToString(command.botRequiredPermissions)+"`\n\n" +
                            "*For the recommended permissions please invite the bot by clicking the button below*").build())
                    .addActionRow(
                            Button.of(ButtonStyle.LINK, "https://discord.com/oauth2/authorize?client_id=893558050504466482&permissions=269479308656&scope=bot%20applications.commands", "Give Permissions! (Requires Manage Server)")
                    )
                    .setEphemeral(false)
                    .queue();
            return false;
        }
        return true;
    }

    protected boolean botEmbedCheck(SlashCommandInteractionEvent event) {
        if (!event.getGuild().getSelfMember().hasPermission(event.getGuildChannel(), net.dv8tion.jda.api.Permission.MESSAGE_EMBED_LINKS)) {
            event.reply("""
                                    ⚠️ I do not have permissions to send embeds!

                                    Please enable the `Embed Links` permission for my role in this channel in order for my commands to work!""")
                    .queue();
            return false;
        }
        return true;
    }

    protected abstract void buildCommand();
    public abstract String getHelp();

    public String getUsages() {
        return null;
    }

    protected Builder getBuilder() {
        return new Builder();
    }

    protected static class Command {
        @Getter
        @NotNull
        private final String name;
        @Getter @NotNull
        private final String description;
        @Getter @NotNull
        private final List<CommandOption> options;
        @Getter @NotNull
        private final List<SubCommandGroup> subCommandGroups;
        @Getter @NotNull
        private final List<SubCommand> subCommands;
        @Getter
        private final List<net.dv8tion.jda.api.Permission> botRequiredPermissions;
        @Nullable
        @Getter
        private final Predicate<SlashCommandInteractionEvent> checkPermission;

        private Command(@NotNull String name, @Nullable String description, @NotNull List<CommandOption> options,
                        @NotNull List<SubCommandGroup> subCommandGroups, @NotNull List<SubCommand> subCommands, @Nullable Predicate<SlashCommandInteractionEvent> checkPermission,
                        List<net.dv8tion.jda.api.Permission> botRequiredPermissions) {
            this.name = name.toLowerCase();
            this.description = description;
            this.options = options;
            this.subCommandGroups = subCommandGroups;
            this.subCommands = subCommands;
            this.checkPermission = checkPermission;
            this.botRequiredPermissions = botRequiredPermissions;
        }

        public boolean permissionCheck(SlashCommandInteractionEvent e) {
            if (checkPermission == null)
                throw new NullPointerException("Can't perform permission check since a check predicate was not provided!");

            return checkPermission.test(e);
        }
    }

    protected static class SubCommand {
        @NotNull @Getter
        private final String name;
        @Nullable @Getter
        private final String description;
        @Getter
        private final List<CommandOption> options;


        public SubCommand(@NotNull String name, @Nullable String description, @NotNull List<CommandOption> options) {
            this.name = name.toLowerCase();
            this.description = description;
            this.options = options;
        }

        public static SubCommand of(String name, String description, List<CommandOption> options) {
            return new SubCommand(name, description, options);
        }

        public static SubCommand of(String name, String description) {
            return new SubCommand(name, description, List.of());
        }
    }

    protected static class SubCommandGroup {
        @NotNull
        private final String name;
        private final String description;
        private final List<SubCommand> subCommands;

        private SubCommandGroup(@NotNull String name, String description, List<SubCommand> subCommands) {
            this.name = name;
            this.description = description;
            this.subCommands = subCommands;
        }

        public static SubCommandGroup of(String name, String description, List<SubCommand> subCommands) {
            return new SubCommandGroup(name, description, subCommands);
        }

        @SneakyThrows
        public SubcommandGroupData build() {

            SubcommandGroupData data = new SubcommandGroupData(name, description);

            for (var command : subCommands) {
                SubcommandData subcommandData = new SubcommandData(command.name, command.description);

                for (var option : command.getOptions()) {
                    OptionData optionData = new OptionData(
                            option.getType(),
                            option.getName(),
                            option.getDescription(),
                            option.isRequired()
                    );

                    if (option.getChoices() != null)
                        for (final var choice : option.getChoices())
                            optionData.addChoice(choice, choice);
                    subcommandData.addOptions(optionData);
                };

                data.addSubcommands(subcommandData);
            }

            return data;
        }
    }

    protected static class CommandOption {
        @Getter
        private final OptionType type;
        @Getter
        private final String name;
        @Getter
        private final String description;
        @Getter
        private final boolean required;
        @Getter
        private final List<String> choices;

        private CommandOption(OptionType type, String name, String description, boolean required, List<String> choices) {
            this.type = type;
            this.name = name.toLowerCase();
            this.description = description;
            this.required = required;
            this.choices = choices;
        }

        public static CommandOption of(OptionType type, String name, String description, boolean required) {
            return new CommandOption(type, name, description, required, null);
        }

        public static CommandOption of(OptionType type, String name, String description, boolean required, List<String> choices) {
            return new CommandOption(type, name, description, required, choices);
        }
    }

    protected static class Builder {
        private String name, description;
        private final List<CommandOption> options;
        private final List<SubCommand> subCommands;
        private final List<SubCommandGroup> subCommandGroups;
        private final List<Permission> botRequiredPermissions;
        private Predicate<SlashCommandInteractionEvent> permissionCheck;
        private boolean isPrivate;

        private Builder() {
            this.options = new ArrayList<>();
            this.subCommands = new ArrayList<>();
            this.subCommandGroups = new ArrayList<>();
            this.botRequiredPermissions = new ArrayList<>();
            this.isPrivate = false;
        }

        public Builder setName(@NotNull String name) {
            this.name = name.toLowerCase();
            return this;
        }

        public Builder setDescription(@NotNull String description) {
            this.description = description;
            return this;
        }

        public Builder addOptions(CommandOption... options) {
            this.options.addAll(Arrays.asList(options));
            return this;
        }

        public Builder addSubCommands(SubCommand... subCommands) {
            this.subCommands.addAll(Arrays.asList(subCommands));
            return this;
        }

        public Builder addSubCommandGroups(SubCommandGroup... subCommandGroups) {
            this.subCommandGroups.addAll(Arrays.asList(subCommandGroups));
            return this;
        }

        public Builder setPermissionCheck(Predicate<SlashCommandInteractionEvent> predicate) {
            this.permissionCheck = predicate;
            return this;
        }

        public Builder setBotRequiredPermissions(Permission... permissions) {
            botRequiredPermissions.addAll(Arrays.asList(permissions));
            return this;
        }

        @SneakyThrows
        public Command build() {
            if (name == null)
                throw new InvalidBuilderException("The name of the command can't be null!");
            if (name.isBlank())
                throw new InvalidBuilderException("The name of the command can't be empty!");
            if (description == null)
                throw new InvalidBuilderException("The description of the command can't be null!");
            if (description.isBlank())
                throw new InvalidBuilderException("The description of the command can't be empty!");
            if (!options.isEmpty() && (!subCommands.isEmpty() || !subCommandGroups.isEmpty()))
                throw new InvalidBuilderException("You can't provide command options with subcommands and/or subcommand groups!");

            return new Command(
                    name,
                    description,
                    options,
                    subCommandGroups,
                    subCommands,
                    permissionCheck,
                    botRequiredPermissions
            );
        }
    }
}
