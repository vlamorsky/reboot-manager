package me.vlamorsky.spongeplugin.command.vote;

import me.vlamorsky.spongeplugin.RebootManager;
import me.vlamorsky.spongeplugin.command.reboot.Vote;
import me.vlamorsky.spongeplugin.config.Config;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class No implements CommandExecutor {

    private final Vote.VoteThread voteThread;
    private Config config;

    public No() {
        config = RebootManager.getInstance().getConfig();
        voteThread = Vote.getVoteThread();
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {

        if (!config.VOTING_ENABLED | !(source instanceof Player)) {
            return CommandResult.empty();
        }

        Player player = ((Player) source).getPlayer().get();

        voteThread.receiveVote(player,"no");

        return CommandResult.success();
    }
}
