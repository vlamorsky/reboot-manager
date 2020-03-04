package me.vlamorsky.spongeplugin.command.vote;

import me.vlamorsky.spongeplugin.RebootManager;
import me.vlamorsky.spongeplugin.command.reboot.Vote;
import me.vlamorsky.spongeplugin.config.Config;
import me.vlamorsky.spongeplugin.util.TextCreator;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class Yes implements CommandExecutor {

    private final Vote.VoteThread voteThread;
    private Config config;
    private TextCreator textCreator;

    public Yes() {
        config = RebootManager.getInstance().getConfig();
        textCreator = RebootManager.getInstance().getTextCreator();
        voteThread = Vote.getVoteThread();
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {

        if (!(source instanceof Player) | !config.VOTING_ENABLED) {
            return CommandResult.empty();
        }

        if (!voteThread.isVotes()) {
            source.sendMessage(textCreator.getMessageNoActiveVoting());
            return CommandResult.success();
        }

        Player player = ((Player) source).getPlayer().get();

        voteThread.receiveVote(player,"yes");

        return CommandResult.success();
    }
}
