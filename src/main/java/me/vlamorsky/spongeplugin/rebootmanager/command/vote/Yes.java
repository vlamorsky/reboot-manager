package me.vlamorsky.spongeplugin.rebootmanager.command.vote;

import me.vlamorsky.spongeplugin.rebootmanager.RebootManager;
import me.vlamorsky.spongeplugin.rebootmanager.config.Config;
import me.vlamorsky.spongeplugin.rebootmanager.task.VoteThread;
import me.vlamorsky.spongeplugin.rebootmanager.util.TextCreator;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.title.Title;

public class Yes implements CommandExecutor {

    private final VoteThread voteThread;
    private Config config;
    private TextCreator textCreator;

    public Yes() {
        config = RebootManager.getInstance().getConfig();
        textCreator = RebootManager.getInstance().getTextCreator();
        voteThread = RebootManager.getInstance().getVoteThread();
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
        player.sendTitle(Title.builder()
                .subtitle(textCreator.fromLegacy("Ваш &2голос &rпринят"))
                .stay(20)
                .build());


        player.spawnParticles(ParticleEffect.builder()
                .type(ParticleTypes.HAPPY_VILLAGER)
                .build(), player.getPosition().add(0.6, 1, 0));
        player.spawnParticles(ParticleEffect.builder()
                .type(ParticleTypes.HAPPY_VILLAGER)
                .build(), player.getPosition().add(-0.6, 1, 0));
        player.spawnParticles(ParticleEffect.builder()
                .type(ParticleTypes.HAPPY_VILLAGER)
                .build(), player.getPosition().add(0, 1, 0.6));
        player.spawnParticles(ParticleEffect.builder()
                .type(ParticleTypes.HAPPY_VILLAGER)
                .build(), player.getPosition().add(0, 1, -0.6));



        player.playSound(SoundTypes.ENTITY_VILLAGER_YES, player.getPosition(), 4000);

        return CommandResult.success();
    }
}
