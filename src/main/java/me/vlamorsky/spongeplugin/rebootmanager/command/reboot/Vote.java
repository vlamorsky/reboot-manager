package me.vlamorsky.spongeplugin.rebootmanager.command.reboot;

import com.flowpowered.math.vector.Vector3d;
import me.vlamorsky.spongeplugin.rebootmanager.task.VoteThread;
import me.vlamorsky.spongeplugin.rebootmanager.util.TextCreator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import me.vlamorsky.spongeplugin.rebootmanager.RebootManager;
import me.vlamorsky.spongeplugin.rebootmanager.config.Config;
import org.spongepowered.api.text.title.Title;

public class Vote implements CommandExecutor {

    private VoteThread voteThread;

    private Config config;
    private TextCreator textCreator;

    public Vote() {
        config = RebootManager.getInstance().getConfig();
        textCreator = RebootManager.getInstance().getTextCreator();
        voteThread = RebootManager.getInstance().getVoteThread();
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {

        if (!config.VOTING_ENABLED) {
            return CommandResult.empty();
        }

        if (voteThread.ableToStartVoting(source)) {

            voteThread.startVoting();

            Text votingMessage = Text.builder()
                    .append(Text.builder()
                            .append(textCreator.getMessageAskToRestart())
                            .build())
                    .append(Text.builder()
                            .append(textCreator.fromLegacy("&8[&2Да&8]"))
                            .onClick(TextActions.runCommand("/reboot vote yes"))
                            .build())
                    .append(Text.builder()
                            .append(Text.of("  "))
                            .build())
                    .append(Text.builder()
                            .append(textCreator.fromLegacy("&8[&4Нет&8]"))
                            .onClick(TextActions.runCommand("/reboot vote no"))
                            .build())
                    .append(Text.builder()
                            .append(textCreator.fromLegacy(" &8<- &7кликабельно"))
                            .build())
                    .build();

            broadcastMessage(votingMessage);
        }

        return CommandResult.success();
    }

    private void broadcastMessage(Text message) {
        Sponge.getServer().getBroadcastChannel().send(message);

        Sponge.getServer().getWorlds().forEach(world -> {

            if (config.VOTING_SOUND_ENABLED) {
                world.playSound(SoundTypes.ENTITY_FIREWORK_LAUNCH, new Vector3d(0, 60, 0), 4000);
            }

            if (config.VOTING_TITLE_ENABLED) {
                world.sendTitle(Title.builder()
                        .subtitle(textCreator.fromLegacy("Началось голосование за перезагрузку сервера"))
                        .title(Text.of(""))
                        .stay(40)
                        .build());
            }

        });
    }
}
