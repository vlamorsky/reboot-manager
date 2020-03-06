package me.vlamorsky.spongeplugin.command.reboot;

import me.vlamorsky.spongeplugin.RebootManager;
import me.vlamorsky.spongeplugin.task.TimeCheckerThread;
import me.vlamorsky.spongeplugin.util.TextCreator;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class Start implements CommandExecutor {

    private TextCreator textCreator;

    public Start() {
        textCreator = RebootManager.getInstance().getTextCreator();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        String time = args.<String>getOne("time").get();
        Optional<String> reasonOP = args.getOne("reason");

        try {
            LocalDateTime newRestartTime = LocalTime.parse(time).atDate(LocalDate.now());

            if (newRestartTime.isBefore(LocalDateTime.now())) {
                newRestartTime = newRestartTime.plusDays(1);
            }

            if (reasonOP.isPresent()) {
                TimeCheckerThread.setRestartDateTime(newRestartTime, reasonOP.get());
            } else {
                TimeCheckerThread.setRestartDateTime(newRestartTime);
            }

            src.sendMessage(textCreator.getMessageRestartTimeWasSet());

        } catch (DateTimeException e) {
            src.sendMessage(textCreator.getMessageInvalidTimeFormat());
            return CommandResult.empty();
        }

        return CommandResult.success();
    }
}
