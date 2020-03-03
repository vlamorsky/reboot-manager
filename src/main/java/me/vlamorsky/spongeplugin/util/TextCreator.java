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
        if (hour >= 11 && hour <= 14) {
            return "&7 часов";
        }

        switch (hour % 10) {
            case 1:
                ending = "&7 час";
                break;
            case 2: case 3:
                ending = "&7 часа";
                break;
            case 5: case 6: case 7: case 8: case 9: case 0:
                ending = "&7 часов";
                break;
            default:
                ending = "&7 ч.";
        }
        return ending;
    }

    public static String endOfMinutes(int minute) {
        String ending;
        if (minute >= 11 && minute <= 14) {
            return "&7 минут";
        }

        switch (minute % 10) {
            case 1:
                ending = "&7 минута";
                break;
            case 2: case 3:
                ending = "&7 минуты";
                break;
            case 5: case 6: case 7: case 8: case 9: case 0:
                ending = "&7 минут";
                break;
            default:
                ending = "&7 мин.";
        }
        return ending;
    }

    public static String endOfSeconds(int second) {
        String ending;
        if (second >= 11 && second <= 14) {
            return "&7 секунд";
        }

        switch (second % 10) {
            case 1:
                ending = "&7 секунда";
                break;
            case 2: case 3:
                ending = "&7 секунды";
                break;
            case 5: case 6: case 7: case 8: case 9: case 0:
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
