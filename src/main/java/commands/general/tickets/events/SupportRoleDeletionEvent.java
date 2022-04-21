package commands.general.tickets.events;

import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utils.json.tickets.TicketConfig;

public class SupportRoleDeletionEvent extends ListenerAdapter {

    @Override
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        final var config = new TicketConfig();
        final var guild = event.getGuild();

        if (!config.supportRoleIsSet(guild.getIdLong()))
            return;

        final var role = event.getRole();

        if (config.getSupportRole(guild.getIdLong()) != role.getIdLong())
            return;

        config.removeSupportRole(guild.getIdLong());
    }
}
