package me.vlamorsky.spongeplugin.util;

import me.vlamorsky.spongeplugin.RebootManager;
import me.vlamorsky.spongeplugin.config.Config;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class TextCreator {

    private Config config;

    public TextCreator(Config config) {
        this.config = config;
    }

    public Text fromLegacy(String legacy) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked(legacy);
    }

    public String endOfHours(int hour) {
        String ending;
        if (hour >= 11 && hour <= 14) {
            return "&7 часов";
        }

        switch (hour % 10) {
            case 1:
                ending = "&7 час";
                break;
            case 2: case 3: case 4:
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
            case 2: case 3: case 4:
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
            case 2: case 3: case 4:
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

    public Text getMessageTimeUntilNextVoting(int hour, int minute, int second) {
        StringBuilder stringMessage = new StringBuilder(config.MESSAGE_PREFIX + " &7До следующего голосования");
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

    public Text getMessageTimeUntilRestart(int hour, int minute, int second) {
        StringBuilder stringMessage = new StringBuilder("&7До перезагрузки сервера");
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

    public Text getMessageServerWillRestartSoon() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Голосование недоступно&8. &7Сервер скоро перезагрузится&8.");
    }

    public Text getMessageServerIsRestarting() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Сервер перезагружается&8...");
    }

    public Text getMessageNoScheduledTasks() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Запланированных перезагрузок нет&8.");
    }

    public Text getMessageTaskCancelled() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Перезагрузка сервера отменена &2успешно&8.");
    }

    public Text getMessageAskToRestart() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Перезагрузить сервер&8?  ");
    }

    public Text getMessageNotEnoughPlayers(int players) {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Необходимо как минимум &3" + players + " &7игроков онлайн&8.");
    }

    public Text getMessageAlreadyVoting() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Голосование уже идет&8.");
    }

    public Text getMessageVotingCompleted(int yesVotes, int noVotes) {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Голосование завершено  &2за &a" + yesVotes + "  &4против &c" + noVotes);
    }

    public Text getMessageNotEnoughVotes() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Недостаточно голосов для перезагрузки сервера&8.");
    }

    public Text getMessageNoActiveVoting() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Активных голосований нет&8.");
    }

    public Text getMessageInvalidTimeFormat() {
        return fromLegacy(config.MESSAGE_PREFIX + " &4Недопустимый формат времени&8. &7Используйте &3hh&8:&3mm&8.");
    }

    public Text getMessageRestartTimeWasSet() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Время перезагрузки сервера установлено &2успешно&8.");
    }

    public Text getMessageHelpTitle() {
        return fromLegacy("&6" + RebootManager.NAME + " &7v&6" + RebootManager.VERSION);
    }
}
