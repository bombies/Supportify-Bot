package commands;

import main.Config;

public interface IDevCommand extends ICommand {

    default boolean permissionCheck(CommandContext ctx) {
        return ctx.getAuthor().getIdLong() == Config.getOwnerID();
    }
}
