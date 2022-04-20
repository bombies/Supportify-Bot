package utils.json.tickets;

import lombok.SneakyThrows;
import main.Supportify;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.json.JSONObject;
import utils.database.mongodb.databases.GuildDB;
import utils.json.AbstractGuildConfig;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;

public class TicketConfig extends AbstractGuildConfig {

    public void createCreator(long gid, long cid, long mid, String description, String emoji) {
        if (creatorExists(gid))
            throw new IllegalStateException("This guild already has a ticket creator!");

        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());

        ticketObj.put(GuildDB.Field.Tickets.CREATOR_CHANNEL.toString(), cid);
        ticketObj.put(GuildDB.Field.Tickets.CREATOR_MESSAGE.toString(), mid);
        ticketObj.put(GuildDB.Field.Tickets.CREATOR_MESSAGE_DESCRIPTION.toString(), description);
        ticketObj.put(GuildDB.Field.Tickets.CREATOR_MESSAGE_EMOJI.toString(), emoji);

        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);
    }

    public void removeCreator(long gid) {
        if (!creatorExists(gid))
            throw new IllegalStateException("This guild already doesn't have a ticket creator!");

        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());

        ticketObj.put(GuildDB.Field.Tickets.CREATOR_CHANNEL.toString(), -1L);
        ticketObj.put(GuildDB.Field.Tickets.CREATOR_MESSAGE.toString(), -1L);
        ticketObj.put(GuildDB.Field.Tickets.CREATOR_MESSAGE_DESCRIPTION.toString(), "");
        ticketObj.put(GuildDB.Field.Tickets.CREATOR_MESSAGE_EMOJI.toString(), "");

        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);
    }

    public TicketCreator getCreator(long gid) {
        if (!creatorExists(gid))
            throw new IllegalStateException("This guild doesn't have a ticket creator!");

        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());

        return new TicketCreator(
                gid,
                ticketObj.getLong(GuildDB.Field.Tickets.CREATOR_CHANNEL.toString()),
                ticketObj.getLong(GuildDB.Field.Tickets.CREATOR_MESSAGE.toString()),
                ticketObj.getString(GuildDB.Field.Tickets.CREATOR_MESSAGE_DESCRIPTION.toString()),
                ticketObj.getString(GuildDB.Field.Tickets.CREATOR_MESSAGE_EMOJI.toString())
        );
    }

    public void setCreatorDescription(long gid, String description) {
        if (!creatorExists(gid))
            throw new IllegalStateException("This guild doesn't have a ticket creator!");

        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());

        ticketObj.put(GuildDB.Field.Tickets.CREATOR_MESSAGE_DESCRIPTION.toString(), description);
        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);
    }

    public void setCreatorEmoji(long gid, String emoji) {
        if (!creatorExists(gid))
            throw new IllegalStateException("This guild doesn't have a ticket creator!");

        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());

        ticketObj.put(GuildDB.Field.Tickets.CREATOR_MESSAGE_EMOJI.toString(), emoji);
        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);
    }

    public boolean creatorExists(long gid) {
        if (!guildHasInfo(gid))
            return false;

        return getGuildObject(gid)
                .getJSONObject(GuildDB.Field.Tickets.INFO.toString())
                .getLong(GuildDB.Field.Tickets.CREATOR_CHANNEL.toString()) != -1;
    }

    public int getTicketCount(long gid) {
        final var obj = getGuildObject(gid);
        return obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString())
                .getInt(GuildDB.Field.Tickets.TOTAL_COUNT.toString());
    }

    private void incrementTicketCount(long gid) {
        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        int count = ticketObj.getInt(GuildDB.Field.Tickets.TOTAL_COUNT.toString());

        ticketObj.put(GuildDB.Field.Tickets.TOTAL_COUNT.toString(), ++count);
        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);
    }

    private int incrementAndGetTicketCount(long gid) {
        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        int count = ticketObj.getInt(GuildDB.Field.Tickets.TOTAL_COUNT.toString());

        ticketObj.put(GuildDB.Field.Tickets.TOTAL_COUNT.toString(), ++count);
        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);

        return count;
    }

    public void openTicket(long gid, long cid, long owner) {
        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        final var openedTickets = ticketObj.getJSONArray(GuildDB.Field.Tickets.OPENED_TICKETS.toString());

        openedTickets.put(new JSONObject()
                .put(GuildDB.Field.Tickets.OWNER.toString(), owner)
                .put(GuildDB.Field.Tickets.TIME_OPENED.toString(), System.currentTimeMillis())
                .put(GuildDB.Field.Tickets.ID.toString(), incrementAndGetTicketCount(gid))
                .put(GuildDB.Field.Tickets.CHANNEL.toString(), cid)
        );

        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);
    }

    public void closeTicket(long gid, long cid) {
        if (!isOpenedTicket(gid, cid))
            throw new IllegalStateException("The ticket passed isn't an opened ticket!");

        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        final var openedTickets = ticketObj.getJSONArray(GuildDB.Field.Tickets.OPENED_TICKETS.toString());

        openedTickets.remove(getIndexOfObjectInArray(openedTickets, GuildDB.Field.Tickets.CHANNEL, cid));

        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);
    }

    public void closeTicket(Ticket ticket) {
        closeTicket(ticket.guildID, ticket.channelID);
    }

    public boolean isOpenedTicket(long gid, long cid) {
        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        final var openedTickets = ticketObj.getJSONArray(GuildDB.Field.Tickets.OPENED_TICKETS.toString());

        return arrayHasObject(openedTickets, GuildDB.Field.Tickets.CHANNEL, cid);
    }

    public boolean isOpenedTicket(long gid, int id) {
        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        final var openedTickets = ticketObj.getJSONArray(GuildDB.Field.Tickets.OPENED_TICKETS.toString());

        return arrayHasObject(openedTickets, GuildDB.Field.Tickets.ID, id);
    }

    public boolean isOpenedTicket(Ticket ticket) {
        return isOpenedTicket(ticket.guildID, ticket.channelID);
    }

    public Ticket getTicket(long gid, int id) {
        if (!isOpenedTicket(gid, id))
            throw new IllegalStateException("The ticket passed isn't an opened ticket!");

        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        final var openedTickets = ticketObj.getJSONArray(GuildDB.Field.Tickets.OPENED_TICKETS.toString());

        JSONObject openedTicketObj = openedTickets.getJSONObject(getIndexOfObjectInArray(openedTickets, GuildDB.Field.Tickets.ID, id));

        return new Ticket(
                gid,
                openedTicketObj.getLong(GuildDB.Field.Tickets.OWNER.toString()),
                openedTicketObj.getLong(GuildDB.Field.Tickets.TIME_OPENED.toString()),
                openedTicketObj.getInt(GuildDB.Field.Tickets.ID.toString()),
                openedTicketObj.getLong(GuildDB.Field.Tickets.CHANNEL.toString())
        );
    }

    public Ticket getTicket(long gid, long cid) {
        if (!isOpenedTicket(gid, cid))
            throw new IllegalStateException("The ticket passed isn't an opened ticket!");

        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        final var openedTickets = ticketObj.getJSONArray(GuildDB.Field.Tickets.OPENED_TICKETS.toString());

        JSONObject openedTicketObj = openedTickets.getJSONObject(getIndexOfObjectInArray(openedTickets, GuildDB.Field.Tickets.CHANNEL, cid));

        return new Ticket(
                gid,
                openedTicketObj.getLong(GuildDB.Field.Tickets.OWNER.toString()),
                openedTicketObj.getLong(GuildDB.Field.Tickets.TIME_OPENED.toString()),
                openedTicketObj.getInt(GuildDB.Field.Tickets.ID.toString()),
                openedTicketObj.getLong(GuildDB.Field.Tickets.CHANNEL.toString())
        );
    }

    public List<Ticket> getTickets(long gid) {
        final List<Ticket> tickets = new ArrayList<>();
        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        final var openedTickets = ticketObj.getJSONArray(GuildDB.Field.Tickets.OPENED_TICKETS.toString());

        for (final var o : openedTickets) {
            final var openedTicketObj = (JSONObject) o;
            tickets.add(new Ticket(
                    gid,
                    openedTicketObj.getLong(GuildDB.Field.Tickets.OWNER.toString()),
                    openedTicketObj.getLong(GuildDB.Field.Tickets.TIME_OPENED.toString()),
                    openedTicketObj.getInt(GuildDB.Field.Tickets.ID.toString()),
                    openedTicketObj.getLong(GuildDB.Field.Tickets.CHANNEL.toString())
            ));
        }
        return tickets;
    }

    public void setSupportRole(long gid, long rid) {
        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        ticketObj.put(GuildDB.Field.Tickets.SUPPORT_ROLE.toString(), rid);

        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);
    }

    public long getSupportRole(long gid) {
        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        return ticketObj.getLong(GuildDB.Field.Tickets.SUPPORT_ROLE.toString());
    }

    public boolean isSupportRole(long gid, long rid) {
        return getSupportRole(gid) == rid;
    }

    public boolean isSupportRole(Role role) {
        return isSupportRole(role.getGuild().getIdLong(), role.getIdLong());
    }

    public boolean isSupportMember(long gid, long uid) {
        return isSupportMember(Supportify.getApi().getGuildById(gid).retrieveMemberById(uid).complete());
    }

    public boolean isSupportMember(Member member) {
        for (final var role : member.getRoles())
            if (isSupportRole(role))
                return true;
        return false;
    }

    public void setLogChannel(long gid, long cid) {
        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        ticketObj.put(GuildDB.Field.Tickets.LOG_CHANNEL.toString(), cid);

        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);
    }

    public long getLogChannel(long gid, long cid) {
        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        return ticketObj.getLong(GuildDB.Field.Tickets.LOG_CHANNEL.toString());
    }

    public void addSupportMember(long gid, long uid) {
        if (!isSupportMember(gid, uid))
            throw new IllegalStateException("This user doesn't have the support role!");
        if (isSupportMemberInArr(gid, uid))
            throw new IllegalArgumentException("This user already has information in the database!");

        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        final var supportTeamArr = ticketObj.getJSONArray(GuildDB.Field.Tickets.SUPPORT_TEAM_INFO.toString());

        supportTeamArr.put(new JSONObject()
                .put(GuildDB.Field.Tickets.SUPPORT_USER_ID.toString(), uid)
                .put(GuildDB.Field.Tickets.SUPPORT_CLOSES.toString(), 0)
                .put(GuildDB.Field.Tickets.SUPPORT_MESSAGES.toString(), 0)
        );

        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);
    }

    public SupportTeamMember getSupportMember(long gid, long uid) {
        if (!isSupportMemberInArr(gid, uid))
            throw new IllegalArgumentException("This user doesn't have information in the database!");

        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        final var supportTeamArr = ticketObj.getJSONArray(GuildDB.Field.Tickets.SUPPORT_TEAM_INFO.toString());

        JSONObject memberObj = supportTeamArr.getJSONObject(getIndexOfObjectInArray(supportTeamArr, GuildDB.Field.Tickets.SUPPORT_USER_ID, uid));
        return new SupportTeamMember(
                gid,
                uid,
                memberObj.getInt(GuildDB.Field.Tickets.SUPPORT_CLOSES.toString()),
                memberObj.getInt(GuildDB.Field.Tickets.SUPPORT_MESSAGES.toString())
        );
    }

    public void incrementSupportMemberStats(SupportTeamMember member, SupportStat stat, int step) {
        incrementSupportMemberStats(member.guildID, member.userID, stat, step);
    }

    @SneakyThrows
    public void incrementSupportMemberStats(long gid, long uid, SupportStat stat, int step) {
        GuildDB.Field.Tickets field;
        switch (stat) {
            case CLOSES -> field = GuildDB.Field.Tickets.SUPPORT_CLOSES;
            case MESSAGES -> field = GuildDB.Field.Tickets.SUPPORT_MESSAGES;
            default -> throw new UnexpectedException("Unexpected error");
        }

        if (!isSupportMemberInArr(gid, uid))
            throw new IllegalArgumentException("This user doesn't have information in the database!");

        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        final var supportTeamArr = ticketObj.getJSONArray(GuildDB.Field.Tickets.SUPPORT_TEAM_INFO.toString());

        JSONObject memberObj = supportTeamArr.getJSONObject(getIndexOfObjectInArray(supportTeamArr, GuildDB.Field.Tickets.SUPPORT_USER_ID, uid));

        memberObj.put(field.toString(), memberObj.getInt(field.toString()) + step);
        getCache().setField(gid, GuildDB.Field.Tickets.INFO, obj);
    }

    private boolean isSupportMemberInArr(long gid, long uid) {
        final var obj = getGuildObject(gid);
        final var ticketObj = obj.getJSONObject(GuildDB.Field.Tickets.INFO.toString());
        final var supportTeamArr = ticketObj.getJSONArray(GuildDB.Field.Tickets.SUPPORT_TEAM_INFO.toString());
        return arrayHasObject(supportTeamArr, GuildDB.Field.Tickets.SUPPORT_USER_ID, uid);
    }

    @Override
    public void update(long gid) {

    }

    private enum SupportStat {
        CLOSES,
        MESSAGES
    }
}
