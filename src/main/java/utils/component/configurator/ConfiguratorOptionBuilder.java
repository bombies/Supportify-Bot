package utils.component.configurator;

import lombok.SneakyThrows;
import main.Supportify;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import utils.component.InvalidBuilderException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConfiguratorOptionBuilder {
    private String label, id;
    private Emoji emoji;
    private Function<ButtonInteractionEvent, Void> eventHandler;
    private List<Function<GenericEvent, Void>> secondaryEventHandlers = new ArrayList<>();

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

    public ConfiguratorOptionBuilder setOwner(User owner) {
        return this;
    }

    /**
     * Remember at the end of each handler null is to be returned
     * @param eventHandler The piece of code to execute when an event is fired
     * @return This object
     */
    public ConfiguratorOptionBuilder setEventHandler(Function<ButtonInteractionEvent, Void> eventHandler) {
        this.eventHandler = eventHandler;
        return this;
    }

    public ConfiguratorOptionBuilder addSecondaryEventHandler(Function<GenericEvent, Void> eventHandler) {
        secondaryEventHandlers.add(eventHandler);
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
                this.secondaryEventHandlers
        );
    }
}
