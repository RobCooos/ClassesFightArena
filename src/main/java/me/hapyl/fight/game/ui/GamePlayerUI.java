package me.hapyl.fight.game.ui;

import me.hapyl.fight.database.Database;
import me.hapyl.fight.game.*;
import me.hapyl.fight.game.effect.GameEffect;
import me.hapyl.fight.game.gamemode.Modes;
import me.hapyl.fight.game.gamemode.modes.Deathmatch;
import me.hapyl.fight.game.profile.PlayerProfile;
import me.hapyl.fight.game.setting.Setting;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.game.task.ShutdownAction;
import me.hapyl.fight.game.team.GameTeam;
import me.hapyl.fight.game.trial.Trial;
import me.hapyl.spigotutils.EternaPlugin;
import me.hapyl.spigotutils.module.chat.Chat;
import me.hapyl.spigotutils.module.math.nn.IntInt;
import me.hapyl.spigotutils.module.player.song.Song;
import me.hapyl.spigotutils.module.player.song.SongPlayer;
import me.hapyl.spigotutils.module.scoreboard.Scoreboarder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Map;

// this controls all UI based elements such as scoreboard, tab-list and actionbar (while in game)
public class GamePlayerUI {

    private final PlayerProfile profile;
    private final Player player;
    private final Scoreboarder builder;
    private final UIFormat format = UIFormat.DEFAULT;

