package utils.component.configurator;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import utils.MultiValueMap;
import utils.SupportifyEmbedUtils;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ConfiguratorOption {
    @Getter
    private final String label, id;
    @Getter
    private final Emoji emoji;
    @Getter
    private final Consumer<ButtonInteractionEvent> eventHandler;
    @Getter
    private final MultiValueMap<Class<?>, ConfiguratorOption.SecondaryEvent> secondaryEventHandlers;

    protected ConfiguratorOption(String id, String label,
                                 Emoji emoji, Consumer<ButtonInteractionEvent> eventHandler,
                                 MultiValueMap<Class<?>, ConfiguratorOption.SecondaryEvent> secondaryEventHandlers) {
        this.id = id;
        this.label = label;
        this.emoji = emoji;
        this.eventHandler = eventHandler;
        this.secondaryEventHandlers = secondaryEventHandlers;
    }

    protected static class SecondaryEvent<T extends GenericEvent> {
        private final Predicate<T> condition;
        private final Predicate<T> componentCondition;
        private final Consumer<T> action;


        public SecondaryEvent(Predicate<T> condition, Consumer<T> action) {
            this.condition = condition;
            this.action = action;
            this.componentCondition = null;
        }

        public SecondaryEvent(Predicate<T> condition, Predicate<T> componentCondition, Consumer<T> action) {
            this.condition = condition;
            this.componentCondition = componentCondition;
            this.action = action;
        }

        boolean attempt(T event) {
            if (event instanceof ButtonInteractionEvent buttonEvent) {
                if (condition == null) {
                    try {
                        if (AbstractConfigurator.isOwner(buttonEvent.getUser(), buttonEvent.getMessage())) {
                            action.accept(event);
                            return true;
                        } else return false;
                    } catch (NullPointerException e) {
                        buttonEvent.editComponents(ActionRow.of(buttonEvent.getButton().asDisabled())).queue(success ->
                                success.sendMessageEmbeds(SupportifyEmbedUtils.embedMessage("This button is no longer valid!").build())
                                        .setEphemeral(true)
                                        .queue()
                        );
                        return true; // Returning true to avoid a double acknowledgement
                    }
                }
            }

            if (event instanceof GenericInteractionCreateEvent) {
                if (componentCondition != null) {
                    if (componentCondition.test(event)) {
                        action.accept(event);
                        return true;
                    }
                    return false;
                } else return true;
            }
            if (condition.test(event)) {
                action.accept(event);
                return true;
            }
            return false;
        }

        boolean isMatchingComponent(T event) {
            if (!(event instanceof GenericInteractionCreateEvent))
                throw new IllegalArgumentException("The event passed isn't a valid interaction event!");

            if (componentCondition == null)
                return true;

            return componentCondition.test(event);
        }
    }
}
