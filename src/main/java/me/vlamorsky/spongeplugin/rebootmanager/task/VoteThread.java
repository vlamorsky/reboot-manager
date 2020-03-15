package me.vlamorsky.spongeplugin.rebootmanager.task;

import me.vlamorsky.spongeplugin.rebootmanager.RebootManager;
import me.vlamorsky.spongeplugin.rebootmanager.config.Config;
import me.vlamorsky.spongeplugin.rebootmanager.config.Permissions;
import me.vlamorsky.spongeplugin.rebootmanager.util.TextCreator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class VoteThread extends Thread {

    private Config config;
    private TextCreator textCreator;

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

    public boolean ableToStartVoting(CommandSource source) {

        LocalDateTime timeNow = LocalDateTime.now();

        if (votes) {
            source.sendMessage(textCreator.getMessageAlreadyVoting());
            return false;
        }

        if (ChronoUnit.SECONDS.between(timeNow, TimeCheckerThread.getRestartDateTime()) <= config.VOTING_DURATION
                && TimeCheckerThread.haveRebootTask()) {

            source.sendMessage(textCreator.getMessageServerWillRestartSoon());

            return false;
        }

        if (source.hasPermission(Permissions.COMMAND_EXEMPT)) {
            return true;
        }

        if (RebootManager.getInstance().getGame().getServer()
                .getOnlinePlayers().size() < config.VOTING_MIN_PLAYERS) {

            source.sendMessage(textCreator.getMessageNotEnoughPlayers(config.VOTING_MIN_PLAYERS));

            return false;
        }

        if (ChronoUnit.SECONDS.between(timeServerStart, timeNow) < config.VOTING_DELAY_AFTER_RESTART * 60 &&
                ChronoUnit.SECONDS.between(timeServerStart, timeNow) > 0) {

            LocalTime timeUntilNextVoting = LocalTime
                    .ofSecondOfDay(ChronoUnit.SECONDS.between(timeNow, timeServerStart.plusMinutes(config.VOTING_DELAY_AFTER_RESTART).plusSeconds(1L)));

            source.sendMessage(textCreator.getMessageTimeUntilNextVoting(
                    timeUntilNextVoting.getHour(), timeUntilNextVoting.getMinute(), timeUntilNextVoting.getSecond()));


            return false;
        }

        if (timeLastVoting != null &&
                ChronoUnit.SECONDS.between(timeLastVoting, timeNow) < config.VOTING_DELAY_RE_VOTE * 60 &&
                ChronoUnit.SECONDS.between(timeLastVoting, timeNow) > 0) {

            LocalTime timeUntilNextVoting = LocalTime
                    .ofSecondOfDay(ChronoUnit.SECONDS.between(timeNow, timeLastVoting.plusMinutes(config.VOTING_DELAY_RE_VOTE).plusSeconds(1L)));

            source.sendMessage(textCreator.getMessageTimeUntilNextVoting(
                    timeUntilNextVoting.getHour(), timeUntilNextVoting.getMinute(), timeUntilNextVoting.getSecond()));

            return false;
        }

        return true;
    }

    public void startVoting() {
        scoreboard.removeObjective(objective);
        scoreboard.addObjective(objective);
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
        config = RebootManager.getInstance().getConfig();
        textCreator = RebootManager.getInstance().getTextCreator();

        scoreboard = Sponge.getServer().getServerScoreboard().get();
        objective = Objective.builder()
                .name("voting_rmc")
                .criterion(Criteria.DUMMY)
                .build();
        scoreboard.removeObjective(objective);
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
                textCreator.fromLegacy("&7Reboot Voting &3" + timer)
        );

        objective.getOrCreateScore(textCreator.fromLegacy(
                "&l&2               Yes"
        )).setScore(yesVotes);

        objective.getOrCreateScore(textCreator.fromLegacy(
                "&l&4                No"
        )).setScore(noVotes);

        scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);

            /*Task.builder()
                    .execute(() -> {
                        Sponge.getServer()
                                .getOnlinePlayers().forEach(player -> {
                            player.getScoreboard().updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
                        });
                    })
                    .async()
                    .submit(RebootManager.getInstance());*/
    }

    private void clearScoreBoard() {
            /*Sponge.getGame().getServer()
                    .getOnlinePlayers().forEach((player) -> player.getScoreboard().clearSlot(DisplaySlots.SIDEBAR));*/
        scoreboard.removeObjective(objective);
        scoreboard.clearSlot(DisplaySlots.SIDEBAR);
    }

    private void resetValues() {
        yesVotes = 0;
        noVotes = 0;
        timer = config.VOTING_DURATION;
        playerVotes.clear();
    }

    private void checkVotesAndRestart() {

        Sponge.getGame()
                .getServer()
                .getBroadcastChannel()
                .send(textCreator.getMessageVotingCompleted(yesVotes, noVotes));

        if (votesPercent <= (double)yesVotes / Sponge.getServer().getOnlinePlayers().size() && yesVotes > noVotes) {
            TimeCheckerThread.setRestartDateTime(LocalDateTime.now().plusSeconds(31L), "Rebooting based on vote results");
        } else {
            Sponge.getGame()
                    .getServer()
                    .getBroadcastChannel()
                    .send(textCreator.getMessageNotEnoughVotes());
        }

        timeLastVoting = LocalDateTime.now();
    }

    public boolean isVotes() {
        return votes;
    }

    public void cancelVote() {
        votes = false;
        clearScoreBoard();
        resetValues();

        Sponge.getGame()
                .getServer()
                .getBroadcastChannel()
                .send(textCreator.getMessageVoteInterrupted());
    }
}
