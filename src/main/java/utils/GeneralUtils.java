package utils;

import constants.BotConstants;
import constants.ENV;
import constants.TimeFormat;
import lombok.SneakyThrows;
import main.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class GeneralUtils {
    private static final Logger logger = LoggerFactory.getLogger(GeneralUtils.class);

    public final static Color EMBED_COLOR = parseColor(Config.get(ENV.BOT_COLOR));

    public static boolean stringIsNum(String s) {
        if (s == null) return false;
        else {
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    public static boolean stringIsInt(String s) {
        if (s == null) return false;
        else {
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    public static <E> String listToString(java.util.List<E> list) {
        final var sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            var elem = list.get(i);
            sb.append(elem instanceof net.dv8tion.jda.api.Permission ?
                            ((net.dv8tion.jda.api.Permission) elem).getName()
                            : elem.toString())
                    .append(i != list.size() - 1 ? ", " : "");
        }
        return sb.toString();
    }

    public static <E> String arrayToString(E[] arr) {
        return listToString(Arrays.stream(arr).toList());
    }

    public static int longToInt(long l) {
        if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE)
            throw new IllegalArgumentException("This long exceeds the integer limits!");

        return Integer.parseInt(String.valueOf(l));
    }

    public static boolean stringIsID(String s) {
        String idRegex = "^[0-9]{18}$";
        return Pattern.matches(idRegex, getDigitsOnly(s));
    }

    public static boolean isUrl(String url) {
        if (url == null)
            return false;

        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static String getDigitsOnly(String s) {
        return s.replaceAll("\\D", "");
    }

    public static String removeAllDigits(String s) {
        return s.replaceAll("\\d", "");
    }

    public static void updateENVField(ENV field, String str) throws IOException {
        switch (field) {
            case BOT_TOKEN -> throw new IllegalAccessError("This env value can't be changed from the bot!");
            default -> {}
        }
        String fileContent = getFileContent(".env");
        String envFieldTitle = field.name();
        String envFieldValue = Config.get(field);
        setFileContent(
                ".env",
                fileContent.replace(envFieldTitle+"="+envFieldValue, envFieldTitle+"="+str)
        );
        Config.reload();
    }

    public static String progressBar(Guild guild, TextChannel channel, double percent, ProgressBar barType) {
        final var self = guild.getSelfMember();
        switch (barType) {
            case DURATION -> {
                StringBuilder str = new StringBuilder();
                for(int i = 0; i < 12; i++)
                    if(i == (int)(percent*12))
                        str.append("\uD83D\uDD18"); // 🔘
                    else
                        str.append("▬");
                return str.toString();
            }
            case FILL -> {
                StringBuilder str = new StringBuilder();

                if (percent * 12 == 0L) {
                    for (int i = 0; i < 12; i++) {
                        if (i == 0)
                            str.append("⬜");
                        else if (i == 11)
                            str.append("⬜");
                        else
                            str.append("⬜");
                    }
                } else {
                    for (int i = 0; i < 12; i++)
                        if (i <= (int) (percent * 12)) {
                            if (i == 0)
                                str.append("⬛");
                            else if (i == 11)
                                str.append("⬛");
                            else
                                str.append("⬛");
                        } else {
                            if (i == 0)
                                str.append("⬜");
                            else if (i == 11)
                                str.append("⬜");
                            else
                                str.append("⬜");
                        }
                }
                return str.toString();
            }
        }
        throw new NullPointerException("Something went wrong!");
    }

    public enum ProgressBar {
        DURATION,
        FILL
    }

    public static String trimString(String string, String delimiter) {
        if (!string.contains(delimiter)) return string;

        switch (delimiter) {
            case "?", "^", "[", ".", "$", "{", "&", "(", "+", ")", "|", "<", ">", "]", "}"
                    -> delimiter = "\\\\" + delimiter;
        }

        return string.replaceAll(delimiter+"[a-zA-Z0-9~!@#$%^&*()\\-_=;:'\"|\\\\,./]*", "");
    }

    public static String getJoinedString(java.util.List<String> args, int startIndex) {
        StringBuilder arg = new StringBuilder();
        for (int i = startIndex; i < args.size(); i++)
            arg.append(args.get(i)).append((i < args.size() - 1) ? " " : "");
        return  arg.toString();
    }

    public static long getTimeFromMillis(long time, TimeUnit unit) {
        return GeneralUtils.getTimeFromSeconds(TimeUnit.MILLISECONDS.toSeconds(time), unit);
    }

    public static long getTimeFromSeconds(long time, TimeUnit unit) {
        return switch (unit) {
            case SECONDS -> ((time % 86400) % 3600) % 60;
            case MINUTES -> ((time % 86400 ) % 3600 ) / 60;
            case HOURS -> (time % 86400 ) / 3600;
            default -> throw new IllegalArgumentException("The enum provided isn't a supported enum!");
        };
    }

    public static long getTimeFromMillis(Duration duration, TimeUnit unit) {
        return switch (unit) {
            case SECONDS -> duration.toSeconds() % 60;
            case MINUTES ->  duration.toMinutes() % 60;
            case HOURS -> duration.toHours() % 24;
            case DAYS -> duration.toDays();
            default -> throw new IllegalArgumentException("The enum provided isn't a supported enum!");
        };
    }

    public static String getDurationString(long duration) {
        long second = GeneralUtils.getTimeFromMillis(Duration.ofMillis(duration), TimeUnit.SECONDS);
        long minute = GeneralUtils.getTimeFromMillis(Duration.ofMillis(duration), TimeUnit.MINUTES);
        long hour = GeneralUtils.getTimeFromMillis(Duration.ofMillis(duration), TimeUnit.HOURS);
        long day = GeneralUtils.getTimeFromMillis(Duration.ofMillis(duration), TimeUnit.DAYS);
        return ((day > 0) ? day + ((day > 1) ? " days, " : " day, ") : "")
                + ((hour > 0) ? hour + ((hour > 1) ? " hours, " : " hour, ") : "")
                + ((minute > 0) ? minute + ((minute > 1) ? " minutes, " : " minute, ") : "")
                + second + ((second > 1) ? " seconds" : (second == 0 ? " seconds" : " second"));
    }

    public static String formatDate(long date, TimeFormat style) {
        return switch (style) {
            case DD_MMMM_YYYY, MM_DD_YYYY, DD_MMMM_YYYY_ZZZZ, DD_M_YYYY_HH_MM_SS,
                    E_DD_MMM_YYYY_HH_MM_SS_Z, MMM_DD_YYYY_HH_MM_SS -> new SimpleDateFormat(style.toString()).format(date);
            default -> throw new IllegalArgumentException("The enum provided isn't a supported enum!");
        };
    }

    public static String formatTime(long duration) {
        return DurationFormatUtils.formatDuration(duration, "HH:mm:ss");
    }

    public static String formatTime(long duration, String format) {
        return DurationFormatUtils.formatDuration(duration, format);
    }

    public static boolean isValidDuration(String timeUnparsed) {
        String durationStrRegex = "^\\d*[sSmMhHdD]$";
        return Pattern.matches(durationStrRegex, timeUnparsed);
    }

    public static long getFutureTime(String timeUnparsed) {
        String timeDigits       = timeUnparsed.substring(0, timeUnparsed.length()-1);
        char duration           = timeUnparsed.charAt(timeUnparsed.length()-1);
        long scheduledDuration;

        if (Integer.parseInt(timeDigits) < 0)
            throw new IllegalArgumentException("The time cannot be negative!");

        if (GeneralUtils.stringIsInt(timeDigits))
            switch (duration) {
                case 's' -> scheduledDuration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Integer.parseInt(timeDigits));
                case 'm' -> scheduledDuration = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(Integer.parseInt(timeDigits));
                case 'h' -> scheduledDuration = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(Integer.parseInt(timeDigits));
                case 'd' -> scheduledDuration = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(Integer.parseInt(timeDigits));
                default -> throw new IllegalArgumentException("The duration specifier \""+duration+"\" is invalid!");
            }
        else throw new IllegalArgumentException("There was no valid integer provided!");

        return scheduledDuration;
    }

    public static long getStaticTime(String timeUnparsed) {
        return getFutureTime(timeUnparsed) - System.currentTimeMillis();
    }

    public static String formatDuration(String timeUnparsed) {
        String ret;
        String timeDigits       = timeUnparsed.substring(0, timeUnparsed.length()-1);
        char duration           = timeUnparsed.charAt(timeUnparsed.length()-1);

        if (GeneralUtils.stringIsInt(timeDigits))
            switch (duration) {
                case 's' -> ret = timeDigits + " seconds";
                case 'm' -> ret = timeDigits + " minutes";
                case 'h' -> ret = timeDigits + " hours";
                case 'd' -> ret = timeDigits + " days";
                default -> throw new IllegalArgumentException("The duration specifier \""+duration+"\" is invalid!");
            }
        else throw new IllegalArgumentException("There was no valid integer provided!");

        return ret;
    }

    public static String getFileContent(String path) {
        String ret = null;
        try {
            ret = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            logger.error("[FATAL ERROR] There was an error reading from the file!", e);
        }

        if (ret == null) throw new NullPointerException();

        return ret.replaceAll("\t\n", "");
    }

    public static String getFileContent(Path path) {
        String ret = null;
        try {
            ret = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            logger.error("[FATAL ERROR] There was an error reading from the file!", e);
        }

        if (ret == null) throw new NullPointerException();

        return ret.replaceAll("\t\n", "");
    }

    public static void setFileContent(String path, String content) throws IOException {
        File file = new File(path);
        if (!file.exists())
            file.createNewFile();

        FileWriter writer = new FileWriter(path, false);
        writer.write(content);
        writer.close();
    }

    public static void setFileContent(Path path, String content) throws IOException {
        File file = new File(String.valueOf(path));
        if (!file.exists())
            file.createNewFile();

        FileWriter writer = new FileWriter(String.valueOf(path), false);
        writer.write(content);
        writer.close();
    }

    public static void setFileContent(File passedFile, String content) throws IOException {
        File file = new File(passedFile.getPath());
        if (!file.exists())
            file.createNewFile();

        FileWriter writer = new FileWriter(passedFile.getPath(), false);
        writer.write(content);
        writer.close();
    }

    @SneakyThrows
    public static void makeDir(String path) {
        if (!Files.exists(Path.of(path)))
            Files.createDirectory(Path.of(path));
    }

    @SneakyThrows
    public static void createFile(Path path, String fileName) {
        Path fullPath = Path.of(path.toString() + "/" + fileName);
        if (!Files.exists(fullPath))
            Files.createFile(fullPath);
    }

    public static void setDefaultEmbed() {
        SupportifyEmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(EMBED_COLOR)
                        .setAuthor(BotConstants.EMBED_TITLE.toString(), null, BotConstants.EMBED_LOGO.toString())
        );
    }

    public static void setCustomEmbed(Color color) {
        SupportifyEmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(color)
        );
    }

    public static void setCustomEmbed(String author, Color color) {
        SupportifyEmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(color)
                        .setAuthor(author, null, BotConstants.EMBED_LOGO.toString())
        );
    }

    public static void setCustomEmbed(String author) {
        SupportifyEmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(EMBED_COLOR)
                        .setAuthor(author, null, BotConstants.EMBED_LOGO.toString())
        );
    }

    public static void setCustomEmbed(String author, String footer) {
        SupportifyEmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(EMBED_COLOR)
                        .setAuthor(author,null, BotConstants.EMBED_LOGO.toString())
                        .setFooter(footer)
        );
    }

    public static void setCustomEmbed(String author, @Nullable String title, String footer) {
        SupportifyEmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setAuthor(author, null, BotConstants.EMBED_LOGO.toString())
                        .setColor(EMBED_COLOR)
                        .setTitle(title)
                        .setFooter(footer)
        );
    }

    public static void setCustomEmbed(String title, Color color, String footer) {
        SupportifyEmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(color)
                        .setTitle(title)
                        .setFooter(footer)
        );
    }

    public static Color parseColor(String hex) {
        return Color.decode(hex);
    }

    public static int parseNumEmoji(String emoji) {
        switch (emoji) {
            case "0️⃣" -> {
                return 0;
            }
            case "1️⃣" -> {
                return 1;
            }
            case "2️⃣" -> {
                return 2;
            }
            case "3️⃣" -> {
                return 3;
            }
            case "4️⃣" -> {
                return 4;
            }
            case "5️⃣" -> {
                return 5;
            }
            case "6️⃣" -> {
                return 6;
            }
            case "7️⃣" -> {
                return 7;
            }
            case "8️⃣" -> {
                return 8;
            }
            case "9️⃣" -> {
                return 9;
            }
            default -> throw new IllegalArgumentException("Invalid argument \""+emoji+"\"");
        }
    }

    public static String parseNumEmoji(int num) {
        switch (num) {
            case 0 -> {
                return "0️⃣";
            }
            case 1 -> {
                return "1️⃣";
            }
            case 2 -> {
                return "2️⃣";
            }
            case 3 -> {
                return "3️⃣";
            }
            case 4 -> {
                return "4️⃣";
            }
            case 5 -> {
                return "5️⃣";
            }
            case 6 -> {
                return "6️⃣";
            }
            case 7 -> {
                return "7️⃣";
            }
            case 8 -> {
                return "8️⃣";
            }
            case 9 -> {
                return "9️⃣";
            }
            default -> throw new IllegalArgumentException("Invalid argument \""+num+"\"");
        }
    }

    public static String toSafeString(String str) {
        return Pattern.quote(str);
    }

    public static String toMention(long id, Mentioner mentioner) {
        return listOfIDsToMentions(java.util.List.of(id), mentioner);
    }

    public static String listOfIDsToMentions(List<Long> mentions, Mentioner mentioner) {
        String mentionTag = null;
        switch (mentioner) {
            case USER -> mentionTag = "@";
            case ROLE -> mentionTag = "@&";
            case CHANNEL -> mentionTag = "#";
        }

        final var sb = new StringBuilder();
        for (int i = 0; i < mentions.size(); i++) {
            var elem = mentions.get(i);
            sb.append("<").append(mentionTag).append(elem.toString()).append(">")
                    .append(i != mentions.size() - 1 ? ", " : "");
        }
        return sb.isEmpty() ? "Nothing is here" :  sb.toString();
    }

    public enum Mentioner {
        USER,
        ROLE,
        CHANNEL
    }
}
