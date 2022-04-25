package commands.general.welcomer;

import constants.SupportifyEmoji;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import utils.SupportifyEmbedUtils;
import utils.component.configurator.AbstractConfigurator;
import utils.component.configurator.ConfiguratorBuilder;
import utils.component.configurator.ConfiguratorOptionBuilder;
import utils.json.welcomer.WelcomerConfig;

import java.awt.*;
import java.util.concurrent.ExecutionException;

public class WelcomerConfigurator extends AbstractConfigurator {
    private final static String WELCOMER_EMBED_TITLE = "Welcomer Configuration";
    private final static String BUTTON_PREFIX = "welcomerconfig:";
    private final static String BUTTON_EDIT_EMBED_PREFIX = "edit_embed:";

    @SneakyThrows
    public WelcomerConfigurator() {
        super(new ConfiguratorBuilder()
                .setEmbed(
                        new EmbedBuilder()
                                .setColor(new Color(153, 37, 255))
                                .setTitle(WELCOMER_EMBED_TITLE)
                                .setDescription("Welcome to the Welcomer configuration panel!\n" +
                                        "Select any of the below to start editing your configuration")
                                .build()
                )
                // Toggle Button
                .addOption(new ConfiguratorOptionBuilder()
                        .setID(BUTTON_PREFIX +  "toggle")
                        .setLabel("Toggle Welcomer")
                        .setEmoji(SupportifyEmoji.SWITCH.getEmoji())
                        .setButtonEventHandler(event -> {
                            final var guild = event.getGuild();
                            final var config = new WelcomerConfig();

                            if (!config.channelIsSet(guild.getIdLong())) {
                                event.replyEmbeds(SupportifyEmbedUtils.embedMessage("The welcomer channel must be set before" +
                                        " interacting with the toggler!").build())
                                        .setEphemeral(true)
                                        .queue();
                                return;
                            }

                            if (config.isEnabled(guild.getIdLong())) {
                                config.setEnabled(guild.getIdLong(), false);
                                event.replyEmbeds(SupportifyEmbedUtils.embedMessage("You have toggled the welcomer **OFF**").build())
                                        .queue();
                            } else {
                                config.setEnabled(guild.getIdLong(), true);
                                event.replyEmbeds(SupportifyEmbedUtils.embedMessage("You have toggled the welcomer **ON**").build())
                                        .queue();
                            }
                        })
                        .build()
                )

                // Set Channel Button
                .addOption(new ConfiguratorOptionBuilder()
                        .setID(BUTTON_PREFIX + "setchannel")
                        .setLabel("Set Channel")
                        .setEmoji(SupportifyEmoji.CHANNEL.getEmoji())
                        .setButtonEventHandler(event -> {
                            Modal modal = Modal.create("setchannel_modal", "Set Welcomer Channel")
                                    .addActionRow(TextInput.create(
                                            "channel_answer", "ID of Channel", TextInputStyle.SHORT
                                    ).setRequiredRange(18, 18).setRequired(true).build())
                                    .build();
                            event.replyModal(modal).queue();
                        })
                        .addSecondaryInteractionEventHandler(
                                ModalInteractionEvent.class,
                                e -> e.getModalId().equals("setchannel_modal"),
                                e -> {
                                    final var channelID = e.getValue("channel_answer").getAsString();
                                    final var guild = e.getGuild();
                                    final TextChannel channel;
                                    try {
                                        channel = guild.getTextChannelById(channelID);
                                    } catch (NumberFormatException exc) {
                                        e.replyEmbeds(SupportifyEmbedUtils.embedMessage("The value passed isn't a valid ID!")
                                                .build())
                                                .setEphemeral(true)
                                                .queue();
                                        return;
                                    }

                                    if (channel == null) {
                                        e.replyEmbeds(SupportifyEmbedUtils.embedMessage("You've provided an invalid channel ID!").build())
                                                .setEphemeral(true)
                                                .queue();
                                        return;
                                    }

                                    final var config = new WelcomerConfig();
                                    config.setChannel(guild.getIdLong(), channel.getIdLong());
                                    e.replyEmbeds(SupportifyEmbedUtils.embedMessage("You have set the welcomer channel to: " + channel.getAsMention())
                                            .build())
                                            .queue();
                                }
                        )
                        .build()
                )
                // Edit Embed Button
                .addOption(new ConfiguratorOptionBuilder()
                        .setID(BUTTON_PREFIX + "edit_embed")
                        .setLabel("Edit Embed")
                        .setEmoji(SupportifyEmoji.PENCIL.getEmoji())
                        .setButtonEventHandler(e ->
                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                        WELCOMER_EMBED_TITLE + " - Edit Embed",
                                        "Which component of the embed would you like to edit?"
                                )
                                        .setColor(new Color(153, 37, 255))
                                        .build())
                                .addActionRows(
                                        ActionRow.of(
                                                Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "author", "Author", SupportifyEmoji.FEATHER_PEN.getEmoji()),
                                                Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "title", "Title", SupportifyEmoji.TITLE.getEmoji()),
                                                Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "thumbnail", "Thumbnail", SupportifyEmoji.THUMBNAIL.getEmoji()),
                                                Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "description", "Description", SupportifyEmoji.DESCRIPTION.getEmoji()),
                                                Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "fields", "Fields", SupportifyEmoji.FIELDS.getEmoji())
                                                ),
                                        ActionRow.of(
                                                Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "image", "Image", SupportifyEmoji.IMAGE.getEmoji()),
                                                Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "footer", "Footer", SupportifyEmoji.FOOTER.getEmoji()),
                                                Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "timestamp", "Timestamp", SupportifyEmoji.TIMESTAMP.getEmoji()),
                                                Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "colour", "Colour", SupportifyEmoji.COLOUR.getEmoji())
                                                )
                                )
                                .queue(success -> {
                                    try {
                                        AbstractConfigurator.addOwner(e.getUser(), success.retrieveOriginal().submit().get());
                                    } catch (InterruptedException | ExecutionException exc) {
                                        exc.printStackTrace();
                                    }
                                }))
                        .addSecondaryInteractionEventHandler(
                                ButtonInteractionEvent.class,
                                e -> e.getButton().getId().startsWith(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX),
                                e -> {
                                    switch (e.getButton().getId().split(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX)[1]) {
                                        case "author" -> e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                WELCOMER_EMBED_TITLE + " - Edit Embed Author",
                                                "What about the embed author would you like to edit?"
                                                ).build())
                                                .addActionRow(
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "author:name", "Name", SupportifyEmoji.TITLE.getEmoji()),
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "author:url", "URL", SupportifyEmoji.INTERNET.getEmoji()),
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "author:imageurl", "Image URL", SupportifyEmoji.IMAGE.getEmoji())
                                                )
                                                .queue(success -> {
                                                    try {
                                                        AbstractConfigurator.addOwner(e.getUser(), success.retrieveOriginal().submit().get());
                                                    } catch (InterruptedException | ExecutionException exc) {
                                                        exc.printStackTrace();
                                                    }
                                                });
