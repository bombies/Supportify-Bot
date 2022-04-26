package commands.general.welcomer;

import constants.SupportifyEmoji;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import utils.GeneralUtils;
import utils.SupportifyEmbedUtils;
import utils.component.configurator.AbstractConfigurator;
import utils.component.configurator.ConfiguratorBuilder;
import utils.component.configurator.ConfiguratorOptionBuilder;
import utils.json.welcomer.WelcomerConfig;

import java.awt.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
                        // Main Menu
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
                        // Edit Menu Handler
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
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "author:imageurl", "Image URL", SupportifyEmoji.IMAGE.getEmoji()),
                                                        Button.of(ButtonStyle.DANGER, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "author:remove", "Remove Author", SupportifyEmoji.IMAGE.getEmoji())
                                                )
                                                .queue(success -> {
                                                    try {
                                                        AbstractConfigurator.addOwner(e.getUser(), success.retrieveOriginal().submit().get());
                                                    } catch (InterruptedException | ExecutionException exc) {
                                                        exc.printStackTrace();
                                                    }
                                                });
                                        case "title" -> e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Title",
                                                        "What about the embed title would you like to edit?"
                                                ).build())
                                                .addActionRow(
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "title:set", "Set Title", SupportifyEmoji.TITLE.getEmoji()),
                                                        Button.of(ButtonStyle.DANGER, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "title:remove", "Remove Title", SupportifyEmoji.X.getEmoji())
                                                )
                                                .queue(success -> {
                                                    try {
                                                        AbstractConfigurator.addOwner(e.getUser(), success.retrieveOriginal().submit().get());
                                                    } catch (InterruptedException | ExecutionException exc) {
                                                        exc.printStackTrace();
                                                    }
                                                });
                                        case "thumbnail" -> e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Thumbnail",
                                                        "What about the embed thumbnail would you like to edit?"
                                                ).build())
                                                .addActionRow(
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "thumbnail:set", "Set Thumbnail", SupportifyEmoji.THUMBNAIL.getEmoji()),
                                                        Button.of(ButtonStyle.DANGER, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "thumbnail:remove", "Remove Thumbnail", SupportifyEmoji.X.getEmoji())
                                                )
                                                .queue(success -> {
                                                    try {
                                                        AbstractConfigurator.addOwner(e.getUser(), success.retrieveOriginal().submit().get());
                                                    } catch (InterruptedException | ExecutionException exc) {
                                                        exc.printStackTrace();
                                                    }
                                                });
                                        case "description" -> e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Description",
                                                        "What about the embed description would you like to edit?"
                                                ).build())
                                                .addActionRow(
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "description:set", "Set Description", SupportifyEmoji.DESCRIPTION.getEmoji()),
                                                        Button.of(ButtonStyle.DANGER, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "description:remove", "Remove Description", SupportifyEmoji.X.getEmoji())
                                                )
                                                .queue(success -> {
                                                    try {
                                                        AbstractConfigurator.addOwner(e.getUser(), success.retrieveOriginal().submit().get());
                                                    } catch (InterruptedException | ExecutionException exc) {
                                                        exc.printStackTrace();
                                                    }
                                                });
                                        case "fields" -> e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Fields",
                                                        "What about the embed fields would you like to edit?"
                                                ).build())
                                                .addActionRow(
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "fields:add", "Add Field", SupportifyEmoji.TITLE.getEmoji()),
                                                        Button.of(ButtonStyle.DANGER, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "fields:remove", "Remove Fields", SupportifyEmoji.X.getEmoji()),
                                                        Button.of(ButtonStyle.DANGER, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "fields:removeall", "Remove All Fields", SupportifyEmoji.X.getEmoji())
                                                )
                                                .queue(success -> {
                                                    try {
                                                        AbstractConfigurator.addOwner(e.getUser(), success.retrieveOriginal().submit().get());
                                                    } catch (InterruptedException | ExecutionException exc) {
                                                        exc.printStackTrace();
                                                    }
                                                });
                                        case "image" -> e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Image",
                                                        "What about the embed image would you like to edit?"
                                                ).build())
                                                .addActionRow(
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "image:set", "Set Image", SupportifyEmoji.IMAGE.getEmoji()),
                                                        Button.of(ButtonStyle.DANGER, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "image:remove", "Remove Image", SupportifyEmoji.X.getEmoji())
                                                )
                                                .queue(success -> {
                                                    try {
                                                        AbstractConfigurator.addOwner(e.getUser(), success.retrieveOriginal().submit().get());
                                                    } catch (InterruptedException | ExecutionException exc) {
                                                        exc.printStackTrace();
                                                    }
                                                });
                                        case "footer" -> e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Footer",
                                                        "What about the embed Footer would you like to edit?"
                                                ).build())
                                                .addActionRow(
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "footer:settext", "Set Text", SupportifyEmoji.TITLE.getEmoji()),
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "footer:setimage", "Set Image", SupportifyEmoji.IMAGE.getEmoji()),
                                                        Button.of(ButtonStyle.DANGER, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "footer:remove", "Remove Footer", SupportifyEmoji.X.getEmoji())
                                                )
                                                .queue(success -> {
                                                    try {
                                                        AbstractConfigurator.addOwner(e.getUser(), success.retrieveOriginal().submit().get());
                                                    } catch (InterruptedException | ExecutionException exc) {
                                                        exc.printStackTrace();
                                                    }
                                                });
                                        case "timestamp" -> e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Timestamp",
                                                        "What about the embed timestamp would you like to edit?"
                                                ).build())
                                                .addActionRow(
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "timestamp:toggle", "Toggle Timestamp", SupportifyEmoji.SWITCH.getEmoji())
                                                )
                                                .queue(success -> {
                                                    try {
                                                        AbstractConfigurator.addOwner(e.getUser(), success.retrieveOriginal().submit().get());
                                                    } catch (InterruptedException | ExecutionException exc) {
                                                        exc.printStackTrace();
                                                    }
                                                });
                                        case "colour" -> e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Colour",
                                                        "What about the embed colour would you like to edit?"
                                                ).build())
                                                .addActionRow(
                                                        Button.of(ButtonStyle.SECONDARY, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "colour:set", "Set Colour", SupportifyEmoji.COLOUR.getEmoji()),
                                                        Button.of(ButtonStyle.DANGER, BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "colour:remove", "Remove Colour", SupportifyEmoji.X.getEmoji())
                                                )
                                                .queue(success -> {
                                                    try {
                                                        AbstractConfigurator.addOwner(e.getUser(), success.retrieveOriginal().submit().get());
                                                    } catch (InterruptedException | ExecutionException exc) {
                                                        exc.printStackTrace();
                                                    }
                                                });
                                    }

                                }
                        )
                        // Author Edit Submenu
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
                                        case "remove" -> {
                                            final var config = new WelcomerConfig();
                                            config.setEmbedAuthor(e.getGuild().getIdLong(), null, null, null);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Author",
                                                    "You have removed the author!"
                                            )
                                                    .setColor(new Color(146, 255, 78))
                                                    .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Author Edit Submenu Input Handler
                        .addSecondaryInteractionEventHandler(
                                ModalInteractionEvent.class,
                                e -> e.getModalId().startsWith("editembed_author_"),
                                e -> {
                                    final var config = new WelcomerConfig();
                                    final var guild = e.getGuild();
                                    switch (e.getModalId().split("editembed_author_")[1]) {
                                        case "name" -> {
                                            final var name = e.getValue("author_name").getAsString();
                                            config.setEmbedAuthorName(guild.getIdLong(), name);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Author Name",
                                                    "You have set the author name to: " + name
                                            )
                                                    .setColor(new Color(146, 255, 78))
                                                    .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                        case "url" -> {
                                            final var url = e.getValue("author_url").getAsString();

                                            if (!GeneralUtils.isUrl(url)) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                                        "That is an invalid URL!"
                                                                )
                                                                .setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true)
                                                        .queue();
                                                return;
                                            }

                                            try {
                                                config.setEmbedAuthorUrl(guild.getIdLong(), url);
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                                "You have set the author URL to: \n" + url
                                                        )
                                                        .setColor(new Color(146, 255, 78))
                                                        .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                            } catch (IllegalStateException exc) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                        "You must set a name before attempting to set a URL!"
                                                )
                                                        .setColor(new Color(181, 0, 0))
                                                        .build())
                                                        .setEphemeral(true)
                                                        .queue();
                                            } catch (IllegalArgumentException exc) {
                                                if (exc.getMessage().contains("valid http")) {
                                                    e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                            WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                                            "That is an invalid URL!"
                                                                    )
                                                                    .setColor(new Color(181, 0, 0))
                                                                    .build())
                                                            .setEphemeral(true)
                                                            .queue();
                                                } else {
                                                    e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                            WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                                            "You must set a name before attempting to set a URL!"
                                                                    )
                                                                    .setColor(new Color(181, 0, 0))
                                                                    .build())
                                                            .setEphemeral(true)
                                                            .queue();
                                                }
                                            }
                                        }
                                        case "imgurl" -> {
                                            final var imgUrl = e.getValue("author_imgurl").getAsString();

                                            if (!GeneralUtils.isUrl(imgUrl)) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                                        "That is an invalid URL!"
                                                                )
                                                                .setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true)
                                                        .queue();
                                                return;
                                            }

                                            try {
                                                config.setEmbedAuthorImageUrl(guild.getIdLong(), imgUrl);
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                                "You have set the author URL to:"
                                                        )
                                                        .setImage(imgUrl)
                                                        .setColor(new Color(146, 255, 78))
                                                        .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                            } catch (IllegalStateException exc) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                                        "You must set a name before attempting to set an image URL!"
                                                                )
                                                                .setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true)
                                                        .queue();
                                            } catch (IllegalArgumentException exc) {
                                                if (exc.getMessage().contains("valid http")) {
                                                    e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                            WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                                            "That is an invalid URL!"
                                                                    )
                                                                    .setColor(new Color(181, 0, 0))
                                                                    .build())
                                                            .setEphemeral(true)
                                                            .queue();
                                                } else {
                                                    e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                            WELCOMER_EMBED_TITLE + " - Edit Embed Author URL",
                                                                            "You must set a name before attempting to set a URL!"
                                                                    )
                                                                    .setColor(new Color(181, 0, 0))
                                                                    .build())
                                                            .setEphemeral(true)
                                                            .queue();
                                                }
                                            }
                                        }
                                    }
                                }
                        )
                        // Title Edit Submenu
                        .addSecondaryInteractionEventHandler(
                                ButtonInteractionEvent.class,
                                e -> e.getButton().getId().startsWith(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "title:"),
                                e -> {
                                    switch (e.getButton().getId().split(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "title:")[1]) {
                                        case "set" -> {
                                            Modal modal = Modal.create("editembed_title_set", "Edit Embed Title")
                                                    .addActionRow(TextInput.create("title", "What would you like to set the title to?", TextInputStyle.SHORT)
                                                            .setRequiredRange(1, 256).build()).build();
                                            e.replyModal(modal).queue();
                                        }
                                        case "remove" -> {
                                            final var config = new WelcomerConfig();
                                            config.setEmbedTitle(e.getGuild().getIdLong(), "");
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                            WELCOMER_EMBED_TITLE + " - Edit Embed Title",
                                                            "You have removed the title!"
                                                    )
                                                    .setColor(new Color(146, 255, 78))
                                                    .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Title Edit Submenu Input Handler
                        .addSecondaryInteractionEventHandler(
                                ModalInteractionEvent.class,
                                e -> e.getModalId().startsWith("editembed_title_"),
                                e -> {
                                    switch (e.getModalId().split("editembed_title_")[1]) {
                                        case "set" -> {
                                            final var title = e.getValue("title").getAsString();
                                            final var config = new WelcomerConfig();

                                            config.setEmbedTitle(e.getGuild().getIdLong(), title);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Title",
                                                    "You have set the embed title to: " + title
                                            )
                                                    .setColor(new Color(146, 255, 78))
                                                    .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Thumbnail Edit Submenu
                        .addSecondaryInteractionEventHandler(
                                ButtonInteractionEvent.class,
                                e -> e.getButton().getId().startsWith(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "thumbnail:"),
                                e -> {
                                    switch (e.getButton().getId().split(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "thumbnail:")[1]) {
                                        case "set" -> {
                                            Modal modal = Modal.create("editembed_thumbnail_set", "Edit Embed Thumbnail")
                                                    .addActionRow(TextInput.create("thumbnail", "What would you like to set the thumbnail to?", TextInputStyle.SHORT)
                                                            .setRequiredRange(1, 256).build()).build();
                                            e.replyModal(modal).queue();
                                        }
                                        case "remove" -> {
                                            final var config = new WelcomerConfig();
                                            config.setEmbedThumbnail(e.getGuild().getIdLong(), null);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                            WELCOMER_EMBED_TITLE + " - Edit Embed Thumbnail",
                                                            "You have removed the thumbnail!"
                                                    )
                                                    .setColor(new Color(146, 255, 78))
                                                    .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Thumbnail Edit Submenu Input Handler
                        .addSecondaryInteractionEventHandler(
                                ModalInteractionEvent.class,
                                e -> e.getModalId().startsWith("editembed_thumbnail_"),
                                e -> {
                                    switch (e.getModalId().split("editembed_thumbnail_")[1]) {
                                        case "set" -> {
                                            final var config = new WelcomerConfig();
                                            final var thumbnail = e.getValue("thumbnail").getAsString();

                                            try {
                                                config.setEmbedThumbnail(e.getGuild().getIdLong(), thumbnail);
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Thumbnail",
                                                                        "You have set the thumbnail to:"
                                                                )
                                                                .setImage(thumbnail)
                                                                .build())
                                                        .queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                            }  catch (IllegalArgumentException exc) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Image",
                                                                        "That is an invalid URL!"
                                                                )
                                                                .setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true)
                                                        .queue();
                                            }
                                        }
                                    }
                                }
                        )
                        // Description Edit Submenu
                        .addSecondaryInteractionEventHandler(
                                ButtonInteractionEvent.class,
                                e -> e.getButton().getId().startsWith(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "description:"),
                                e -> {
                                    switch (e.getButton().getId().split(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "description:")[1]) {
                                        case "set" -> {
                                            Modal modal = Modal.create("editembed_description_set", "Edit Embed Description")
                                                    .addActionRow(TextInput.create("description", "What should the description be?", TextInputStyle.PARAGRAPH)
                                                            .setRequiredRange(1, 4000).build()).build();
                                            e.replyModal(modal).queue();
                                        }
                                        case "remove" -> {
                                            final var config = new WelcomerConfig();
                                            config.setEmbedDescription(e.getGuild().getIdLong(), "");
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                            WELCOMER_EMBED_TITLE + " - Edit Embed Description",
                                                            "You have removed the description!"
                                                    )
                                                    .setColor(new Color(146, 255, 78))
                                                    .build()).queue(msg -> msg.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Description Edit Submenu Input Handler
                        .addSecondaryInteractionEventHandler(
                                ModalInteractionEvent.class,
                                e -> e.getModalId().startsWith("editembed_description_"),
                                e -> {
                                    switch (e.getModalId().split("editembed_description_")[1]) {
                                        case "set" -> {
                                            final var config = new WelcomerConfig();
                                            final var description = e.getValue("description").getAsString();

                                            config.setEmbedDescription(e.getGuild().getIdLong(), description);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Description",
                                                                    "You have set the description to:\n\n" + description
                                                            )
                                                            .build())
                                                    .queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Fields Edit Submenu
                        .addSecondaryInteractionEventHandler(
                                ButtonInteractionEvent.class,
                                e -> e.getButton().getId().startsWith(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "fields:"),
                                e -> {
                                    switch (e.getButton().getId().split(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "fields:")[1]) {
                                        case "add" -> {
                                            final var config = new WelcomerConfig();
                                            EmbedBuilder welcomeEmbed = config.getWelcomeEmbed(e.getGuild().getIdLong());
                                            if (!welcomeEmbed.isEmpty())
                                                if (welcomeEmbed.getFields().size() == 25) {
                                                    e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                            WELCOMER_EMBED_TITLE + " Edit Embed Fields",
                                                            "You can't add anymore fields!"
                                                    ).setColor(new Color(181, 0, 0))
                                                                    .build())
                                                            .setEphemeral(true)
                                                            .queue();
                                                    return;
                                                }

                                            Modal modal = Modal.create("editembed_fields_add", "Add Embed Field")
                                                    .addActionRow(TextInput.create("label", "What should the field's label be?", TextInputStyle.SHORT).setRequiredRange(1, 256).build())
                                                    .addActionRow(TextInput.create("value", "What should the field's description be?", TextInputStyle.PARAGRAPH).setRequiredRange(1, 1024).build())
                                                    .addActionRow(TextInput.create("inline", "Should the field be inline? (Yes/No)", TextInputStyle.SHORT).setRequiredRange(2, 3).build())
                                                    .build();
                                            e.replyModal(modal).queue();
                                        }
                                        case "remove" -> {
                                            final var config = new WelcomerConfig();
                                            EmbedBuilder welcomeEmbed = config.getWelcomeEmbed(e.getGuild().getIdLong());
                                            if (welcomeEmbed.isEmpty()) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                WELCOMER_EMBED_TITLE + " Edit Embed Fields",
                                                                "There are no fields to remove!"
                                                        ).setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true).queue();
                                                return;
                                            }

                                            if (welcomeEmbed.getFields().size() == 0) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                WELCOMER_EMBED_TITLE + " Edit Embed Fields",
                                                                "There are no fields to remove!"
                                                        ).setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true).queue();
                                                return;
                                            }

                                            Modal modal = Modal.create("editembed_fields_remove", "Remove Embed Field")
                                                    .addActionRow(
                                                            TextInput.create("id", "ID of the field you would like to remove", TextInputStyle.SHORT).setRequiredRange(1, 2).build()
                                                    ).build();
                                            e.replyModal(modal).queue();
                                        }
                                        case "removeall" -> {
                                            final var config = new WelcomerConfig();
                                            EmbedBuilder welcomeEmbed = config.getWelcomeEmbed(e.getGuild().getIdLong());
                                            if (welcomeEmbed.isEmpty()) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                WELCOMER_EMBED_TITLE + " Edit Embed Fields",
                                                                "There are no fields to remove!"
                                                        ).setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true).queue();
                                                return;
                                            }

                                            if (welcomeEmbed.getFields().size() == 0) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                WELCOMER_EMBED_TITLE + " Edit Embed Fields",
                                                                "There are no fields to remove!"
                                                        ).setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true).queue();
                                                return;
                                            }

                                            config.removeEmbedAllFields(e.getGuild().getIdLong());
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Fields",
                                                    "You have successfully removed all fields!"
                                            )
                                                    .setColor(new Color(146, 255, 78))
                                                    .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Fields Edit Submenu Input Handler
                        .addSecondaryInteractionEventHandler(
                                ModalInteractionEvent.class,
                                e -> e.getModalId().startsWith("editembed_fields_"),
                                e -> {
                                    switch (e.getModalId().split("editembed_fields_")[1]) {
                                        case "add" -> {
                                            final var config = new WelcomerConfig();
                                            final var label = e.getValue("label").getAsString();
                                            final var value = e.getValue("value").getAsString();
                                            final var inline = e.getValue("inline").getAsString();

                                            if (!inline.equalsIgnoreCase("yes") && !inline.equalsIgnoreCase("no")) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Fields",
                                                        "The inline response is invalid!\n" +
                                                                "You must provide either **Yes** or **No**"
                                                ).setColor(new Color(181, 0, 0))
                                                        .build()).setEphemeral(true).queue();
                                                return;
                                            }

                                            config.addEmbedField(e.getGuild().getIdLong(), label, value, inline.equalsIgnoreCase("yes"));
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Description",
                                                                    "You have added a new field:\n\n"
                                                            )
                                                            .addField(label, value, inline.equalsIgnoreCase("yes"))
                                                            .build())
                                                    .queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                        case "remove" -> {
                                            final var config = new WelcomerConfig();
                                            final var idStr = e.getValue("id").getAsString();

                                            if (!GeneralUtils.stringIsInt(idStr)) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Fields",
                                                        "The ID provided must be a valid integer!"
                                                ).setColor(new Color(181, 0, 0))
                                                        .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                                return;
                                            }

                                            final var id = Integer.parseInt(idStr);
                                            final var embed = config.getWelcomeEmbed(e.getGuild().getIdLong());

                                            if (id < 0 || id >= embed.getFields().size()) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                WELCOMER_EMBED_TITLE + " - Edit Embed Fields",
                                                                "The ID isn't a valid ID!\n" +
                                                                        "You must provide and ID between **0** and **" + (embed.getFields().size() - 1) + "**."
                                                        ).setColor(new Color(181, 0, 0))
                                                        .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                                return;
                                            }

                                            final var removedField = embed.getFields().get(id);
                                            config.removeEmbedField(e.getGuild().getIdLong(), id);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Description",
                                                                    "You have removed the field:\n\n"
                                                            )
                                                            .addField(removedField)
                                                            .build())
                                                    .queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Image Edit Submenu
                        .addSecondaryInteractionEventHandler(
                                ButtonInteractionEvent.class,
                                e -> e.getButton().getId().startsWith(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "image:"),
                                e -> {
                                    switch (e.getButton().getId().split(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "image:")[1]) {
                                        case "set" -> {
                                            Modal modal = Modal.create("editembed_image_set", "Edit Embed Image")
                                                    .addActionRow(TextInput.create("image", "What should the image be?", TextInputStyle.SHORT)
                                                            .setRequiredRange(1, 256).build()).build();
                                            e.replyModal(modal).queue();
                                        }
                                        case "remove" -> {
                                            final var config = new WelcomerConfig();
                                            config.setEmbedImage(e.getGuild().getIdLong(), null);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                            WELCOMER_EMBED_TITLE + " - Edit Embed Image",
                                                            "You have removed the image!"
                                                    )
                                                    .setColor(new Color(146, 255, 78))
                                                    .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Image Edit Submenu Input Handler
                        .addSecondaryInteractionEventHandler(
                                ModalInteractionEvent.class,
                                e -> e.getModalId().startsWith("editembed_image_"),
                                e -> {
                                    switch (e.getModalId().split("editembed_image_")[1]) {
                                        case "set" -> {
                                            final var config = new WelcomerConfig();
                                            final var image = e.getValue("image").getAsString();

                                            try {
                                                config.setEmbedImage(e.getGuild().getIdLong(), image);
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Image",
                                                                        "You have set the image to:"
                                                                )
                                                                .setImage(image)
                                                                .build())
                                                        .queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                            }  catch (IllegalArgumentException exc) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Image",
                                                                        "That is an invalid URL!"
                                                                )
                                                                .setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true)
                                                        .queue();
                                            }
                                        }
                                    }
                                }
                        )
                        // Footer Edit Submenu
                        .addSecondaryInteractionEventHandler(
                                ButtonInteractionEvent.class,
                                e -> e.getButton().getId().startsWith(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "footer:"),
                                e -> {
                                    switch (e.getButton().getId().split(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "footer:")[1]) {
                                        case "setimage" -> {
                                            Modal modal = Modal.create("editembed_footer_setimage", "Edit Embed Footer Image")
                                                    .addActionRow(TextInput.create("image", "What should the footer image be?", TextInputStyle.SHORT)
                                                            .setRequiredRange(1, 256).build()).build();
                                            e.replyModal(modal).queue();
                                        }
                                        case "settext" -> {
                                            Modal modal = Modal.create("editembed_footer_settext", "Edit Embed Footer Text")
                                                    .addActionRow(TextInput.create("text", "What should the footer text be?", TextInputStyle.SHORT)
                                                            .setRequiredRange(1, 2048).build()).build();
                                            e.replyModal(modal).queue();
                                        }
                                        case "remove" -> {
                                            final var config = new WelcomerConfig();
                                            config.setEmbedFooterText(e.getGuild().getIdLong(), null);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                            WELCOMER_EMBED_TITLE + " - Edit Embed Footer",
                                                            "You have removed the footer!"
                                                    )
                                                    .setColor(new Color(146, 255, 78))
                                                    .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Footer Edit Submenu Input Handler
                        .addSecondaryInteractionEventHandler(
                                ModalInteractionEvent.class,
                                e -> e.getModalId().startsWith("editembed_footer_"),
                                e -> {
                                    switch (e.getModalId().split("editembed_footer_")[1]) {
                                        case "setimage" -> {
                                            final var config = new WelcomerConfig();
                                            final var image = e.getValue("image").getAsString();
                                            final var embed = config.getWelcomeEmbed(e.getGuild().getIdLong());

                                            if (embed.isEmpty()) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Footer Image",
                                                                        "You must set the footer text before setting the image!"
                                                                )
                                                                .setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true)
                                                        .queue();
                                                return;
                                            }

                                            final var builtEmbed = embed.build();
                                            if (builtEmbed.getFooter() == null) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Footer Image",
                                                                        "You must set the footer text before setting the image!"
                                                                )
                                                                .setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true)
                                                        .queue();
                                                return;
                                            }

                                            try {
                                                config.setEmbedFooter(e.getGuild().getIdLong(), builtEmbed.getFooter().getText(),  image);
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Footer Image",
                                                                        "You have set the image to:"
                                                                )
                                                                .setColor(new Color(146, 255, 78))
                                                                .setImage(image)
                                                                .build())
                                                        .queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                            }  catch (IllegalArgumentException exc) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                        WELCOMER_EMBED_TITLE + " - Edit Embed Footer Image",
                                                                        "That is an invalid URL!"
                                                                )
                                                                .setColor(new Color(181, 0, 0))
                                                                .build())
                                                        .setEphemeral(true)
                                                        .queue();
                                            }
                                        }
                                        case "settext" -> {
                                            final var config = new WelcomerConfig();
                                            final var text = e.getValue("text").getAsString();

                                            config.setEmbedFooterText(e.getGuild().getIdLong(), text);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Footer Text",
                                                                    "You have set the text to:\n\n" + text
                                                            )
                                                            .setColor(new Color(146, 255, 78))
                                                            .build())
                                                    .queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Timestamp Edit Submenu
                        .addSecondaryInteractionEventHandler(
                                ButtonInteractionEvent.class,
                                e -> e.getButton().getId().startsWith(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "timestamp:"),
                                e -> {
                                    switch (e.getButton().getId().split(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "timestamp:")[1]) {
                                        case "toggle" -> {
                                            final var config = new WelcomerConfig();
                                            final var embed = config.getWelcomeEmbed(e.getGuild().getIdLong());

                                            if (embed.isEmpty()) {
                                                config.showTimestamp(e.getGuild().getIdLong(), true);
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                        WELCOMER_EMBED_TITLE + " - Edit Timestamp",
                                                        "You have toggled timestamps **ON**"
                                                )
                                                        .setColor(new Color(146, 255, 78))
                                                        .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                                return;
                                            }

                                            if (embed.build().getTimestamp() != null) {
                                                config.showTimestamp(e.getGuild().getIdLong(), false);
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                WELCOMER_EMBED_TITLE + " - Edit Timestamp",
                                                                "You have toggled timestamps **OFF**"
                                                        )
                                                        .setColor(new Color(176, 0, 0))
                                                        .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                            } else {
                                                config.showTimestamp(e.getGuild().getIdLong(), true);
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                WELCOMER_EMBED_TITLE + " - Edit Timestamp",
                                                                "You have toggled timestamps **ON**"
                                                        )
                                                        .setColor(new Color(146, 255, 78))
                                                        .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                            }
                                        }
                                    }
                                }
                        )
                        // Colour Edit Submenu
                        .addSecondaryInteractionEventHandler(
                                ButtonInteractionEvent.class,
                                e -> e.getButton().getId().startsWith(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "colour:"),
                                e -> {
                                    switch (e.getButton().getId().split(BUTTON_PREFIX + BUTTON_EDIT_EMBED_PREFIX + "colour:")[1]) {
                                        case "set" -> {
                                            Modal modal = Modal.create("editembed_colour_set", "Edit Embed Colour")
                                                    .addActionRow(TextInput.create("colour", "What should the colour be? (Eg. #FF123G)", TextInputStyle.SHORT)
                                                            .setRequiredRange(7, 7).build()).build();
                                            e.replyModal(modal).queue();
                                        }
                                        case "remove" -> {
                                            final var config = new WelcomerConfig();
                                            config.setEmbedColor(e.getGuild().getIdLong(), null);
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                            WELCOMER_EMBED_TITLE + " - Edit Embed Colour",
                                                            "You have removed the colour!"
                                                    )
                                                    .setColor(new Color(146, 255, 78))
                                                    .build()).queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                                        }
                                    }
                                }
                        )
                        // Colour Edit Submenu Input Handler
                        .addSecondaryInteractionEventHandler(
                                ModalInteractionEvent.class,
                                e -> e.getModalId().startsWith("editembed_colour_"),
                                e -> {
                                    switch (e.getModalId().split("editembed_colour_")[1]) {
                                        case "set" -> {
                                            final var config = new WelcomerConfig();
                                            final var colour = e.getValue("colour").getAsString();

                                            if (!Pattern.matches("^#[a-fA-F\\d]{6}$", colour)) {
                                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                WELCOMER_EMBED_TITLE + " - Edit Embed Colour",
                                                                "Invalid HEX code!"
                                                        )
                                                        .setColor(new Color(176, 0, 0))
                                                        .build())
                                                        .setEphemeral(true).queue();
                                                return;
                                            }

                                            config.setEmbedColor(e.getGuild().getIdLong(), GeneralUtils.parseColor(colour));
                                            e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(
                                                                    WELCOMER_EMBED_TITLE + " - Edit Embed Colour",
                                                                    "You have set the colour to:\n\n" + colour
                                                            )
                                                            .setColor(GeneralUtils.parseColor(colour))
                                                            .build())
                                                    .queue(msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
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

                            try {
                                e.deferReply().queue();
                                e.getHook().sendMessageEmbeds(config.getWelcomeEmbed(e.getGuild().getIdLong()).build())
                                        .setEphemeral(true)
                                        .queue(null, new ErrorHandler().handle(ErrorResponse.INVALID_FORM_BODY, exc -> {
                                            e.getHook().sendMessageEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(WELCOMER_EMBED_TITLE + " - Preview Embed", "*The embed is empty*").build())
                                                    .setEphemeral(true)
                                                    .queue();
                                        }));
                            } catch (IllegalStateException exc) {
                                e.replyEmbeds(SupportifyEmbedUtils.embedMessageWithTitle(WELCOMER_EMBED_TITLE + " - Preview Embed", "*The embed is empty*").build())
                                        .setEphemeral(true)
                                        .queue();
                            }
                        })
                        .build()
                )
        );
    }
}
