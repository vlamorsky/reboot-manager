package me.vlamorsky.spongeplugin.command.reboot;

import me.vlamorsky.spongeplugin.task.ShutdownTask;
import me.vlamorsky.spongeplugin.task.TimeCheckerThread;
import me.vlamorsky.spongeplugin.util.TextCreator;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class Cancel implements CommandExecutor {
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

        TimeCheckerThread.cancelRebootTask();
        source.sendMessage(TextCreator.getMessageTaskCancelled());

        return CommandResult.success();
    }

    public boolean isShutDownTaskInitialized() {
        return (ShutdownTask.getInstance() != null);
    }
}