//                                        case "title" -> ;
//                                        case "thumbnail" -> ;
//                                        case "description" -> ;
//                                        case "image" -> ;
//                                        case "footer" -> ;
//                                        case "timestamp" -> ;
//                                        case "colour" -> ;
                                    }
                                }
                        )
                        .addSecondaryInteractionEventHandler(
                                ButtonInteractionEvent.class,
                                e -> e.getButton().getId().startsWith(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "author:"),
                                e -> {
                                    switch (e.getButton().getId().split(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "author:")[1]) {
                                        case "name" -> {
                                            Modal modal = Modal.create("editembed_author_name", "Edit Author Name")
                                                    .addActionRow(TextInput.create(
                                                            "author_name", "The name of the author", TextInputStyle.SHORT
                                                    ).setRequiredRange(1, 256).setRequired(true).build())
                                                    .build();
                                            e.replyModal(modal).queue();
                                        }
                                        case "url" -> {
                                            Modal modal = Modal.create("editembed_author_url", "Edit Author URL")
                                                    .addActionRow(TextInput.create(
                                                            "author_url", "The URL of the author", TextInputStyle.SHORT
                                                    ).setRequiredRange(1, 999).setRequired(true).build())
                                                    .build();
                                            e.replyModal(modal).queue();
                                        }
                                        case "imageurl" -> {
                                            Modal modal = Modal.create("editembed_author_imgurl", "Edit Author Image URK")
                                                    .addActionRow(TextInput.create(
                                                            "author_imgurl", "The image URL of the author", TextInputStyle.SHORT
                                                    ).setRequiredRange(1, 999).setRequired(true).build())
                                                    .build();
                                            e.replyModal(modal).queue();
                                        }
                                    }
                                }
                        )
                        .addSecondaryInteractionEventHandler(
                                ModalInteractionEvent.class,
                                e -> e.getModalId().startsWith("editembed_author_"),
                                e -> {
                                    final var config = new WelcomerConfig();
                                    final var guild = e.getGuild();
                                    System.out.println(e.getModalId());
                                    switch (e.getModalId().split("editembed_author_")[1]) {
                                        case "name" -> {
                                            final var name = e.getValue("author_name").getAsString();
                                            config.setEmbedAuthorName(guild.getIdLong(), name);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Author Name",
                                                    "You have set the author name to: " + name
                                            )
                                                    .setColor(new Color(153, 37, 255))
                                                    .build()).queue();
                                        }
                                        case "url" -> {
                                            final var url = e.getValue("author_url").getAsString();
                                            config.setEmbedAuthorUrl(guild.getIdLong(), url);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                    "You have set the author URL to: \n" + url
                                            )
                                                    .setColor(new Color(153, 37, 255))
                                                    .build()).queue();
                                        }
                                        case "imgurl" -> {
                                            final var imgUrl = e.getValue("author_imgurl").getAsString();
                                            config.setEmbedAuthorImageUrl(guild.getIdLong(), imgUrl);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                    "You have set the author URL to:"
                                            )
                                                    .setImage(imgUrl)
                                                    .setColor(new Color(153, 37, 255))
                                                    .build()).queue();
                                        }
                                    }
                                }
                        )
                        .build()
                )
                // Preview Embed Button
                .addOption(new ConfiguratorOptionBuilder()
                        .setID(BUTTON_PREFIX + "preview_embed")
                        .setLabel("Preview Embed")
                        .setEmoji(SupportifyEmoji.EYE.getEmoji())
                        .setButtonEventHandler(e -> {
                            final var config = new WelcomerConfig();
                            e.replyEmbeds(config.getWelcomeEmbed(e.getGuild().getIdLong()).build())
                                    .queue();
                        })
                        .build()
                )
        );
    }
}