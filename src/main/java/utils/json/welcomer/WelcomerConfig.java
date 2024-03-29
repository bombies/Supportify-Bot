package utils.json.welcomer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.database.mongodb.databases.GuildDB;
import utils.json.AbstractGuildConfig;

import java.awt.*;
import java.time.Instant;
import java.util.Objects;

public class WelcomerConfig extends AbstractGuildConfig {

    public void setChannel(long gid, long cid) {
        final var obj = getObject(gid);
        obj.put(Fields.CHANNEL_ID.toString(), cid);
        setObject(gid, obj);
    }

    public void setEnabled(long gid, boolean val) {
        final var obj = getObject(gid);
        obj.put(Fields.IS_ENABLED.toString(), val);
        setObject(gid, obj);
    }

    public void setEmbedInfo(long gid, MessageEmbed embed) {
        final var obj = getObject(gid);
        final var embedObject = obj.getJSONObject(Fields.EMBED_INFO.toString());

        embedObject.put(Fields.EmbedInfo.TITLE.toString(), embed.getTitle() != null ? embed.getTitle().isBlank() || embed.getTitle().isEmpty() ? null : embed.getTitle() : "");


        embedObject.put(Fields.EmbedInfo.AUTHOR.toString(), new JSONObject()
                .put(Fields.EmbedInfo.Author.NAME.toString(), embed.getAuthor() != null ? embed.getAuthor().getName() : "")
                .put(Fields.EmbedInfo.Author.IMAGE.toString(), (embed.getAuthor() != null) ? (embed.getAuthor().getIconUrl() != null ? embed.getAuthor().getIconUrl() : "") : "")
                .put(Fields.EmbedInfo.Author.URL.toString(), (embed.getAuthor() != null) ? (embed.getAuthor().getUrl() != null ? embed.getAuthor().getUrl() : "")  : "")
        );

        embedObject.put(Fields.EmbedInfo.THUMBNAIL.toString(), embed.getThumbnail() != null ? embed.getThumbnail().getUrl() : "");
        embedObject.put(Fields.EmbedInfo.IMAGE.toString(), embed.getImage() != null ? embed.getImage().getUrl() : "");
        embedObject.put(Fields.EmbedInfo.COLOUR.toString(), embed.getColorRaw());
        embedObject.put(Fields.EmbedInfo.DESCRIPTION.toString(), embed.getDescription() != null ? embed.getDescription() : "");

        final var fieldArr = new JSONArray();
        embed.getFields().forEach(field -> fieldArr.put(new JSONObject()
                .put(Fields.EmbedInfo.Field.LABEL.toString(), field.getName())
                .put(Fields.EmbedInfo.Field.VALUE.toString(), field.getValue() != null ? field.getValue() : "")
                .put(Fields.EmbedInfo.Field.INLINE.toString(), field.isInline())
        ));
        embedObject.put(Fields.EmbedInfo.FIELDS.toString(), fieldArr);

        if (embed.getFooter() != null)
            embedObject.put(Fields.EmbedInfo.FOOTER.toString(), new JSONObject()
                    .put(Fields.EmbedInfo.Footer.TEXT.toString(), embed.getFooter().getText() != null ? embed.getFooter().getText() : "")
                    .put(Fields.EmbedInfo.Footer.IMAGE.toString(), embed.getFooter().getIconUrl() != null ? embed.getFooter().getIconUrl() : "")
            );
        else
            embedObject.put(Fields.EmbedInfo.FOOTER.toString(), new JSONObject()
                    .put(Fields.EmbedInfo.Footer.TEXT.toString(), "")
                    .put(Fields.EmbedInfo.Footer.IMAGE.toString(), "")
            );

        embedObject.put(Fields.EmbedInfo.SHOW_TIMESTAMP.toString(), embed.getTimestamp() != null ? "true" : false);

        setObject(gid, obj);
    }

