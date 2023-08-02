package me.hapyl.fight.game;

import me.hapyl.fight.game.entity.GameEntity;
import me.hapyl.fight.game.entity.GamePlayer;
import me.hapyl.spigotutils.module.chat.Gradient;
import me.hapyl.spigotutils.module.chat.gradient.Interpolators;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public record DeathMessage(String message, String damagerSuffix) {

    private static final String DAMAGER_PLACEHOLDER = "{damager}";

    public DeathMessage(String message, String damagerSuffix) {
        this.message = message;

        // If a message has placeholder, then damagerSuffix is not needed
        if (message.contains(DAMAGER_PLACEHOLDER)) {
            this.damagerSuffix = "";
        }
        else {
            // If the suffix has placeholder, then don't append it
            if (damagerSuffix.contains(DAMAGER_PLACEHOLDER)) {
                this.damagerSuffix = damagerSuffix;
            }
            else {
                this.damagerSuffix = damagerSuffix + " " + DAMAGER_PLACEHOLDER;
            }
        }
    }

    public String formatMessage(String damager) {
        return message.replace(DAMAGER_PLACEHOLDER, damager);
    }

    public String formatSuffix(String damager) {
        if (damagerSuffix.isBlank()) {
            return "";
        }

        return damagerSuffix.replace(DAMAGER_PLACEHOLDER, damager);
    }

    @Nonnull
    public String format(@Nonnull GamePlayer player, @Nullable GameEntity killer, double distance) {
        return format(player, getValidPronoun(killer), distance);
    }

    @Nonnull
    public String format(@Nonnull GamePlayer player, @Nonnull String killer, double distance) {
        final String message = message().replace(DAMAGER_PLACEHOLDER, killer);
        final String suffix = damagerSuffix().replace(DAMAGER_PLACEHOLDER, killer);
        final String longDistanceSuffix = distance >= 20.0d ? " (from %.1f meters away!)".formatted(distance) : "";

        String string;
        final String playerName = player.getName();

        if (killer.isBlank()) {
            string = "%s %s".formatted(playerName, message + longDistanceSuffix);
        }
        else {
            string = "%s %s %s".formatted(playerName, message, suffix + longDistanceSuffix);
        }

        return "&4☠ " + new Gradient(string)
                .rgb(
                        new Color(160, 0, 0),
                        new Color(255, 51, 51),
                        Interpolators.LINEAR
                );
    }

    private String getValidPronoun(@Nullable GameEntity gameEntity) {
        if (gameEntity == null) {
            return "";
        }

        final LivingEntity entity = gameEntity.getEntity();

        if (entity instanceof Projectile projectile) {
            final ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof LivingEntity livingShooter) {
                return livingShooter.getName() + "'s " + gameEntity.getNameUnformatted();
            }
        }

        return gameEntity.getNameUnformatted();
    }

    public static DeathMessage of(String message, String suffix) {
        return new DeathMessage(message, suffix);
    }

}