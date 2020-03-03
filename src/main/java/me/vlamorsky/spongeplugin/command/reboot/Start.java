package me.vlamorsky.spongeplugin.command.reboot;

import me.vlamorsky.spongeplugin.task.TimeCheckerThread;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import java.time.LocalDateTime;

public class Start implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if (!(src instanceof Player)) {
            return CommandResult.empty();
        }

        TimeCheckerThread.setRestartDateTime(LocalDateTime.now().plusSeconds(10L));

        return CommandResult.success();
    }
}