    public Welcomer getWelcomer(long gid) {
        final var obj = getObject(gid);

        return new Welcomer(
                obj.getBoolean(Fields.IS_ENABLED.toString()),
                obj.getLong(Fields.CHANNEL_ID.toString()),
                gid,
                getWelcomeEmbed(gid).build()
        );
    }

    public void setEmbedTitle(long gid, String title) {
        if (title.length() > 256)
            throw new IllegalArgumentException("The title can be no more than 256 characters!");

        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.setTitle(title.isBlank() || title.isEmpty() ? null : title);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void setEmbedDescription(long gid, String description) {
        if (description.length() > 4096)
            throw new IllegalArgumentException("The description can be no more than 4096 characters!");

        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.setDescription(description);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void setEmbedAuthor(long gid, String name, String url, String imageURL) {
        if (name != null)
            if (name.length() > 256)
                throw new IllegalArgumentException("The author name can be no more than 256 characters!");

        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.setAuthor(name, url, imageURL);

        if (welcomeEmbed.isEmpty())
            welcomeEmbed.setColor(Role.DEFAULT_COLOR_RAW + 1);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void setEmbedAuthor(long gid, String name, String url) {
        if (name.length() > 256)
            throw new IllegalArgumentException("The author name can be no more than 256 characters!");

        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.setAuthor(name, url);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void setEmbedAuthorName(long gid, String name) {
        if (name.length() > 256)
            throw new IllegalArgumentException("The author name can be no more than 256 characters!");

        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        if (!welcomeEmbed.isEmpty()) {
            MessageEmbed embedBuilt = welcomeEmbed.build();
            welcomeEmbed.setAuthor(name, embedBuilt.getAuthor() != null ? embedBuilt.getAuthor().getUrl() : null, embedBuilt.getAuthor() != null ? embedBuilt.getAuthor().getIconUrl() : null);
        } else welcomeEmbed.setAuthor(name);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void setEmbedAuthorUrl(long gid, String url) {
        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        if (welcomeEmbed.isEmpty())
            throw new IllegalArgumentException("The embed can't be empty when attempting to set the URL of the author");

        MessageEmbed embedBuilt = welcomeEmbed.build();

        if (embedBuilt.getAuthor().getName() == null)
            throw new IllegalStateException("The name of the author must be set before setting a URL!");

        welcomeEmbed.setAuthor(embedBuilt.getAuthor().getName(), url, embedBuilt.getAuthor().getIconUrl());
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void setEmbedAuthorImageUrl(long gid, String url) {
        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        if (welcomeEmbed.isEmpty())
            throw new IllegalArgumentException("The embed can't be empty when attempting to set the image URL of the author");

        MessageEmbed embedBuilt = welcomeEmbed.build();

        if (embedBuilt.getAuthor() == null)
            throw new IllegalStateException("The name of the author must be set before setting a URL!");
        if (embedBuilt.getAuthor().getName() == null)
            throw new IllegalStateException("The name of the author must be set before setting an image URL!");

        welcomeEmbed.setAuthor(embedBuilt.getAuthor().getName(), embedBuilt.getAuthor().getUrl(), url);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void setEmbedThumbnail(long gid, String thumbnailURL) {
        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.setThumbnail(thumbnailURL);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void setEmbedImage(long gid, String imageURL) {
        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.setImage(imageURL);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void setEmbedColor(long gid, Color color) {
        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.setColor(color);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void addEmbedField(long gid, String label, String value, boolean inline) {
        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.addField(label, value, inline);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void removeEmbedField(long gid, int fieldIndex) {
        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.getFields().remove(fieldIndex);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void removeEmbedAllFields(long gid) {
        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.getFields().clear();
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void setEmbedFooter(long gid, String text, String imageURL) {
        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.setFooter(text, imageURL);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void setEmbedFooterText(long gid, String text) {
        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        welcomeEmbed.setFooter(text);
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public void showTimestamp(long gid, boolean status) {
        EmbedBuilder welcomeEmbed = getWelcomeEmbed(gid);
        if (!status)
            welcomeEmbed.setTimestamp(null);
        else
            welcomeEmbed.setTimestamp(Instant.now());
        setEmbedInfo(gid, welcomeEmbed.build());
    }

    public EmbedBuilder getWelcomeEmbed(long gid) {
        final var obj = getObject(gid);
        final var embedInfo = obj.getJSONObject(Fields.EMBED_INFO.toString());

        EmbedBuilder embedBuilder = new EmbedBuilder();

        final var title = embedInfo.getString(Fields.EmbedInfo.TITLE.toString());
        if (title != null)
            if (!title.isEmpty() && !title.isBlank())
                embedBuilder.setTitle(title);

        final var authorInfo = embedInfo.getJSONObject(Fields.EmbedInfo.AUTHOR.toString());
        final var authorName = authorInfo.getString(Fields.EmbedInfo.Author.NAME.toString());
        if (authorName != null) {
            if (!authorName.isBlank() && !authorName.isEmpty()) {
                final var authorURL = authorInfo.getString(Fields.EmbedInfo.Author.URL.toString());
                final var authorImageURL = authorInfo.getString(Fields.EmbedInfo.Author.IMAGE.toString());
                embedBuilder.setAuthor(
                        authorName,
                        (authorURL != null) ? (!authorURL.isBlank() && !authorURL.isEmpty() ? authorURL : null) : null,
                        (authorImageURL != null) ? (!authorImageURL.isBlank() && !authorImageURL.isEmpty() ? authorImageURL : null) : null
                );
            }
        }

        final var thumbnail = embedInfo.getString(Fields.EmbedInfo.THUMBNAIL.toString());
        if (thumbnail != null)
            if (!thumbnail.isEmpty() && !thumbnail.isBlank())
                embedBuilder.setThumbnail(thumbnail);

        final var image = embedInfo.getString(Fields.EmbedInfo.IMAGE.toString());
        if (image != null)
            if (!image.isEmpty() && !image.isBlank())
                embedBuilder.setImage(image);

        final var color = embedInfo.getInt(Fields.EmbedInfo.COLOUR.toString());
        if (color != 0)
            embedBuilder.setColor(color);

        final var description = embedInfo.getString(Fields.EmbedInfo.DESCRIPTION.toString());
        embedBuilder.setDescription(Objects.requireNonNullElse(description, ""));

        final var fieldsInfo = embedInfo.getJSONArray(Fields.EmbedInfo.FIELDS.toString());
        for (final var field : fieldsInfo) {
            final var fieldObj = (JSONObject) field;
            embedBuilder.addField(
                    fieldObj.getString(Fields.EmbedInfo.Field.LABEL.toString()),
                    fieldObj.getString(Fields.EmbedInfo.Field.VALUE.toString()),
                    fieldObj.getBoolean(Fields.EmbedInfo.Field.INLINE.toString())
            );
        }

        final var footerInfo = embedInfo.getJSONObject(Fields.EmbedInfo.FOOTER.toString());
        final var footerText = footerInfo.getString(Fields.EmbedInfo.Footer.TEXT.toString());
        if (footerText != null) {
            if (!footerText.isEmpty() && !footerText.isBlank()) {
                final var footerImage = footerInfo.getString(Fields.EmbedInfo.Footer.IMAGE.toString());
                embedBuilder.setFooter(
                        footerText,
                        (footerImage != null) ? (!footerImage.isBlank() && !footerImage.isEmpty() ? footerImage : null) : null
                );
            }
        }

        if (embedInfo.getBoolean(Fields.EmbedInfo.SHOW_TIMESTAMP.toString()))
            embedBuilder.setTimestamp(Instant.now());

        return embedBuilder;
    }

    public boolean isEnabled(long gid) {
        return getObject(gid).getBoolean(Fields.IS_ENABLED.toString());
    }

    public boolean channelIsSet(long gid) {
        return getObject(gid).getLong(Fields.CHANNEL_ID.toString()) != -1L;
    }

    private boolean hasObject(long gid) {
        return getGuildObject(gid).has(Fields.WELCOMER.toString());
    }

    private JSONObject getObject(long gid) {
        if (!hasObject(gid))
            update(gid);
        return getGuildObject(gid).getJSONObject(Fields.WELCOMER.toString());
    }

    private void setObject(long gid, JSONObject object) {
        getCache().setField(gid, Fields.WELCOMER, object);
    }

    @Override
    public void update(long gid) {
        if (!guildHasInfo(gid))
            loadGuild(gid);

        final var guildObj = getGuildObject(gid);
        if (guildObj.has(Fields.WELCOMER.toString()))
            return;
        getCache().setField(gid, Fields.WELCOMER, getDefaultObject());
    }

    private JSONObject getDefaultObject() {
        final JSONObject ret = new JSONObject();

        ret.put(Fields.IS_ENABLED.toString(), false);
        ret.put(Fields.CHANNEL_ID.toString(), -1L);
        ret.put(Fields.EMBED_INFO.toString(), new JSONObject()
                .put(Fields.EmbedInfo.TITLE.toString(), "")
                .put(Fields.EmbedInfo.THUMBNAIL.toString(), "")
                .put(Fields.EmbedInfo.AUTHOR.toString(), new JSONObject()
                        .put(Fields.EmbedInfo.Author.NAME.toString(), "")
                        .put(Fields.EmbedInfo.Author.IMAGE.toString(), "")
                        .put(Fields.EmbedInfo.Author.URL.toString(), "")
                )
                .put(Fields.EmbedInfo.COLOUR.toString(), 0)
                .put(Fields.EmbedInfo.DESCRIPTION.toString(), "")
                .put(Fields.EmbedInfo.FIELDS.toString(), new JSONArray())
                .put(Fields.EmbedInfo.IMAGE.toString(), "")
                .put(Fields.EmbedInfo.FOOTER.toString(), new JSONObject()
                        .put(Fields.EmbedInfo.Footer.TEXT.toString(), "")
                        .put(Fields.EmbedInfo.Footer.IMAGE.toString(), "")

                )
                .put(Fields.EmbedInfo.SHOW_TIMESTAMP.toString(), false)
        );

        return ret;
    }

    public enum Fields implements GuildDB.GuildField {
        WELCOMER("welcomer"),

        IS_ENABLED("is_enabled"),
        CHANNEL_ID("channel_id"),
        EMBED_INFO("embed_info");

        public enum EmbedInfo implements GuildDB.GuildField {
            TITLE("embed_title"),
            AUTHOR("embed_author"),
            THUMBNAIL("embed_thumbnail"),
            COLOUR("embed_colour"),
            DESCRIPTION("embed_description"),
            FIELDS("embed_fields"),
            IMAGE("embed_image"),
            FOOTER("embed_footer"),
            SHOW_TIMESTAMP("show_timestamp");

            public enum Author implements GuildDB.GuildField {
                NAME("embed_author_name"),
                IMAGE("embed_author_image"),
                URL("embed_author_uri");

                private final String str;

                Author(String str) {
                    this.str = str;
                }

                @Override
                public String toString() {
                    return str;
                }
            }

            public enum Field implements GuildDB.GuildField {
                LABEL("embed_field_label"),
                VALUE("embed_field_value"),
                INLINE("embed_field_inline");

                private final String str;

                Field(String str) {
                    this.str = str;
                }

                @Override
                public String toString() {
                    return str;
                }
            }

            public enum Footer implements GuildDB.GuildField {
                TEXT("embed_footer_text"),
                IMAGE("embed_footer_image");

                private final String str;

                Footer(String str) {
                    this.str = str;
                }

                @Override
                public String toString() {
                    return str;
                }
            }

            private final String str;

            EmbedInfo(String str) {
                this.str = str;
            }

            @Override
            public String toString() {
                return str;
            }
        }

        private final String str;

        Fields(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }
}
