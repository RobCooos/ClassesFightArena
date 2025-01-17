package me.hapyl.fight.game.talents.storage.harbinger;

import com.google.common.collect.Sets;
import me.hapyl.fight.game.GamePlayer;
import me.hapyl.fight.game.Manager;
import me.hapyl.fight.game.heroes.Heroes;
import me.hapyl.fight.game.heroes.storage.Harbinger;
import me.hapyl.fight.game.talents.Talents;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.util.Utils;
import me.hapyl.spigotutils.module.locaiton.LocationHelper;
import me.hapyl.spigotutils.module.player.PlayerLib;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Set;

public class TidalWave {

    private final int period = 3;

    private final Player player;
    private final int duration;
    private final Set<Block> affectedBlocks;

    private final Location location;
    private final Vector vector;

    private final TidalWaveTalent talent;

    public TidalWave(Player player, int duration) {
        this.player = player;
        this.duration = duration;
        this.affectedBlocks = Sets.newHashSet();
        this.talent = Talents.TIDAL_WAVE.getTalent(TidalWaveTalent.class);

        this.location = player.getLocation();
        this.vector = location.getDirection().normalize().setY(0.0d).multiply(1.5d);

        launch();
    }

    private void launch() {
        location.setPitch(0.0f);
        location.add(vector);

        new GameTask() {
            private int tick = 0;

            @Override
            public void run() {
                if (!affectedBlocks.isEmpty()) {
                    Utils.clearCollection(affectedBlocks);
                }

                if (tick > duration) {
                    cancel();
                    return;
                }

                final Location right = LocationHelper.getToTheRight(location, talent.horizontalOffset);
                final Location left = LocationHelper.getToTheLeft(location, talent.horizontalOffset);

                createBlocks(location);
                createBlocks(right);
                createBlocks(left);

                location.add(vector);
                tick += period;

                // Fx
                if (tick % 10 == 0) {
                    PlayerLib.playSound(location, Sound.AMBIENT_UNDERWATER_ENTER, 0.0f);
                    PlayerLib.playSound(location, Sound.AMBIENT_UNDERWATER_EXIT, 0.0f);
                }
            }
        }.runTaskTimer(0, period);

        // Fx
        PlayerLib.playSound(location, Sound.AMBIENT_UNDERWATER_ENTER, 0.0f);
        PlayerLib.playSound(location, Sound.AMBIENT_UNDERWATER_EXIT, 0.0f);
    }

    private void createBlocks(Location location) {
        final World world = location.getWorld();
        final Vector pushVector = vector.clone().multiply(0.5d);

        // Skip if not passable
        if (!location.getBlock().getType().isAir()) {
            return;
        }

        // Anchor location to the ground
        while (true) {
            final Block block = location.getBlock().getRelative(BlockFace.DOWN);

            if (!block.getType().isAir() || world == null || location.getY() <= world.getMinHeight()) {
                break;
            }

            location.subtract(0.0d, 1.0d, 0.0d);
        }

        // Create blocks
        for (int i = 0; i < talent.height; i++) {
            final Block block = location.getBlock().getRelative(BlockFace.UP, i);

            if (!block.getType().isAir()) {
                continue;
            }

            // Push enemies
            Utils.getEntitiesInRange(location, 1.0d, entity -> Utils.isEntityValid(entity, player))
                    .forEach(entity -> {
                        entity.setVelocity(pushVector);
                        Heroes.HARBINGER.getHero(Harbinger.class).addRiptide(player, entity, talent.riptideDuration, false);
                    });

            affectedBlocks.add(block);

            for (GamePlayer player : Manager.current().getCurrentGame().getAlivePlayers()) {
                final Location blockLocation = block.getLocation();
                player.getPlayer().sendBlockChange(blockLocation, Material.WATER.createBlockData());

                // Fx
                final double offset = Utils.scaleParticleOffset(0.5d);
                PlayerLib.spawnParticle(blockLocation, Particle.WATER_SPLASH, 1, offset, offset, offset, 0.15f);
            }
        }
    }

}
