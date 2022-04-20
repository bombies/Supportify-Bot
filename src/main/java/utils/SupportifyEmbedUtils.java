package utils;

import constants.BotConstants;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.function.Supplier;

public class SupportifyEmbedUtils {
    private static Supplier<EmbedBuilder> embedSupplier = EmbedBuilder::new;

    public static void setEmbedBuilder(Supplier<EmbedBuilder> supplier) {
        embedSupplier = supplier;
    }

    public static EmbedBuilder getEmbedBuilder() {
        return embedSupplier.get();
    }

    public static EmbedBuilder embedMessage(String message) {
        return getDefaultEmbed().setDescription(message);
    }

    public static EmbedBuilder embedMessageWithTitle(String title, String message) {
        return getDefaultEmbed().setTitle(title).setDescription(message);
    }

    public static EmbedBuilder embedMessageWithAuthor(String author, String message) {
        return getDefaultEmbed().setAuthor(author, null, BotConstants.EMBED_LOGO.toString()).setDescription(message);
    }

    private static EmbedBuilder getDefaultEmbed() {
            return embedSupplier.get();
    }
}
