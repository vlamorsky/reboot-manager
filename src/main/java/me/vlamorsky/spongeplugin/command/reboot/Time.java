package me.vlamorsky.spongeplugin.command.reboot;

import me.vlamorsky.spongeplugin.RebootManager;
import me.vlamorsky.spongeplugin.task.ShutdownTask;
import me.vlamorsky.spongeplugin.util.TextCreator;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import me.vlamorsky.spongeplugin.task.TimeCheckerThread;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Time implements CommandExecutor {

    private TextCreator textCreator;

    public Time() {
        textCreator = RebootManager.getInstance().getTextCreator();
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {

        if (!(source instanceof Player)) {
            return CommandResult.empty();
        }

        if (isShutDownTaskInitialized()) {
            source.sendMessage(textCreator.getMessageServerIsRestarting());
            return CommandResult.success();
        }

        if (!TimeCheckerThread.haveRebootTask()) {
            source.sendMessage(textCreator.getMessageNoScheduledTasks());
            return CommandResult.success();
        }

        int secondsUntilRestart = new Long(ChronoUnit.SECONDS
                .between(LocalDateTime.now(), TimeCheckerThread.getRestartDateTime())).intValue();

        int hour = secondsUntilRestart / 3600;
        int minute = (secondsUntilRestart % 3600) / 60;
        int second = secondsUntilRestart % 60;

        if (secondsUntilRestart > 0) {
            source.sendMessage(textCreator.getMessageTimeUntilRestart(hour, minute, second));
        } else {
            source.sendMessage(textCreator.getMessageServerIsRestarting());
        }

        return CommandResult.success();
    }

    public boolean isShutDownTaskInitialized() {
        return (ShutdownTask.getInstance() != null);
    }
}
