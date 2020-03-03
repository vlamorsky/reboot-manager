package me.vlamorsky.spongeplugin.command.reboot;

import me.vlamorsky.spongeplugin.task.TimeCheckerThread;
import me.vlamorsky.spongeplugin.util.TextCreator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import me.vlamorsky.spongeplugin.RebootManager;
import me.vlamorsky.spongeplugin.config.Config;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class Vote implements CommandExecutor {

    private static VoteThread voteThread;

    private Config config;

    public static VoteThread getVoteThread() {
        return voteThread;
    }

    public Vote() {
        config = RebootManager.getInstance().getConfig();
        voteThread = new VoteThread();
        voteThread.start();
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {

        if (!config.VOTING_ENABLED) {
            return CommandResult.empty();
        }

        if (!(source instanceof Player)) {
            return CommandResult.empty();
        }

        Player player = ((Player) source).getPlayer().get();

        if (voteThread.ableToStartVoting(player)) {

            if (RebootManager.getInstance().getGame().getServer()
                    .getOnlinePlayers().size() >= config.VOTING_MIN_PLAYERS) {

                voteThread.startVoting();

                Text votingMessage = Text.builder()
                        .append(Text.builder()
                                .append(TextCreator.fromLegacy("&8[&6REBOOT&8] &7Перезагрузить сервер&8?  "))
                                .build())
                        .append(Text.builder()
                                .append(TextCreator.fromLegacy("&8[&2Да&8]"))
                                .onClick(TextActions.runCommand("/vote yes"))
                                .build())
                        .append(Text.builder()
                                .append(Text.of("  "))
                                .build())
                        .append(Text.builder()
                                .append(TextCreator.fromLegacy("&8[&4Нет&8]"))
                                .onClick(TextActions.runCommand("/vote no"))
                                .build())
                        .append(Text.builder()
                                .append(TextCreator.fromLegacy(" &8<- &7кликабельно"))
                                .build())
                        .build();

                broadcastMessage(votingMessage);

            } else {

                player.sendMessage(TextCreator.fromLegacy(
                                "&8[&6REBOOT&8] &7Необходимо как минимум &3" +
                                config.VOTING_MIN_PLAYERS +
                                " &7игроков онлайн&8."));
            }

        }

        return CommandResult.success();
    }

    private void broadcastMessage(Text message) {
        Sponge.getServer().getBroadcastChannel().send(message);
    }

    public class VoteThread extends Thread {
        private final Scoreboard scoreboard;
        private final Objective objective;
        private int yesVotes;
        private int noVotes;
        private int timer;
        private Map<Player, String> playerVotes;
        private final LocalDateTime timeServerStart;
        private LocalDateTime timeLastVoting;
        private double votesPercent;

        private boolean votes;

        public boolean ableToStartVoting(Player player) {

            LocalDateTime timeNow = LocalDateTime.now();

            if (ChronoUnit.SECONDS.between(timeNow, TimeCheckerThread.getRestartDateTime()) <= config.VOTING_DURATION
                && !TimeCheckerThread.haveRebootTask()) {

                player.sendMessage(TextCreator.getMessageServerWillRestartSoon());

                return false;
            }

            if (ChronoUnit.SECONDS.between(timeServerStart, timeNow) < config.VOTING_DELAY_AFTER_RESTART * 60 &&
                    ChronoUnit.SECONDS.between(timeServerStart, timeNow) > 0) {

                LocalTime timeUntilNextVoting = LocalTime
                        .ofSecondOfDay(ChronoUnit.SECONDS.between(timeNow, timeServerStart.plusMinutes(config.VOTING_DELAY_AFTER_RESTART).plusSeconds(1L)));

                player.sendMessage(TextCreator.getMessageTimeUntilNextVoting(
                        timeUntilNextVoting.getHour(), timeUntilNextVoting.getMinute(), timeUntilNextVoting.getSecond()));


                System.out.println(ChronoUnit.SECONDS.between(timeServerStart, timeNow));
                return false;
            }

            if (timeLastVoting != null &&
                    ChronoUnit.SECONDS.between(timeLastVoting, timeNow) < config.VOTING_DELAY_RE_VOTE * 60 &&
                    ChronoUnit.SECONDS.between(timeLastVoting, timeNow) > 0) {

                LocalTime timeUntilNextVoting = LocalTime
                        .ofSecondOfDay(ChronoUnit.SECONDS.between(timeNow, timeLastVoting.plusMinutes(config.VOTING_DELAY_RE_VOTE).plusSeconds(1L)));

                player.sendMessage(TextCreator.getMessageTimeUntilNextVoting(
                        timeUntilNextVoting.getHour(), timeUntilNextVoting.getMinute(), timeUntilNextVoting.getSecond()));

                System.out.println(ChronoUnit.SECONDS.between(timeLastVoting, timeNow));

                return false;
            }

            if (votes) {

                player.sendMessage(TextCreator
                        .fromLegacy("&8[&6REBOOT&8] &7Голосование уже идет"));

                return false;
            }

            return true;
        }

        public void startVoting() {
            votes = true;
        }

        public void receiveVote(Player player, String vote) {
            if (!votes) {
                return;
            }

            if (!playerVotes.containsKey(player)) {
                playerVotes.put(player, vote);

                if ("yes".equals(vote)) {
                    yesVotes++;
                } else if ("no".equals(vote)) {
                    noVotes++;
                }

            } else {
                if (!vote.equals(playerVotes.get(player))) {

                    if ("yes".equals(vote)) {
                        noVotes--;
                        yesVotes++;
                    } else if ("no".equals(vote)) {
                        yesVotes--;
                        noVotes++;
                    }

                    playerVotes.put(player, vote);
                }
            }
        }

        public VoteThread() {
            scoreboard = Scoreboard.builder().build();
            objective = Objective.builder()
                    .name("voting_rmc")
                    .criterion(Criteria.DUMMY)
                    .build();
            scoreboard.addObjective(objective);
            playerVotes = new HashMap<>();
            timeServerStart = LocalDateTime.now();

            yesVotes = 0;
            noVotes = 0;
        }

        private void loadConfigs() {
            timer = config.VOTING_DURATION;

            if (config.VOTING_PERCENT >= 0 && config.VOTING_PERCENT <= 100) {
                votesPercent = config.VOTING_PERCENT / 100d;
            } else {
                votesPercent = 0.6;
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                while (votes) {
                    if (timer <= 0) {
                        clearScoreBoard();
                        checkVotesAndRestart();
                        resetValues();
                        votes = false;
                    } else {
                        updateScoreBoard();
                        timer--;
                    }

                    try {
                        Thread.currentThread().sleep(990);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    resetValues();
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void updateScoreBoard() {
             objective.setDisplayName(
                     TextCreator.fromLegacy("&6Перезагрузить сервер? &3" + timer)
             );

            objective.getOrCreateScore(TextCreator.fromLegacy(
                    "&l&2               Да"
            )).setScore(yesVotes);

            objective.getOrCreateScore(TextCreator.fromLegacy(
                    "&l&4              Нет"
            )).setScore(noVotes);

            scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);


            RebootManager.getInstance().getGame().getServer()
                    .getOnlinePlayers().forEach((player) -> player.setScoreboard(scoreboard));
        }

        private void clearScoreBoard() {
            RebootManager.getInstance().getGame().getServer()
                    .getOnlinePlayers().forEach((player) -> player.getScoreboard().clearSlot(DisplaySlots.SIDEBAR));
        }

        private void resetValues() {
            yesVotes = 0;
            noVotes = 0;
            timer = config.VOTING_DURATION;
            playerVotes.clear();
        }

        private void checkVotesAndRestart() {

            RebootManager.getInstance()
                    .getGame()
                    .getServer()
                    .getBroadcastChannel()
                    .send(TextCreator.fromLegacy(
                                    "&8[&6REBOOT&8] &7Голосование завершено" +
                                    "  &2за &a" +
                                    yesVotes +
                                    "  &4против &c" +
                                    noVotes));

            /*if (yesVotes > noVotes) {
                TimeCheckerThread.setRestartDateTime(LocalDateTime.now().plusSeconds(31L), "Перезагрузка по результатам голосования");
            } else {

                RebootManager.getInstance()
                        .getGame()
                        .getServer()
                        .getBroadcastChannel()
                        .send(TextCreator.fromLegacy("&8[&6REBOOT&8] &7Перезагрузке не быть"));

            }*/

            if (votesPercent <= (double)yesVotes / Sponge.getServer().getOnlinePlayers().size() && yesVotes > noVotes) {
                TimeCheckerThread.setRestartDateTime(LocalDateTime.now().plusSeconds(31L), "Перезагрузка по результатам голосования");
            } else {
                RebootManager.getInstance()
                        .getGame()
                        .getServer()
                        .getBroadcastChannel()
                        .send(TextCreator.fromLegacy("&8[&6REBOOT&8] &7Недостаточно голосов для перезагрузки сервера&8."));
            }

            timeLastVoting = LocalDateTime.now();
        }
    }
}
