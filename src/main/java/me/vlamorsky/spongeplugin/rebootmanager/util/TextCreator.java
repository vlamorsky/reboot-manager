package me.vlamorsky.spongeplugin.rebootmanager.util;

import me.vlamorsky.spongeplugin.rebootmanager.RebootManager;
import me.vlamorsky.spongeplugin.rebootmanager.config.Config;
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
            return "&7 hours";
        }

        switch (hour % 10) {
            case 1:
                ending = "&7 hour";
                break;
            case 2: case 3: case 4:
                ending = "&7 hours";
                break;
            case 5: case 6: case 7: case 8: case 9: case 0:
                ending = "&7 hours";
                break;
            default:
                ending = "&7 h.";
        }
        return ending;
    }

    public static String endOfMinutes(int minute) {
        String ending;
        if (minute >= 11 && minute <= 14) {
            return "&7 minutes";
        }

        switch (minute % 10) {
            case 1:
                ending = "&7 minutes";
                break;
            case 2: case 3: case 4:
                ending = "&7 minutes";
                break;
            case 5: case 6: case 7: case 8: case 9: case 0:
                ending = "&7 minutes";
                break;
            default:
                ending = "&7 min.";
        }
        return ending;
    }

    public static String endOfSeconds(int second) {
        String ending;
        if (second >= 11 && second <= 14) {
            return "&7 seconds";
        }

        switch (second % 10) {
            case 1:
                ending = "&7 seconds";
                break;
            case 2: case 3: case 4:
                ending = "&7 seconds";
                break;
            case 5: case 6: case 7: case 8: case 9: case 0:
                ending = "&7 seconds";
                break;
            default:
                ending = "&7 sec.";
        }
        return ending;
    }

    public Text getMessageTimeUntilNextVoting(int hour, int minute, int second) {
        StringBuilder stringMessage = new StringBuilder(config.MESSAGE_PREFIX + " &7Time until next vote&8:");
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
        StringBuilder stringMessage = new StringBuilder("&7Time until reboot&8:");
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
        return fromLegacy(config.MESSAGE_PREFIX + " &7Voting not available. Server will reboot soon.");
    }

    public Text getMessageServerIsRestarting() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Server reboots.");
    }

    public Text getMessageNoScheduledTasks() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7No scscheduled reboots.");
    }

    public Text getMessageTaskCancelled() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Server reboot canceled successfully.");
    }

    public Text getMessageAskToRestart() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Reboot server?  ");
    }

    public Text getMessageNotEnoughPlayers(int players) {
        return fromLegacy(config.MESSAGE_PREFIX + " &7At least &3" + players + " &7players required online.");
    }

    public Text getMessageAlreadyVoting() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Voting is already underway.");
    }

    public Text getMessageVotingCompleted(int yesVotes, int noVotes) {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Voting completed, results &3" + yesVotes + "  &7vs. &3" + noVotes);
    }

    public Text getMessageNotEnoughVotes() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Not enough required votes to reboot the server.");
    }

    public Text getMessageNoActiveVoting() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7No active voting.");
    }

    public Text getMessageInvalidTimeFormat() {
        return fromLegacy(config.MESSAGE_PREFIX + " &3Invalid time format, please use &3hh&8:&3mm.");
    }

    public Text getMessageRestartTimeWasSet() {
        return fromLegacy(config.MESSAGE_PREFIX + " &7Server reboot time has &nsuccessfully &7been set!");
    }

    public Text getMessageHelpTitle() {
        return fromLegacy("&6" + RebootManager.NAME + " &7v&6" + RebootManager.VERSION);
    }

    public Text getMessageVoteInterrupted() {
        return fromLegacy(config.MESSAGE_PREFIX + " &3Voting for server reboot was interrupted!");
    }
}
