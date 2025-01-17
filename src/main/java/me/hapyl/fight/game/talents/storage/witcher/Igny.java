package me.hapyl.fight.game.talents.storage.witcher;

import me.hapyl.fight.game.EnumDamageCause;
import me.hapyl.fight.game.GamePlayer;
import me.hapyl.fight.game.Response;
import me.hapyl.fight.game.talents.Talent;
import me.hapyl.fight.util.Utils;
import me.hapyl.fight.util.displayfield.DisplayField;
import me.hapyl.spigotutils.module.player.PlayerLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Igny extends Talent {

    @DisplayField private final double maximumDistance = 4.0d;

    @DisplayField private final double damageClosest = 5.0d;
    @DisplayField private final int fireDurationClosest = 60;
    @DisplayField private final double damageMedium = 3.5d;
    @DisplayField private final int fireTicksMedium = 40;
    @DisplayField private final double damageFurther = 2.0d;
    @DisplayField private final int fireTicksFurther = 20;

    public Igny() {
        super("Igni");

        addDescription("Fires blazing spirits in front of you that deal damage and sets enemies on fire.");
        addDescription("Damage and burning duration falls off with distance.");

        setItem(Material.BLAZE_POWDER);
        setCdSec(10);
    }

    @Override
    public Response execute(Player player) {
        final Location location = player.getLocation();
        final Location targetLocation = location.add(player.getLocation().getDirection().multiply(3));

        Utils.getPlayersInRange(targetLocation, maximumDistance).forEach(target -> {
            if (target == player) {
                return;
            }

            final double distance = targetLocation.distance(target.getLocation());

            if (isBetween(distance, 0, 1)) {
                GamePlayer.damageEntity(target, damageClosest, player, EnumDamageCause.ENTITY_ATTACK);
                target.setFireTicks(fireDurationClosest);
            }
            else if (isBetween(distance, 1, 2.5)) {
                GamePlayer.damageEntity(target, damageMedium, player, EnumDamageCause.ENTITY_ATTACK);
                target.setFireTicks(fireTicksMedium);
            }
            else if (isBetween(distance, 2.5, 4.1d)) {
                GamePlayer.damageEntity(target, damageFurther, player, EnumDamageCause.ENTITY_ATTACK);
                target.setFireTicks(fireTicksFurther);
            }
        });

        // fx
        PlayerLib.spawnParticle(targetLocation, Particle.FLAME, 20, 2.0, 0.5, 2.0, 0.01f);
        PlayerLib.playSound(targetLocation, Sound.ITEM_FLINTANDSTEEL_USE, 0.0f);
        PlayerLib.playSound(targetLocation, Sound.ITEM_FIRECHARGE_USE, 0.0f);

        return Response.OK;
    }

    private boolean isBetween(double a, double min, double max) {
        return a >= min && a < max;
    }

}
