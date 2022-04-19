package utils.component.interactions.selectionmenu;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.component.InvalidBuilderException;

import java.util.function.Predicate;

public class SelectionMenuOption {
    @Getter
    private final String label;
    @Getter
    private final String value;
    @Getter
    private final Emoji emoji;
    @Getter
    private final Predicate<SelectMenuInteractionEvent> predicate;

    private SelectionMenuOption(@NotNull String label, @NotNull String value, @Nullable Emoji emoji, @Nullable Predicate<SelectMenuInteractionEvent> predicate) {
        this.label = label;
        this.value = value;
        this.emoji = emoji;
        this.predicate = predicate;
    }

    public static OptionBuilder create() {
        return new OptionBuilder();
    }

    public static SelectionMenuOption of(@NotNull String label, @NotNull String id, @Nullable Emoji emoji, @Nullable Predicate<SelectMenuInteractionEvent> predicate) {
        return new SelectionMenuOption(label, id, emoji, predicate);
    }

    public static SelectionMenuOption of(String label, String id, Emoji emoji) {
        return of(label, id, emoji, null);
    }

    public static SelectionMenuOption of(String label, String id) {
        return of(label, id, null);
    }

    public static class OptionBuilder {
        @Setter
        private String label;
        @Setter
        private String id;
        @Setter
        private Emoji emoji;
        @Setter
        private Predicate<SelectMenuInteractionEvent> predicate;

        private OptionBuilder() {}

        @SneakyThrows
        public SelectionMenuOption build() {
            if (label == null || label.isEmpty())
                throw new InvalidBuilderException("The label must not be null or empty!");

            if (id == null || id.isEmpty())
                throw new InvalidBuilderException("The ID must not be null or empty!");

            return new SelectionMenuOption(label, id, emoji, predicate);
        }
    }

    @Override
    public String toString() {
        return label;
    }
}
