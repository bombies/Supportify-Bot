package commands;

import constants.ENV;
import lombok.Getter;
import main.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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

    public void handle(MessageReceivedEvent e) {
        String[] split = e.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(Config.get(ENV.DEFAULT_PREFIX)), "")
                .split("\\s+");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if (cmd != null) {
            final List<String> args = Arrays.asList(split).subList(1, split.length);
            final CommandContext ctx = new CommandContext(e, args);
            final Guild guild = e.getGuild();

            if (!guild.getSelfMember().hasPermission(ctx.getChannel(), net.dv8tion.jda.api.Permission.MESSAGE_EMBED_LINKS)) {
                e.getChannel().sendMessage("""
                                    ⚠️ I do not have permissions to send embeds!

                                    Please enable the `Embed Links` permission for my role in this channel in order for my commands to work!""")
                        .queue();
                return;
            }

            cmd.handle(ctx);
        }
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
