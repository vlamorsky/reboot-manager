package me.vlamorsky.spongeplugin.rebootmanager;

import com.google.inject.Inject;
import me.vlamorsky.spongeplugin.rebootmanager.command.reboot.*;
import me.vlamorsky.spongeplugin.rebootmanager.command.vote.No;
import me.vlamorsky.spongeplugin.rebootmanager.command.vote.Cancel;
import me.vlamorsky.spongeplugin.rebootmanager.command.vote.Yes;
import me.vlamorsky.spongeplugin.rebootmanager.config.Config;
import me.vlamorsky.spongeplugin.rebootmanager.config.Permissions;
import me.vlamorsky.spongeplugin.rebootmanager.task.TimeCheckerThread;
import me.vlamorsky.spongeplugin.rebootmanager.task.VoteThread;
import me.vlamorsky.spongeplugin.rebootmanager.util.TextCreator;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;


@Plugin(
        id = "%id%",
        name = "%name%",
        version = "%version%",
        description = "%description%")
public class RebootManager {
    private Logger logger;
    private Game game;

    private VoteThread voteThread;
    private TextCreator textCreator;
    private Config config;
    private Path configPath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;

    private static RebootManager instance = null;
    public final static String VERSION = "%version%";
    public final static String NAME = "%name%";

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
    }

    @Listener
    public void postInit(GamePostInitializationEvent event) {
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        initTasks();
        registerCommand();
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
        CommandExecutor voteExec = new Vote();

        CommandSpec voteYes = CommandSpec.builder()
                .description(Text.of("vote yes"))
                .permission(Permissions.COMMAND_VOTING)
                .executor(new Yes())
                .build();

        CommandSpec voteNo = CommandSpec.builder()
                .description(Text.of("vote no"))
                .permission(Permissions.COMMAND_VOTING)
                .executor(new No())
                .build();

        CommandSpec voteCancel = CommandSpec.builder()
                .description(Text.of("Vote cancel"))
                .permission(Permissions.COMMAND_VOTE_CANCEL)
                .executor(new Cancel())
                .build();

        CommandSpec vote = CommandSpec.builder()
                .description(Text.of("vote"))
                .permission(Permissions.COMMAND_VOTE)
                .child(voteYes, "yes")
                .child(voteNo, "no")
                .child(voteCancel, "cancel")
                .executor(new Vote())
                .build();

        CommandSpec help = CommandSpec.builder()
                .description(Text.of("help"))
                .executor(new Help())
                .build();

        CommandSpec time = CommandSpec.builder()
                .description(Text.of("time"))
                .permission(Permissions.COMMAND_TIME)
                .executor(new Time())
                .build();

        CommandSpec start = CommandSpec.builder()
                .description(Text.of("start new reboot task"))
                .permission(Permissions.COMMAND_START)
                .arguments(GenericArguments.string(Text.of("time")),
                        GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("reason"))))
                .executor(new Start())
                .build();

        CommandSpec cancel = CommandSpec.builder()
                .description(Text.of("cancel current reboot task"))
                .permission(Permissions.COMMAND_CANCEL)
                .executor(new me.vlamorsky.spongeplugin.rebootmanager.command.reboot.Cancel())
                .build();

        CommandSpec rebootMain = CommandSpec.builder()
                .description(Text.of("reboot"))
                .child(help, "help")
                .child(vote, "vote")
                .child(time, "time")
                .child(start, "start")
                .child(cancel, "cancel")
                .build();

        game.getCommandManager().register(this, rebootMain, "reboot");
    }

    public void stopServer(String message) {
        Task.builder()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        logger.info("Restarting...");
                        try {
                            Sponge.getServer().getBroadcastChannel().send(textCreator.fromLegacy(" &7Server rebooting&8..."));
                            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "save-all");
                            Sponge.getServer().shutdown(textCreator.fromLegacy("&7Server rebooting&8:\n" + message));
                        } catch (Exception e) {
                            logger.info("Something went wrong while saving & stopping!");
                            logger.warn("Exception: " + e);
                        }
                    }
                })
                .submit(this);
    }

    private void initTasks() {
        new TimeCheckerThread().start();

        voteThread = new VoteThread();
        voteThread.start();
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

    public VoteThread getVoteThread() {
        return voteThread;
    }

    public TextCreator getTextCreator() {
        return textCreator;
    }
}
