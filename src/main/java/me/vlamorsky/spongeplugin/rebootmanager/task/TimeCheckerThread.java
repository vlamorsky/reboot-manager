package me.vlamorsky.spongeplugin.rebootmanager.task;

import com.flowpowered.math.vector.Vector3d;
import me.vlamorsky.spongeplugin.rebootmanager.RebootManager;
import me.vlamorsky.spongeplugin.rebootmanager.config.Config;
import me.vlamorsky.spongeplugin.rebootmanager.util.TextCreator;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.title.Title;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class TimeCheckerThread extends Thread {
    private static TimeCheckerThread instance;

    private final Set<Integer> restartIntervals;
    private final Set<Long> notifyIntervals;

    private static LocalDateTime restartDateTime;
    private long secondsRemaining;
    private SoundType notifySound;
    private boolean hasRebootTask;

    private final Config config;
    private static String reasonMessage;
    private TextCreator textCreator;

    public static TimeCheckerThread getInstance() {
        return instance;
    }

    public TimeCheckerThread() {
        instance = this;
        config = RebootManager.getInstance().getConfig();
        textCreator = RebootManager.getInstance().getTextCreator();
        notifyIntervals = new TreeSet<>();
        restartIntervals = new TreeSet<>();
        hasRebootTask = config.AUTORESTART_ENABLED;

        try {
            loadConfigs();
            initRestartTimeValues();
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {

            if (hasRebootTask) {
                secondsRemaining = ChronoUnit
                        .SECONDS.between(LocalDateTime.now(), restartDateTime);

                notifyPlayers();
                if (secondsRemaining <= 10) {
                    Task.builder()
                            .execute(new ShutdownTask(reasonMessage))
                            .async()
                            .submit(RebootManager.getInstance());
                    break;
                }
            }

            try {
                Thread.currentThread().sleep(750);
            } catch (InterruptedException e) {
                //ignored
            }
        }
    }

    private void loadConfigs() throws ObjectMappingException {
        config.AUTORESTART_INTERVALS.forEach(cfg -> {
            if (cfg.length() <= 2) {
                int hour = Integer.parseInt(cfg);

                if (hour <= 23 && hour >= 0) {
                    restartIntervals.add(hour);
                }
            }
        });

        notifyIntervals.addAll(config.NOTIFY_INTERVALS);

        Optional<SoundType> cfgPreShutdownSong = Sponge.getGame()
                .getRegistry().getType(SoundType.class, config.SOUND_BASIC_NOTIFICATION);

        notifySound = cfgPreShutdownSong.orElse(SoundTypes.ENTITY_CAT_AMBIENT);
        reasonMessage = config.AUTORESTART_REASON;
    }

    private void initRestartTimeValues() {
        int hourCurrent = LocalDateTime.now().getHour();
        LocalDateTime startTime = LocalDateTime.now();

        int minDelta = 24;
        int nextRestart = -1;

        int minInterval = 23;

        for (int interval : restartIntervals) {

            if (interval > hourCurrent) {
                if (interval - hourCurrent < minDelta) {
                    minDelta = interval - hourCurrent;
                    nextRestart = interval;
                }
            }

            if (interval < minInterval) {
                minInterval = interval;
            }

        }

        if (nextRestart != -1) {
            restartDateTime = LocalDateTime.of(
                    startTime.getYear(), startTime.getMonth(),
                    startTime.getDayOfMonth(), nextRestart,
                    0, 0);
        } else {
            restartDateTime = LocalDateTime.of(
                    startTime.getYear(), startTime.getMonth(),
                    startTime.getDayOfMonth(), minInterval,
                    0, 0);

            LocalDateTime nextDayRestart = startTime.plusDays(1);
            restartDateTime = LocalDateTime.of(
                    nextDayRestart.getYear(), nextDayRestart.getMonth(),
                    nextDayRestart.getDayOfMonth(), minInterval,
                    0, 0);
        }

    }

    private void notifyPlayers() {

        if (notifyIntervals.contains(secondsRemaining)) {
            notifyIntervals.remove(secondsRemaining);
        } else {
            return;
        }

        playSound();

        int secondsUntilRestart = new Long(secondsRemaining).intValue();
        int hour = secondsUntilRestart / 3600;
        int minute = (secondsUntilRestart % 3600) / 60;
        int second = secondsUntilRestart % 60;

        Sponge.getServer().getBroadcastChannel()
                .send(textCreator.getMessageTimeUntilRestart(hour, minute, second));

        Sponge.getServer().getOnlinePlayers()
                .forEach(player -> player.sendTitle(Title
                        .builder()
                        .title(textCreator.getMessageTimeUntilRestart(hour, minute, second))
                        .subtitle(textCreator.fromLegacy("&7" + reasonMessage))
                        .stay(60)
                        .build()));
    }

    private void playSound() {
        if (!config.SOUND_ENABLED) {
            return;
        }

        Vector3d vector3d = new Vector3d(0, 0, 0);

        Sponge.getServer().getWorlds().forEach(world -> {

            world.playSound(notifySound, vector3d, 4000);

        });
    }

    public static LocalDateTime getRestartDateTime() {
        return restartDateTime;
    }

    public static void setRestartDateTime(LocalDateTime newRestartDateTime) {
        restartDateTime = newRestartDateTime;

        getInstance().notifyIntervals
                .addAll(getInstance().config.NOTIFY_INTERVALS);

        getInstance().hasRebootTask = true;
        getInstance().interrupt();
    }

    public static void setRestartDateTime(LocalDateTime newRestartDateTime, String reasonMessage) {
        restartDateTime = newRestartDateTime;
        TimeCheckerThread.reasonMessage = reasonMessage;

        getInstance().notifyIntervals
                .addAll(getInstance().config.NOTIFY_INTERVALS);

        getInstance().hasRebootTask = true;
        getInstance().interrupt();
    }

    public static void cancelRebootTask() {
        TimeCheckerThread.getInstance().hasRebootTask = false;
    }

    public static boolean haveRebootTask() {
        return TimeCheckerThread.getInstance().hasRebootTask;
    }
}
