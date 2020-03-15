package me.vlamorsky.spongeplugin.rebootmanager.command.vote;

import me.vlamorsky.spongeplugin.rebootmanager.RebootManager;
import me.vlamorsky.spongeplugin.rebootmanager.task.VoteThread;
import me.vlamorsky.spongeplugin.rebootmanager.util.TextCreator;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class Cancel implements CommandExecutor {

    private TextCreator textCreator;
    private VoteThread voteThread;

    public Cancel() {
        textCreator = RebootManager.getInstance().getTextCreator();
        voteThread = RebootManager.getInstance().getVoteThread();
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!isVotes()) {
            source.sendMessage(textCreator.getMessageNoActiveVoting());
            return CommandResult.success();
        }
        voteThread.cancelVote();

        return CommandResult.success();
    }

    private boolean isVotes() {
        return voteThread.isVotes();
    }
}