    public GamePlayerUI(PlayerProfile profile) {
        this.profile = profile;
        this.player = profile.getPlayer();
        this.builder = new Scoreboarder("&e&lCLASSES FIGHT &c&lᴀʀᴇɴᴀ");
        this.updateScoreboard();

        new GameTask() {
            @Override
            public void run() {
                if (player == null || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                // update player list and scoreboard
                final String[] headerFooter = formatHeaderFooter();
                player.setPlayerListHeaderFooter(Chat.format(headerFooter[0]), Chat.format(headerFooter[1]));
                player.setPlayerListName(profile.getDisplay().getDisplayNameTab());

                animateScoreboard();
                updateScoreboard();

                if (Setting.SPECTATE.isEnabled(player)) {
                    Chat.sendActionbar(player, "&aYou will spectate when the game starts.");
                }

                final GamePlayer gamePlayer = profile.getGamePlayer();

                if (gamePlayer != null) {
                    sendInGameUI();
                }
            }
        }.runTaskTimer(0, 5).setShutdownAction(ShutdownAction.IGNORE);

    }

    private void animateScoreboard() {

    }

    public void sendInGameUI() {
        final GamePlayer gamePlayer = profile.getGamePlayer();
        if (gamePlayer == null) {
            return;
        }

        // Send UI information
        if (gamePlayer.isAlive() && !gamePlayer.isSpectator()) {
            Chat.sendActionbar(player, format.format(gamePlayer));
        }
    }

    public void updateScoreboard() {
        final Database database = profile.getDatabase();
        final Manager current = Manager.current();

        this.builder.getLines().clear();
        this.builder.addLines("");
        //        this.builder.addLines("", "Welcome %s to the".formatted(this.player.getName()), "&lClasses Fight &fArena!", "");

        if (current.isGameInProgress()) {
            final AbstractGameInstance game = current.getCurrentGame();
            final AbstractGamePlayer gamePlayer = GamePlayer.getPlayer(this.player);
            final Modes mode = game.getCurrentMode();

            // Have to reduce this so everything fits
            if (!gamePlayer.isAlive() && !gamePlayer.isRespawning()) {
                this.builder.addLines("&6&lSpectator: &f%s".formatted(getTimeLeftString(game)));
                for (final GamePlayer alive : game.getPlayers().values()) {
                    this.builder.addLine(
                            " &6%s &e%s %s &c%s ❤",
                            alive.getHero().getName(),
                            alive.getPlayer().getName(),
                            UIFormat.DIV,
                            alive.getHealth()
                    );
                }
            }

            else {
                // Default Game Lines
                this.builder.addLines(
                        "&6&lGame: &8" + game.hexCode(),
                        " &e&lMap: &f%s".formatted(current.getCurrentMap().getMap().getName()),
                        " &e&lTime Left: &f%s".formatted(getTimeLeftString(game)),
                        " &e&lStatus: &f%s".formatted(gamePlayer.getStatusString())
                );

                // Death match
                if (mode == Modes.DEATH_MATCH && gamePlayer.getStats() != null) {
                    final int limit = 5;
                    final Map<GamePlayer, Long> topKills = ((Deathmatch) game.getMode()).getTopKills(game, limit);
                    this.builder.addLines(
                            "",
                            "&6&lDeathmatch: &f(&b🗡 &l%s&f)".formatted(gamePlayer.getStats()
                                    .getValue(StatContainer.Type.KILLS))
                    );

                    final IntInt i = new IntInt(1);
                    topKills.forEach((pla, val) -> {
                        builder.addLines(" &e#&l%s &f%s &b🗡 &l%s".formatted(
                                i.get(),
                                pla.getPlayer().getName(),
                                val + ((pla.compare(player) ? " &a←&l YOU" : ""))
                        ));
                        i.increment();
                    });

                    for (int j = i.get(); j <= limit; j++) {
                        builder.addLines(" &e...");
                    }
                }
            }

        }
        // Trial
        else if (Manager.current().isTrialExistsAndIsOwner(player)) {
            final Trial trial = Manager.current().getTrial();
            this.builder.addLines(
                    "&6&lTrial:",
                    " &e&lTime Left: &f%s".formatted(new SimpleDateFormat("mm:ss").format(trial.getTimeLeft())),
                    " &e&lHero: &f%s &cTrial".formatted(trial.getHeroes().getHero().getName())
            );
        }
        else {
            this.builder.addLines(
                    "&6&lLobby:",
                    " &e&lMap: &f%s".formatted(current.getCurrentMap().getMap().getName()),
                    " &e&lMode: &f%s".formatted(current.getCurrentMode().getMode().getName()),
                    " &e&lCoins: &f%s".formatted(database.getCurrency().getCoinsString()),
                    String.format(
                            " &e&lHero: &f%s",
                            Setting.RANDOM_HERO.isEnabled(player) ? "Random" : profile.getSelectedHero().getName()
                    )
            );
        }

        this.builder.addLine("");
        this.builder.updateLines();
        this.builder.addPlayer(player);
    }

    private String getTimeLeftString(AbstractGameInstance game) {
        return Manager.current().isDebug() ? "&4&lDEBUG" : new SimpleDateFormat("mm:ss").format(game.getTimeLeftRaw());
    }

    private StringBuilder buildGameFooter() {
        final GameTeam team = GameTeam.getPlayerTeam(player);
        final StringBuilder builder = new StringBuilder();

        // Display teammate information:
        if (team != null) {
            builder.append("\n&e&lTeammates:\n");
            if (team.getPlayers().size() == 1) {
                builder.append("&8None!");
            }
            else {
                int i = 0;
                for (GamePlayer teammate : team.getPlayers()) {
                    final boolean usingUltimate = teammate.getHero().isUsingUltimate(teammate.getPlayer());

                    if (i != 0) {
                        builder.append("\n");
                    }

                    builder.append("&a%s &7⁑ &c&l%s &c❤  &b%s".formatted(
                            teammate.getName(),
                            teammate.getHealthFormatted(),
                            usingUltimate ? "&b&lIN USE" : teammate.isUltimateReady() ? "&b&lREADY" : ("&b%s/%s &l※".formatted(
                                    teammate.getUltPoints(),
                                    teammate.getUltPointsNeeded()
                            ))
                    ));

                    i++;
                }
            }
        }

        // Display active effects
        builder.append("\n\n&e&lActive Effects:\n");
        final GamePlayer gp = GamePlayer.getAlivePlayer(this.player);
        if (gp == null || gp.getActiveEffects().isEmpty()) {
            builder.append("&8None!");
        }
        else {
            // {Positive}{Name} - {Time}
            final IntInt i = new IntInt(0);
            gp.getActiveEffects().forEach((type, active) -> {
                final GameEffect gameEffect = type.getGameEffect();
                builder.append(gameEffect.isPositive() ? "&a" : "&c");
                builder.append(gameEffect.getName());
                builder.append(" &f- ");
                builder.append(new SimpleDateFormat("mm:ss").format(active.getRemainingTicks() * 50));
                builder.append(" ");

                i.increment();
                if (i.get() >= 2) {
                    builder.append("\n");
                    i.set(0);
                }
            });
        }

        return builder;
    }

    private String[] formatHeaderFooter() {
        final StringBuilder footer = new StringBuilder();

        if (Manager.current().isGameInProgress()) {
            footer.append(buildGameFooter());
        }

        // Display NBS player if playing a song
        final SongPlayer songPlayer = EternaPlugin.getPlugin().getSongPlayer();
        if (songPlayer.getCurrentSong() != null) {
            final Song song = songPlayer.getCurrentSong();
            final StringBuilder builder = new StringBuilder();
            final int frame = (int) (songPlayer.getCurrentFrame() * 30 / songPlayer.getMaxFrame());

            for (int i = 0; i < 30; i++) {
                builder.append(i < frame ? ChatColor.DARK_AQUA : ChatColor.DARK_GRAY);
                builder.append(UIFormat.DIV);
            }

            footer.append("\n\n&e&lSong Player:\n");
            footer.append("&f%s - %s\n&8%s".formatted(
                    song.getOriginalAuthor(),
                    song.getName(),
                    songPlayer.isPaused() ? "&e&lPAUSE" : builder.toString()
            ));
        }

        footer.append("\n");

        return new String[] {
                "\n&e&lCLASSES FIGHT\n&c&lᴀʀᴇɴᴀ\n\n&fTotal Players: &l" + Bukkit.getOnlinePlayers().size(), footer.toString()
        };
    }

    public Player getPlayer() {
        return player;
    }
}
