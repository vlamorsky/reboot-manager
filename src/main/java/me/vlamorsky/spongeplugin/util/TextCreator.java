package me.vlamorsky.spongeplugin.util;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

public class TextCreator {

    private Text.Builder builder;
    private TextColor color = TextColors.NONE;
    private TextStyle style = TextStyles.NONE;

    public TextCreator(Text text) {
        builder = text.toBuilder();
    }

    public TextCreator(String string) {
        this(Text.of(string));
    }

    public TextCreator() {
        this(Text.EMPTY);
    }

    public TextColor getColor() {
        return color;
    }

    public TextCreator setColor(TextColor color) {

        this.color = color;
        return this;
    }

    public TextStyle getStyle() {
        return style;
    }

    public TextCreator setStyle(TextStyle style) {

        this.style = style;
        return this;
    }

    public TextCreator append(Text subText) {

        Text.Builder subBuilder = subText.toBuilder();
        subBuilder.color(getColor());
        subBuilder.style(getStyle());
        builder.append(subBuilder.build());

        return this;
    }

    public TextCreator append(String subText) {

        Text.Builder subBuilder = Text.of(subText).toBuilder();
        subBuilder.color(getColor());
        subBuilder.style(getStyle());
        builder.append(subBuilder.build());

        return this;
    }

    public Text build() {
        return builder.build();
    }

    public static Text fromLegacy(String legacy) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked(legacy);
    }

    public static String endOfHours(int hour) {
        String ending;
        switch (hour) {
            case 1: case 21:
                ending = "&7 час";
                break;
            case 2:case 3:case 4: case 22: case 23:
                ending = "&7 часа";
                break;
            case 5:case 6:case 7:case 8:case 9: case 0:
            case 10:case 11:case 12:case 13:case 14:case 15:case 16:case 17:case 18:case 19: case 20:
                ending = "&7 часов";
                break;
            default:
                ending = "&7 ч.";
        }
        return ending;
    }

    public static String endOfMinutes(int minute) {
        String ending;
        switch (minute) {
            case 1: case 21:case 31:case 41:case 51:
                ending = "&7 минута";
                break;
            case 2:case 3:case 4:case 22:case 23:case 24:case 32:case 33:case 34:case 42:case 43:case 44:case 52:case 53:case 54:
                ending = "&7 минуты";
                break;
            case 5:case 6:case 7:case 8:case 9:case 0:
            case 10:case 11:case 12:case 13:case 14:case 15:case 16:case 17:case 18:case 19:
            case 25:case 26:case 27:case 28:case 29:case 20:
            case 35:case 36:case 37:case 38:case 39:case 30:
            case 45:case 46:case 47:case 48:case 49:case 40:
            case 55:case 56:case 57:case 58:case 59:
                ending = "&7 минут";
                break;
            default:
                ending = "&7 мин.";
        }
        return ending;
    }

    public static String endOfSeconds(int second) {
        String ending;
        switch (second) {
            case 1: case 21:case 31:case 41:case 51:
                ending = "&7 секунда";
                break;
            case 2:case 3:case 4:case 22:case 23:case 24:case 32:case 33:case 34:case 42:case 43:case 44:case 52:case 53:case 54:
                ending = "&7 секунды";
                break;
            case 5:case 6:case 7:case 8:case 9:case 0:
            case 10:case 11:case 12:case 13:case 14:case 15:case 16:case 17:case 18:case 19:
            case 25:case 26:case 27:case 28:case 29:case 20:
            case 35:case 36:case 37:case 38:case 39:case 30:
            case 45:case 46:case 47:case 48:case 49:case 40:
            case 55:case 56:case 57:case 58:case 59:
                ending = "&7 секунд";
                break;
            default:
                ending = "&7 сек.";
        }
        return ending;
    }

    public static Text getMessageTimeUntilNextVoting(int hour, int minute, int second) {
        StringBuilder stringMessage = new StringBuilder("&8[&6REBOOT&8] &7До следующего голосования");
        if (hour != 0) {
            stringMessage.append("&3 " + hour + endOfHours(hour));
        }
        if (minute != 0) {
            stringMessage.append("&3 " + minute + endOfMinutes(minute));
        }
        if (second != 0) {
            stringMessage.append("&6 " + second + endOfSeconds(second));
        }
        return fromLegacy(stringMessage.toString());
    }

    public static Text getMessageTimeUntilRestart(int hour, int minute, int second) {
        StringBuilder stringMessage = new StringBuilder("&8[&6REBOOT&8] &7До перезагрузки сервера");
        if (hour != 0) {
            stringMessage.append("&6 " + hour + endOfHours(hour));
        }
        if (minute != 0) {
            stringMessage.append("&6 " + minute + endOfMinutes(minute));
        }
        if (second != 0) {
            stringMessage.append("&6 " + second + endOfSeconds(second));
        }
        return fromLegacy(stringMessage.toString());
    }

    public static Text getMessageServerWillRestartSoon() {
        return fromLegacy("&8[&6REBOOT&8] &7Голосование недоступно&8. &7Сервер скоро перезагрузится&8.");
    }

    public static Text getMessageServerIsRestarting() {
        return fromLegacy("&8[&6REBOOT&8] &7Сервер перезагружается&8...");
    }

    public static Text getMessageNoScheduledTasks() {
        return fromLegacy("&8[&6REBOOT&8] &7Запланированных перезагрузок нет&8.");
    }

    public static Text getMessageTaskCancelled() {
        return fromLegacy("&8[&6REBOOT&8] &7Перезагрузка сервера отменена &2успешно&8.");
    }
}
