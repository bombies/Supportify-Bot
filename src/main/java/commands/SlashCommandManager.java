package commands;

import commands.general.tickets.RenameCommand;
import commands.general.tickets.TicketCommand;
import commands.general.tickets.CloseCommand;
import commands.general.privatevoicechannels.PrivateChannelCommand;
import commands.misc.PingCommand;
import commands.utility.UptimeCommand;
import lombok.Getter;
import utils.component.interactions.AbstractSlashCommand;

import java.util.ArrayList;
import java.util.List;

public class SlashCommandManager {

    @Getter
    private final List<AbstractSlashCommand> generalCommands = new ArrayList<>();
    @Getter
    private final List<AbstractSlashCommand> miscCommands = new ArrayList<>();
    @Getter
    private final List<AbstractSlashCommand> utilityCommands = new ArrayList<>();
    @Getter
    private final List<AbstractSlashCommand> devCommands = new ArrayList<>();

    public SlashCommandManager() {
        addGeneralCommands(
                new TicketCommand(),
                new RenameCommand(),
                new CloseCommand(),
                new PrivateChannelCommand()
        );

        addMiscCommands(
            new PingCommand()
        );

        addUtilityommands(
            new UptimeCommand()
        );

        addDevCommands(

        );
    }

    private void addGeneralCommands(AbstractSlashCommand... commands) {
        generalCommands.addAll(List.of(commands));
    }

    private void addMiscCommands(AbstractSlashCommand... commands) {
        miscCommands.addAll(List.of(commands));
    }

    private void addUtilityommands(AbstractSlashCommand... commands) {
        utilityCommands.addAll(List.of(commands));
    }

    private void addDevCommands(AbstractSlashCommand... commands) {
        devCommands.addAll(List.of(commands));
    }

    public boolean isGeneralCommand(AbstractSlashCommand command) {
        return getGeneralCommands()
                .stream()
                .anyMatch(it -> it.getName().equalsIgnoreCase(command.getName()));
    }

    public boolean isMiscCommand(AbstractSlashCommand command) {
        return getMiscCommands()
                .stream()
                .anyMatch(it -> it.getName().equalsIgnoreCase(command.getName()));
    }

    public boolean isUtilityCommand(AbstractSlashCommand command) {
        return getUtilityCommands()
                .stream()
                .anyMatch(it -> it.getName().equalsIgnoreCase(command.getName()));
    }

    public boolean isDevCommand(AbstractSlashCommand command) {
        return getDevCommands()
                .stream()
                .anyMatch(it -> it.getName().equalsIgnoreCase(command.getName()));
    }

    public boolean isDevCommand(String name) {
        return getDevCommands()
                .stream()
                .anyMatch(it -> it.getName().equalsIgnoreCase(name));
    }

    public List<AbstractSlashCommand> getCommands() {
        final List<AbstractSlashCommand> commands = new ArrayList<>();
        commands.addAll(generalCommands);
        commands.addAll(miscCommands);
        commands.addAll(utilityCommands);
        return commands;
    }

    public AbstractSlashCommand getCommand(String name) {
        return getCommands().stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public AbstractSlashCommand getDevCommand(String name) {
        return getDevCommands().stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
