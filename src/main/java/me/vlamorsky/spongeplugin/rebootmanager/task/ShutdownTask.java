package me.vlamorsky.spongeplugin.rebootmanager.task;

import com.flowpowered.math.vector.Vector3d;
import me.vlamorsky.spongeplugin.rebootmanager.RebootManager;
import me.vlamorsky.spongeplugin.rebootmanager.config.Config;
import me.vlamorsky.spongeplugin.rebootmanager.util.TextCreator;
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
    private TextCreator textCreator;
    private String reasonMessage;

    public ShutdownTask(String reasonMessage) {
        config = RebootManager.getInstance().getConfig();
        loadConfigs();
        textCreator = RebootManager.getInstance().getTextCreator();
        this.reasonMessage = reasonMessage;
        timer = 10;
        percent = timer / 10f;
        serverBossBar = ServerBossBar.builder()
                .name(textCreator.fromLegacy(reasonMessage))
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
                .setName(textCreator.fromLegacy(reasonMessage))
                .setPercent(percent);

        serverBossBar.addPlayers(RebootManager.getInstance().getGame().getServer().getOnlinePlayers());
    }

    private void showBoardMessage() {

        Scoreboard scoreboard = Scoreboard.builder().build();
        Objective objective = Objective.builder()
                .name("restart_board")
                .criterion(Criteria.DUMMY)
                .displayName(textCreator.fromLegacy("&2INFO"))
                .build();
        scoreboard.addObjective(objective);

        objective.getOrCreateScore(Text.builder("До рестарта: ").color(TextColors.GREEN).build()).setScore(timer);

        scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);

        RebootManager.getInstance().getGame().getServer()
                .getOnlinePlayers().forEach(player -> player.setScoreboard(scoreboard));
    }

    private void showChatMessage() {
        Sponge.getServer().getBroadcastChannel().send(
                textCreator.getMessageTimeUntilRestart(0, 0, timer));
    }

    private void showTitleMessage() {

        Sponge.getServer().getOnlinePlayers()
                .forEach(player -> player.sendTitle(Title
                        .builder()
                        .title(textCreator.getMessageTimeUntilRestart(0, 0, timer))
                        .subtitle(textCreator.fromLegacy("&7" + reasonMessage))
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
