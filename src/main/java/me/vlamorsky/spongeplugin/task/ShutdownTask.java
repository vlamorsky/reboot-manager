package me.vlamorsky.spongeplugin.task;

import com.flowpowered.math.vector.Vector3d;
import me.vlamorsky.spongeplugin.config.Config;
import me.vlamorsky.spongeplugin.util.TextCreator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import me.vlamorsky.spongeplugin.RebootManager;
import org.spongepowered.api.text.title.Title;

import java.util.Optional;

public class ShutdownTask implements Runnable {
    private int timer;
    private float percent;
    private ServerBossBar serverBossBar;
    private static ShutdownTask instance;

    private SoundType soundShotDown;
    private SoundType soundPreShotDown;

    private Config config;
    private String reasonMessage;

    public ShutdownTask(String reasonMessage) {
        config = RebootManager.getInstance().getConfig();
        loadConfigs();
        this.reasonMessage = reasonMessage;
        timer = 10;
        percent = timer / 10;
        serverBossBar = ServerBossBar.builder()
                .name(new TextCreator()
                        .append("До рестарта: " + timer)
                        .build())
                .playEndBossMusic(true)
                .color(BossBarColors.GREEN)
                .overlay(BossBarOverlays.PROGRESS)
                .percent(percent)
                .build();
        instance = this;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {

            if (timer <= 0) {
                RebootManager.getInstance().stopServer(reasonMessage);
                break;
            }
            showBossBarMessage();
            //showBoardMessage();
            showChatMessage();
            showTitleMessage();
            playSound();

            timer--;
            percent = (float)timer / 10f;

            try {
                Thread.currentThread().sleep(1001);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void showBossBarMessage() {
        serverBossBar
                .setName(TextCreator.fromLegacy(reasonMessage))
                .setPercent(percent);

        serverBossBar.addPlayers(RebootManager.getInstance().getGame().getServer().getOnlinePlayers());
    }

    private void showBoardMessage() {

        Scoreboard scoreboard = Scoreboard.builder().build();
        Objective objective = Objective.builder()
                .name("restart_board")
                .criterion(Criteria.DUMMY)
                .displayName(new TextCreator()
                        .setColor(TextColors.DARK_RED)
                        .append(Text.of("Info: "))
                        .build())
                .build();
        scoreboard.addObjective(objective);

        objective.getOrCreateScore(Text.builder("До рестарта: ").color(TextColors.GREEN).build()).setScore(timer);

        scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);

        RebootManager.getInstance().getGame().getServer()
                .getOnlinePlayers().forEach(player -> player.setScoreboard(scoreboard));
    }

    private void showChatMessage() {
        Sponge.getServer().getBroadcastChannel().send(
                TextCreator.fromLegacy("&8[&6REBOOT&8] &7До перезагрузки сервера&6" + timer + TextCreator.endOfSeconds(timer)));
    }

    private void showTitleMessage() {

        Sponge.getServer().getOnlinePlayers()
                .forEach(player -> player.sendTitle(Title
                        .builder()
                        .title(TextCreator.fromLegacy("&7До перезагрузки сервера&6" + timer + TextCreator.endOfSeconds(timer)))
                        .subtitle(TextCreator.fromLegacy("&7" + reasonMessage))
                        .stay(40)
                        .build()));
    }

    private void playSound() {

        if (!config.SOUND_ENABLED) {
            return;
        }

        SoundType soundType;
        if (timer == 0) {
             soundType = soundShotDown;
        } else {
            soundType = soundPreShotDown;
        }

        Vector3d vector3d = new Vector3d(0, 0, 0);

        Sponge.getServer().getWorlds().forEach(world -> {

            world.playSound(soundType, vector3d, 4000);

        });
    }

    private void loadConfigs() {
        Optional<SoundType> cfgPreShutdownSong = Sponge.getGame()
                .getRegistry().getType(SoundType.class, config.SOUND_BASIC_NOTIFICATION);

        Optional<SoundType> cfgShutdownSong = Sponge.getGame()
                .getRegistry().getType(SoundType.class, config.SOUND_LAST_NOTIFICATION);

        soundPreShotDown = cfgPreShutdownSong.orElse(SoundTypes.ENTITY_CAT_AMBIENT);
        soundShotDown = cfgShutdownSong.orElse(SoundTypes.ENTITY_CAT_HURT);
    }

    public static ShutdownTask getInstance() {
        return instance;
    }
}
