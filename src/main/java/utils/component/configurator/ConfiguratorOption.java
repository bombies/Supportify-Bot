package utils.component.configurator;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;
import java.util.function.Function;

public class ConfiguratorOption {
    @Getter
    private final String label, id;
    @Getter
    private final Emoji emoji;
    @Getter
    private final Function<ButtonInteractionEvent, Void> eventHandler;
    @Getter
    private final List<Function<GenericEvent, Void>> secondaryEventHandlers;

    protected ConfiguratorOption(String id, String label,
                                 Emoji emoji, Function<ButtonInteractionEvent, Void> eventHandler,
                                 List<Function<GenericEvent, Void>> secondaryEventHandlers) {
        this.id = id;
        this.label = label;
        this.emoji = emoji;
        this.eventHandler = eventHandler;
        this.secondaryEventHandlers = secondaryEventHandlers;
    }
}
