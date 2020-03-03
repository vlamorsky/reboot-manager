package me.vlamorsky.spongeplugin;

import me.vlamorsky.spongeplugin.command.reboot.*;
import me.vlamorsky.spongeplugin.command.reboot.start.Hours;
import me.vlamorsky.spongeplugin.command.reboot.start.Minutes;
import me.vlamorsky.spongeplugin.command.reboot.start.Seconds;
import me.vlamorsky.spongeplugin.command.vote.No;
import me.vlamorsky.spongeplugin.command.vote.Yes;
import me.vlamorsky.spongeplugin.config.Config;
import me.vlamorsky.spongeplugin.config.Permissions;
import me.vlamorsky.spongeplugin.util.TextCreator;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import me.vlamorsky.spongeplugin.task.TimeCheckerThread;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;


@Plugin(
        id = "reboot_manager",
        name = "Reboot manager",
        version = "1.0.3",
        description = "Reboot manager plugin for sponge")
public class RebootManager {
    private Logger logger;
    private Game game;

    private TextCreator textCreator;
    private Config config;
    private Path configPath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;

    private static RebootManager instance = null;

    @Inject
    public RebootManager(Game game,
                         Logger logger_,
                         @DefaultConfig(sharedRoot = false) Path configPath,
                         @DefaultConfig(sharedRoot = false) ConfigurationLoader<CommentedConfigurationNode> configLoader) {
        RebootManager.instance = this;
        this.game = game;
        logger = logger_;
        this.configPath = configPath;
        this.configLoader = configLoader;

    }

    @Listener
    public void preInit(GamePreInitializationEvent event) {
        loadConfig();
        textCreator = new TextCreator(config);
    }

    @Listener
    public void init(GameInitializationEvent event) {
        registerCommand();
    }

    @Listener
    public void postInit(GamePostInitializationEvent event) {
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        initIntervalRebooter();
    }

    @Listener
    public void onServerStop(GameStoppedEvent event) {
        saveConfig();
    }

    private void loadConfig() {
        try {
            config = new Config(configPath, configLoader);
        } catch (IOException | ObjectMappingException e) {
            logger.warn("Failed to load config!");
        }
    }

    private void saveConfig() {
        try {
            config.save();
        } catch (IOException e) {
            logger.warn("Failed to save config!");
        }
    }

    private void registerCommand() {
        CommandSpec help = CommandSpec.builder()
                .description(Text.of("Help description"))
                .executor(new Help())
                .build();

        CommandSpec vote = CommandSpec.builder()
                .description(Text.of("Vote description"))
                .permission(Permissions.COMMAND_VOTE)
                .executor(new Vote())
                .build();

        CommandSpec time = CommandSpec.builder()
                .description(Text.of("time cmd"))
                .permission(Permissions.COMMAND_TIME)
                .executor(new Time())
                .build();

        CommandSpec hours = CommandSpec.builder()
                .description(Text.of("Reboot base command"))
                .permission(Permissions.COMMAND_START)
                .executor(new Hours())
                .arguments(
                        GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("reason"))))
                .build();

        CommandSpec minutes = CommandSpec.builder()
                .description(Text.of("Reboot base command"))
                .permission(Permissions.COMMAND_START)
                .executor(new Minutes())
                .arguments(
                        GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("reason"))))
                .build();

        CommandSpec seconds = CommandSpec.builder()
                .description(Text.of("Reboot base command"))
                .permission(Permissions.COMMAND_START)
                .executor(new Seconds())
                .arguments(
                        GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("reason"))))
                .build();

        CommandSpec start = CommandSpec.builder()
                .description(Text.of("Reboot base command"))
                .permission(Permissions.COMMAND_START)
                .arguments(GenericArguments.integer(Text.of("time")))
                .child(hours, "h")
                .child(minutes, "m")
                .child(seconds, "s")
                .build();

        CommandSpec cancel = CommandSpec.builder()
                .description(Text.of("Reboot base command"))
                .permission(Permissions.COMMAND_CANCEL)
                .executor(new Cancel())
                .build();

        CommandSpec rebootMain = CommandSpec.builder()
                .description(Text.of("restart base command"))
                .child(help, "help")
                .child(vote, "vote")
                .child(time, "time")
                .child(start, "start")
                .child(cancel, "cancel")
                .build();

        game.getCommandManager().register(this, rebootMain, "reboot");

        CommandSpec yes = CommandSpec.builder()
                .description(Text.of("vote yes"))
                .permission(Permissions.COMMAND_VOTE)
                .executor(new Yes())
                .build();

        CommandSpec no = CommandSpec.builder()
                .description(Text.of("vote no"))
                .permission(Permissions.COMMAND_VOTE)
                .executor(new No())
                .build();

        CommandSpec voteMain = CommandSpec.builder()
                .description(Text.of("vote yes|no"))
                .permission(Permissions.COMMAND_VOTE)
                .child(yes, "yes")
                .child(no, "no")
                .build();

        game.getCommandManager().register(this, voteMain, "vote");
    }

    public void stopServer(String message) {
        Task.builder()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        logger.info("Restarting...");
                        try {
                            Sponge.getServer().getBroadcastChannel().send(textCreator.fromLegacy(" &7Сервер перезагружается&8..."));
                            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "save-all");
                            Sponge.getServer().shutdown(textCreator.fromLegacy("&7Сервер перезагружается&8:\n" + message));
                        } catch (Exception e) {
                            logger.info("Something went wrong while saving & stopping!");
                            logger.warn("Exception: " + e);
                        }
                    }
                })
                .submit(this);
    }

    private void initIntervalRebooter() {
        new TimeCheckerThread().start();
    }

    public Game getGame() {
        return game;
    }

    public static RebootManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("Reboot manager plugin is not initialized!");
        }
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public Config getConfig() {
        return config;
    }

    public TextCreator getTextCreator() {
        return textCreator;
    }
}
