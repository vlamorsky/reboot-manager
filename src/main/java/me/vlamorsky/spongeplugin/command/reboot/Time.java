package me.vlamorsky.spongeplugin.command.reboot;

import me.vlamorsky.spongeplugin.task.ShutdownTask;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import me.vlamorsky.spongeplugin.task.TimeCheckerThread;
import me.vlamorsky.spongeplugin.util.TextCreator;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Time implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {

        if (!(source instanceof Player)) {
            return CommandResult.empty();
        }

        if (isShutDownTaskInitialized()) {
            source.sendMessage(TextCreator.getMessageServerIsRestarting());
            return CommandResult.success();
        }

        if (!TimeCheckerThread.haveRebootTask()) {
            source.sendMessage(TextCreator.getMessageNoScheduledTasks());
            return CommandResult.success();
        }

        int secondsUntilRestart = new Long(ChronoUnit.SECONDS
                .between(LocalDateTime.now(), TimeCheckerThread.getRestartDateTime())).intValue();

        int hour = secondsUntilRestart / 3600;
        int minute = (secondsUntilRestart % 3600) / 60;
        int second = secondsUntilRestart % 60;

        if (secondsUntilRestart > 0) {
            source.sendMessage(TextCreator.getMessageTimeUntilRestart(hour, minute, second));
        } else {
            source.sendMessage(TextCreator.getMessageServerIsRestarting());
        }

        return CommandResult.success();
    }

    public boolean isShutDownTaskInitialized() {
        return (ShutdownTask.getInstance() != null);
    }
}
