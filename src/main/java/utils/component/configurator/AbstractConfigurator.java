package utils.component.configurator;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.Getter;
import lombok.SneakyThrows;
import main.Supportify;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.SupportifyEmbedUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class AbstractConfigurator extends ListenerAdapter {
    private final Logger logger = LoggerFactory.getLogger(AbstractConfigurator.class);
    private final static HashMap<Long, Long> configOwners = new HashMap<>();

    @Getter
    private final List<ConfiguratorOption> configuratorOptions;
    @Getter
    private final MessageEmbed embed;

    @Getter
    private final EventWaiter waiter;

    protected AbstractConfigurator(List<ConfiguratorOption> configuratorOptions, MessageEmbed embed) {
        this.waiter = Supportify.getEventWaiter();
        this.configuratorOptions = configuratorOptions;
        this.embed = embed;
    }

    @SneakyThrows
    protected AbstractConfigurator(ConfiguratorBuilder configuratorBuilder) {
        final var configurator = configuratorBuilder.build();
        this.configuratorOptions = configurator.getConfiguratorOptions();
        this.embed = configurator.getEmbed();
        this.waiter = Supportify.getEventWaiter();
    }

    @SneakyThrows
    public RestAction<Message> sendMessage(TextChannel channel) {
        return channel.sendMessageEmbeds(embed)
                .setActionRow(getOptions());
    }

    public void sendMessage(SlashCommandInteractionEvent event) {
        event.replyEmbeds(embed)
                .addActionRows(ActionRow.of(getOptions()))
                .queue(success -> {
                    try {
                        addOwner(event.getUser(), success.retrieveOriginal().submit().get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
    }

    private List<Button> getOptions() {
        return configuratorOptions.stream()
                .map(option -> Button.of(ButtonStyle.SECONDARY, option.getId(), option.getLabel(), option.getEmoji()))
                .toList();
    }

    protected static void addOwner(User owner, Message msg) {
        configOwners.put(msg.getIdLong(), owner.getIdLong());
    }

    private static long getOwner(Message msg) {
        if (!configOwners.containsKey(msg.getIdLong()))
            throw new NullPointerException("This message doesn't have a config owner!");
        return configOwners.get(msg.getIdLong());
    }

    protected static boolean isOwner(User owner, Message msg) {
        return isOwner(owner.getIdLong(), msg.getIdLong());
    }

    protected static boolean isOwner(long owner, long msg) {
        return configOwners.get(msg).equals(owner);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        ConfiguratorOption configuratorOption = configuratorOptions.stream()
                .filter(option -> option.getId().equals(event.getButton().getId()))
                .findFirst()
                .orElse(null);
        if (configuratorOption == null) return;

        try {
            if (!isOwner(event.getUser(), event.getMessage())) {
                event.replyEmbeds(SupportifyEmbedUtils.embedMessage("You do not have permission to interact with this button!").build())
                        .setEphemeral(true)
                        .queue();
                return;
            }
        } catch (NullPointerException e) {
            event.editComponents(ActionRow.of(event.getButton().asDisabled())).queue(success ->
                    success.sendMessageEmbeds(SupportifyEmbedUtils.embedMessage("This button is no longer valid!").build())
                            .setEphemeral(true)
                            .queue()
            );
            return;
        }

        configuratorOption.getEventHandler().accept(event);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onGenericEvent(@NotNull GenericEvent event) {
        Class clazz = event.getClass();

        while (clazz != null) {
            for (ConfiguratorOption configuratorOption : configuratorOptions) {
                final var secondaryEventHandlers = configuratorOption.getSecondaryEventHandlers();
                if (secondaryEventHandlers.isEmpty())
                    continue;

                if (!secondaryEventHandlers.containsKey(clazz))
                    continue;

                List<ConfiguratorOption.SecondaryEvent> secondaryEvents = secondaryEventHandlers.get(clazz);

                for (ConfiguratorOption.SecondaryEvent secondaryEvent : secondaryEvents) {
                    if (event instanceof GenericInteractionCreateEvent)
                        if (!secondaryEvent.isMatchingComponent(event))
                            continue;

                    if (!secondaryEvent.attempt(event) && event instanceof ButtonInteractionEvent buttonInteractionEvent)
                        buttonInteractionEvent.replyEmbeds(SupportifyEmbedUtils.embedMessage("You do not have permission to interact wtih this button!").build())
                                .setEphemeral(true)
                                .queue();
                }
            }

            clazz = clazz.getSuperclass();
        }
    }
}
