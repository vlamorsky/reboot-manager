package me.vlamorsky.spongeplugin.command.reboot;

import me.vlamorsky.spongeplugin.RebootManager;
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

    private TextCreator textCreator;

    public Cancel() {
        this.textCreator = RebootManager.getInstance().getTextCreator();
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

        TimeCheckerThread.cancelRebootTask();
        source.sendMessage(textCreator.getMessageTaskCancelled());

        return CommandResult.success();
    }

    public boolean isShutDownTaskInitialized() {
        return (ShutdownTask.getInstance() != null);
    }
}
