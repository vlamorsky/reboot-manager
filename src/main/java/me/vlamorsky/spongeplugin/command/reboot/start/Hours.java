package me.vlamorsky.spongeplugin.command.reboot.start;

import me.vlamorsky.spongeplugin.task.TimeCheckerThread;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import java.time.LocalDateTime;
import java.util.Optional;

public class Hours implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if (!(src instanceof Player)) {
            return CommandResult.empty();
        }

        int timeAmount = args.<Integer>getOne("time").get();
        Optional<String> reasonOP = args.getOne("reason");
        if (reasonOP.isPresent()) {
            TimeCheckerThread.setRestartDateTime(LocalDateTime.now().plusHours(timeAmount), reasonOP.get());
        } else {
            TimeCheckerThread.setRestartDateTime(LocalDateTime.now().plusHours(timeAmount));
        }

        return CommandResult.success();
    }
}
