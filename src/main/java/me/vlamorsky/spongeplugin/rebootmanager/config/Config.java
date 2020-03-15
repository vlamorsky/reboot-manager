package me.vlamorsky.spongeplugin.rebootmanager.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Config {

    private Path configPath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    public final boolean AUTORESTART_ENABLED;
    public final List<String> AUTORESTART_INTERVALS;
    public final String AUTORESTART_REASON;

    public final boolean VOTING_ENABLED;
    public final boolean VOTING_TITLE_ENABLED;
    public final boolean VOTING_SOUND_ENABLED;
    public final int VOTING_MIN_PLAYERS;
    public final int VOTING_DURATION;
    public final int VOTING_DELAY_AFTER_RESTART;
    public final int VOTING_DELAY_RE_VOTE;
    public final int VOTING_PERCENT;

    public final boolean SOUND_ENABLED;
    public final String SOUND_LAST_NOTIFICATION;
    public final String SOUND_BASIC_NOTIFICATION;

    public final String MESSAGE_PREFIX;
    public final List<Long> NOTIFY_INTERVALS;

    public Config(Path configPath, ConfigurationLoader<CommentedConfigurationNode> loader) throws IOException, ObjectMappingException {
        this.configPath = configPath;
        this.configLoader = loader;
        configNode = configLoader.load();

        AUTORESTART_ENABLED = check(
                configNode.getNode("autorestart", "enabled"),
                true,
                "[true/false]")
                .getBoolean();

        AUTORESTART_INTERVALS = check(
                configNode.getNode("autorestart", "realtime-intervals"),
                Arrays.asList("00", "06", "12", "18"),
                "[\"00\", \"01\", .. \"23\"] Set times for server restarts (24h format: hh), eg: \"13\" => restarts at 13:00, \"00\" => restarts at 00:00,")
                .getList(TypeToken.of(String.class));

        AUTORESTART_REASON = check(
                configNode.getNode("autorestart", "reason"),
                "Плановая перезагрузка",
                "(text message) Server restart reason")
                .getString();

        VOTING_ENABLED = check(
                configNode.getNode("voting", "enabled"),
                true,
                "[true/false]")
                .getBoolean();

        VOTING_TITLE_ENABLED = check(
                configNode.getNode("voting", "middle-screen-notify-enabled"),
                true,
                "[true/false]")
                .getBoolean();

        VOTING_SOUND_ENABLED = check(
                configNode.getNode("voting", "sound-notify-enabled"),
                true,
                "[true/false]")
                .getBoolean();

        VOTING_MIN_PLAYERS = check(
                configNode.getNode("voting", "min-players"),
                1,
                "Minimum number of players online to start voting.")
                .getInt();

        VOTING_PERCENT = check(
                configNode.getNode("voting", "yes-votes-percent"),
                60,
                "[0-100] % of online players to vote yes before a restart is triggered.")
                .getInt();

        VOTING_DURATION = check(
                configNode.getNode("voting", "duration"),
                30,
                "(seconds) Voting duration in seconds.")
                .getInt();

        VOTING_DELAY_AFTER_RESTART = check(
                configNode.getNode("voting", "delay-after-restart"),
                1,
                "(minutes) Voting start delay after server restart in minutes.")
                .getInt();

        VOTING_DELAY_RE_VOTE = check(
                configNode.getNode("voting", "delay-re-vote"),
                1,
                "(minutes) Time before another vote to restart can begin. (In minutes). Value: 0 - 60 (Recommended)")
                .getInt();

        SOUND_ENABLED = check(
                configNode.getNode("sound", "enabled"),
                true,
                "[true/false] Should a sound be played when a restart broadcast is sent?")
                .getBoolean();

        SOUND_BASIC_NOTIFICATION = check(
                configNode.getNode("sound", "notice-sound"),
                "entity.cat.ambient",
                "(sound type) The sound that should play for the notification. (Vanilla sounds can be found here: http://minecraft.gamepedia.com/Sounds.json)")
                .getString();

        SOUND_LAST_NOTIFICATION = check(
                configNode.getNode("sound", "last-notice-sound"),
                "entity.cat.hurt",
                "(sound type) The sound that should play for the last notification.")
                .getString();

        MESSAGE_PREFIX = check(
                configNode.getNode("messages", "prefix"),
                "&8[&6REBOOT&8]",
                "prefix configuration")
                .getString();

        NOTIFY_INTERVALS = check(
                configNode.getNode("notify", "intervals"),
                Arrays.asList(600L, 300L, 240L, 180L, 120L, 60L, 30L, 15L),
                "[SECONDS] intervals for notifying the time before restarting the server")
                .getList(TypeToken.of(Long.class));

        save();
    }

    public void save() throws IOException {
        configLoader.save(configNode);
    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue, String comment) {
        if (node.isVirtual()) {
            node.setValue(defaultValue).setComment(comment);
        }
        return node;
    }
}
