package me.hapyl.fight.game.talents.storage.darkmage;

import me.hapyl.fight.game.Response;
import me.hapyl.fight.game.heroes.Heroes;
import me.hapyl.fight.game.heroes.storage.extra.DarkMageSpell;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.util.Utils;
import me.hapyl.fight.util.displayfield.DisplayField;
import me.hapyl.spigotutils.module.math.Geometry;
import me.hapyl.spigotutils.module.math.geometry.Draw;
import me.hapyl.spigotutils.module.math.geometry.Quality;
import me.hapyl.spigotutils.module.player.PlayerLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;

import static org.bukkit.Sound.BLOCK_HONEY_BLOCK_SLIDE;

public class SlowingAura extends DarkMageTalent {

    @DisplayField(suffix = "blocks") private final short maxDistance = 20;
    @DisplayField private final double radius = 4.0d;

    private final int taskPeriod = 5;

    public SlowingAura() {
        super("Slowing Aura", "Creates a slowness pool at your target block that slows anyone in range.", Material.BONE_MEAL);

        setDuration(50);
        setCd(200);
    }

    @Nonnull
    @Override
    public DarkMageSpell.SpellButton first() {
        return DarkMageSpell.SpellButton.RIGHT;
    }

    @Nonnull
    @Override
    public DarkMageSpell.SpellButton second() {
        return DarkMageSpell.SpellButton.LEFT;
    }

    @Override
    public Response execute(Player player) {
        if (Heroes.DARK_MAGE.getHero().isUsingUltimate(player)) {
            return Response.error("Unable to use while in ultimate form!");
        }

        final Block targetBlock = player.getTargetBlockExact(maxDistance);

        if (targetBlock == null) {
            return Response.error("No valid block in sight!");
        }

        final Location location = targetBlock.getRelative(BlockFace.UP).getLocation();

        new GameTask() {
            private int tick = getDuration();

            @Override
            public void run() {
                if (tick <= 0) {
                    cancel();
                    return;
                }

                tick -= taskPeriod;

                Geometry.drawCircle(location, radius, Quality.LOW, new Draw(Particle.SPELL) {
                    @Override
                    public void draw(Location location) {
                        final World world = location.getWorld();
                        if (world != null) {
                            world.spawnParticle(
                                    this.getParticle(),
                                    location.getX(),
                                    location.getY(),
                                    location.getZ(),
                                    1, 0, 0, 0, null
                            );
                        }
                    }
                });

                PlayerLib.playSound(location, BLOCK_HONEY_BLOCK_SLIDE, 0.0f);
                Utils.getPlayersInRange(location, radius).forEach(entity -> {
                    PlayerLib.addEffect(entity, PotionEffectType.SLOW, 10, 3);
                });

            }
        }.runTaskTimer(0, taskPeriod);
        return Response.OK;
    }
}
