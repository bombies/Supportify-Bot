package utils.component.configurator;

import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import utils.component.InvalidBuilderException;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ConfiguratorOptionBuilder {
    private String label, id;
    private Emoji emoji;
    private Consumer<ButtonInteractionEvent> eventHandler;
    private HashMap<Class<?>, Set<ConfiguratorOption.SecondaryEvent>> secondaryEventHandlers = new HashMap<>();

    public ConfiguratorOptionBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public ConfiguratorOptionBuilder setID(String id) {
        this.id = id;
        return this;
    }

    public ConfiguratorOptionBuilder setEmoji(Emoji emoji) {
        this.emoji = emoji;
        return this;
    }

    public ConfiguratorOptionBuilder setButtonEventHandler(Consumer<ButtonInteractionEvent> eventHandler) {
        this.eventHandler = eventHandler;
        return this;
    }

    public <T extends Event> ConfiguratorOptionBuilder addSecondaryEventHandler(Class<T> clazz, Predicate<T> condition, Consumer<T> eventHandler) {
        ConfiguratorOption.SecondaryEvent<T> secondaryEvent = new ConfiguratorOption.SecondaryEvent<>(condition, eventHandler);
        Set<ConfiguratorOption.SecondaryEvent> secondaryEvents = secondaryEventHandlers.computeIfAbsent(clazz, c -> new HashSet<>());
        secondaryEvents.add(secondaryEvent);
        return this;
    }

    @SneakyThrows
    public <T extends Event> ConfiguratorOptionBuilder addSecondaryEventHandler(Class<T> clazz, Consumer<T> eventHandler) {
        if (!clazz.equals(ButtonInteractionEvent.class))
            throw new InvalidBuilderException("The predicate can't be null unless the event is a ButtonInteractionEvent!");

        ConfiguratorOption.SecondaryEvent<T> secondaryEvent = new ConfiguratorOption.SecondaryEvent<>(null, eventHandler);
        Set<ConfiguratorOption.SecondaryEvent> secondaryEvents = secondaryEventHandlers.computeIfAbsent(clazz, c -> new HashSet<>());
        secondaryEvents.add(secondaryEvent);
        return this;
    }

    public <T extends Event> ConfiguratorOptionBuilder addSecondaryInteractionEventHandler(Class<T> clazz, Predicate<T> condition, Predicate<T> interactionPredicate, Consumer<T> eventHandler) {
        ConfiguratorOption.SecondaryEvent<T> secondaryEvent = new ConfiguratorOption.SecondaryEvent<>(condition, interactionPredicate, eventHandler);
        Set<ConfiguratorOption.SecondaryEvent> secondaryEvents = secondaryEventHandlers.computeIfAbsent(clazz, c -> new HashSet<>());
        secondaryEvents.add(secondaryEvent);
        return this;
    }

    public <T extends Event> ConfiguratorOptionBuilder addSecondaryInteractionEventHandler(Class<T> clazz, Predicate<T> interactionPredicate, Consumer<T> eventHandler) {
        ConfiguratorOption.SecondaryEvent<T> secondaryEvent = new ConfiguratorOption.SecondaryEvent<>(null, interactionPredicate, eventHandler);
        Set<ConfiguratorOption.SecondaryEvent> secondaryEvents = secondaryEventHandlers.computeIfAbsent(clazz, c -> new HashSet<>());
        secondaryEvents.add(secondaryEvent);
        return this;
    }

    @SneakyThrows
    public ConfiguratorOption build() {
        if (id == null)
            throw new InvalidBuilderException("The id can't be null");
        if (id.isBlank() || id.isEmpty())
            throw new InvalidBuilderException("The id must contain a value");
        if (label == null)
            throw new InvalidBuilderException("The label can't be null");
        if (label.isBlank() || label.isEmpty())
            throw new InvalidBuilderException("The label must contain a value");
        if (emoji == null)
            throw new InvalidBuilderException("The emoji can't be null");
        if (eventHandler == null)
            throw new InvalidBuilderException("You must provide an event handler");
        return new ConfiguratorOption(
                id,
                label,
                emoji,
                eventHandler,
                secondaryEventHandlers
        );
    }
}
