package commands.dev.configurator;

import net.dv8tion.jda.api.entities.Emoji;
import utils.SupportifyEmbedUtils;
import utils.component.configurator.AbstractConfigurator;
import utils.component.configurator.ConfiguratorBuilder;
import utils.component.configurator.ConfiguratorOptionBuilder;

public class TestConfigurator extends AbstractConfigurator {
    public TestConfigurator() {
        super(new ConfiguratorBuilder()
                .addOption(new ConfiguratorOptionBuilder()
                        .setID("testconfigbutton")
                        .setLabel("Button!")
                        .setEmoji(Emoji.fromUnicode("🏳️‍🌈"))
                        .setEventHandler(event -> {
                            event.replyEmbeds(SupportifyEmbedUtils.embedMessage("Event handled!").build()).queue();
                            return null;
                        })
                        .build()
                )
                .setEmbed(SupportifyEmbedUtils.embedMessage("Test Configurator").build())
        );
    }
}
