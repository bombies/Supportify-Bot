package constants;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Emoji;

public enum SupportifyEmoji {
    EYE(Emoji.fromMarkdown("<:white_eye:967942198517698620>")),
    SWITCH(Emoji.fromMarkdown("<:switch:967942979262218300>")),
    CHANNEL(Emoji.fromMarkdown("<:channel:967944685257629756>")),
    PENCIL(Emoji.fromMarkdown("<:white_pencil:967950491860484177>")),
    FEATHER_PEN(Emoji.fromMarkdown("<:quillpen:967960701115183125>")),
    TITLE(Emoji.fromMarkdown("<:title:967962344862924820>")),
    THUMBNAIL(Emoji.fromMarkdown("<:thumbnail:967962467357589565>")),
    DESCRIPTION(Emoji.fromMarkdown("<:description:967962592817606656>")),
    IMAGE(Emoji.fromMarkdown("<:image:967962708207104000>")),
    FOOTER(Emoji.fromMarkdown("<:footer:967962812720771122>")),
    TIMESTAMP(Emoji.fromMarkdown("<:timestamp:967962917528010772>")),
    COLOUR(Emoji.fromMarkdown("<:colour:967963008900927548>")),
    FIELDS(Emoji.fromMarkdown("<:fields:967963477488594994>")),
    INTERNET(Emoji.fromMarkdown("<:web:967968309108678686>"));

    @Getter
    private final Emoji emoji;

    SupportifyEmoji(Emoji emoji) {
        this.emoji = emoji;
    }
}
