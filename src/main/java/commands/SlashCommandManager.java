package commands;

import lombok.Getter;
import utils.component.interactions.AbstractSlashCommand;

import java.util.ArrayList;
import java.util.List;

public class SlashCommandManager {

    @Getter
    private final List<AbstractSlashCommand> generalCommands = new ArrayList<>();
    @Getter
    private final List<AbstractSlashCommand> devCommands = new ArrayList<>();

    public SlashCommandManager() {
        addGeneralCommands(

        );

        addDevCommands(

        );
    }

    private void addGeneralCommands(AbstractSlashCommand... commands) {
        generalCommands.addAll(List.of(commands));
    }

    private void addDevCommands(AbstractSlashCommand... commands) {
        devCommands.addAll(List.of(commands));
    }

    public boolean isGeneralCommand(AbstractSlashCommand command) {
        return getGeneralCommands()
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
        return new ArrayList<>(generalCommands);
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
