package commands.dev.configurator;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import utils.SupportifyEmbedUtils;
import utils.component.configurator.AbstractConfigurator;
import utils.component.configurator.ConfiguratorBuilder;
import utils.component.configurator.ConfiguratorOptionBuilder;

import java.util.concurrent.ExecutionException;

public class TestConfigurator extends AbstractConfigurator {
    public TestConfigurator() {
        super(new ConfiguratorBuilder()
                .addOption(new ConfiguratorOptionBuilder()
                        .setID("testconfigbutton")
                        .setLabel("Button!")
                        .setEmoji(Emoji.fromUnicode("🏳️‍🌈"))
                        .setButtonEventHandler(event ->
                                event.replyEmbeds(SupportifyEmbedUtils.embedMessage("Event handled!").build())
                                        .addActionRow(Button.of(ButtonStyle.PRIMARY, "secondbuttontest", "Second Button"))
                                        .queue(success -> {
                                            try {
                                                AbstractConfigurator.addOwner(event.getUser(), success.retrieveOriginal().submit().get());
                                            } catch (InterruptedException | ExecutionException e) {
                                                e.printStackTrace();
                                            }
                                        })
                        )
                        .addSecondaryInteractionEventHandler(
                                ButtonInteractionEvent.class,
                                e -> e.getButton().getId().equals("secondbuttontest"),
                                e -> e.replyEmbeds(SupportifyEmbedUtils.embedMessage("Secondary button clicked").build()).queue()
                        )
                        .build()
                )
                .setEmbed(SupportifyEmbedUtils.embedMessage("Test Configurator").build())
        );
    }
}
