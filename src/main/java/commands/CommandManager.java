package commands;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private final static Logger logger = LoggerFactory.getLogger(CommandManager.class);

    @Getter
    private final List<ICommand> generalCommands = new ArrayList<>();
    @Getter
    private final List<IDevCommand> devCommands = new ArrayList<>();

    public CommandManager() {
        addGeneralCommands(

        );

        addDevCommands(

        );
    }

    private void addGeneralCommands(ICommand... commands) {
        generalCommands.addAll(List.of(commands));
    }

    private void addDevCommands(IDevCommand... commands) {
        devCommands.addAll(List.of(commands));
    }

    public boolean isGeneralCommand(ICommand command) {
        return getGeneralCommands()
                .stream()
                .anyMatch(it -> it.getName().equalsIgnoreCase(command.getName()));
    }

    public boolean isDevCommand(IDevCommand command) {
        return getDevCommands()
                .stream()
                .anyMatch(it -> it.getName().equalsIgnoreCase(command.getName()));
    }

    public boolean isDevCommand(String name) {
        return getDevCommands()
                .stream()
                .anyMatch(it -> it.getName().equalsIgnoreCase(name));
    }

    public List<ICommand> getCommands() {
        return new ArrayList<>(generalCommands);
    }

    public ICommand getCommand(String name) {
        return getCommands().stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public IDevCommand getDevCommand(String name) {
        return getDevCommands().stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
