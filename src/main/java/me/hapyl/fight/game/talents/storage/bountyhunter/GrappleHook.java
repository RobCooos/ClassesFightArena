package me.hapyl.fight.game.talents.storage.bountyhunter;

import me.hapyl.fight.game.talents.Talents;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.util.Nulls;
import me.hapyl.fight.util.Utils;
import me.hapyl.spigotutils.module.chat.Chat;
import me.hapyl.spigotutils.module.entity.Entities;
import me.hapyl.spigotutils.module.entity.EntityUtils;
import me.hapyl.spigotutils.module.player.PlayerLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class GrappleHook {

    private final Player player;

    private LivingEntity hookedEntity;
    private Block hookedBlock;

    private final LivingEntity anchor;
    private final LivingEntity hook;

    private final GameTask syncTask;
    private GameTask extendTask;
    private GameTask retractTask;

    public GrappleHook(Player player) {
        this.player = player;

        this.anchor = createEntity();
        this.hook = createEntity();

        this.anchor.setLeashHolder(this.hook);

        this.syncTask = new GameTask() {
            @Override
            public void run() {
                // Sync
                anchor.teleport(player.getLocation());

                if (hookedEntity != null) {
                    hook.teleport(hookedEntity.getLocation());
                    hook.getWorld().spawnParticle(Particle.ITEM_CRACK, hook.getLocation(), 2, new ItemStack(Material.LEAD));
                }
            }
        }.runTaskTimer(0, 1);

        extendHook();
    }

    private boolean isHookToAnchorObstructed() {
        final Location hookLocation = hook.getLocation();
        final Location anchorLocation = anchor.getLocation();
        double distance = anchorLocation.distance(hookLocation);

        final double step = 0.5d;
        final Vector vector = anchorLocation.toVector().subtract(hookLocation.toVector()).normalize().multiply(step);

        for (double i = 0.0; i < distance; i += step) {
            // Don't check the first and the last block
            if (i == 0.0 || i >= (distance - step)) {
                continue;
            }

            hookLocation.add(vector);

            if (hookLocation.getBlock().getType().isOccluding()) {
                return true;
            }
        }

        return false;
    }

    private void extendHook() {
        final Location location = player.getEyeLocation();
        final Vector vector = location.getDirection().normalize();

        // Fx
        PlayerLib.playSound(player, Sound.ENTITY_BAT_TAKEOFF, 1.0f);
        PlayerLib.playSound(player, Sound.ENTITY_LEASH_KNOT_PLACE, 0.0f);

        this.extendTask = new GameTask() {

            private double distance = 0.0d;
            private final double speed = 0.1d;

            @Override
            public void run() {
                if (isHookBroken()) {
                    breakHook();
                    return;
                }

                if (hookedBlock != null || hookedEntity != null) {
                    return;
                }

                if (distance >= (talent().maxDistance * speed)) {
                    remove();

                    // Fx
                    Chat.sendMessage(player, "&6∞ &cYou didn't hook anything!");
                    return;
                }

                final double x = vector.getX() * distance;
                final double y = vector.getY() * distance;
                final double z = vector.getZ() * distance;

                location.add(x, y, z);

                // Hook detection
                final Block block = location.getBlock();

                if (!block.getType().isAir()) {
                    hookedBlock = block;
                    retractHook();
                    return;
                }

                final LivingEntity nearest = Utils.getNearestLivingEntity(location, 1.5d, player);

                if (nearest != null) {
                    hookedEntity = nearest;

                    if (hook instanceof Slime slime) {
                        slime.setSize(2);
                        slime.setMaxHealth(10.0d);
                        slime.setHealth(10.0d);
                        slime.setInvulnerable(false);
                    }

                    retractHook();

                    // Fx
                    Chat.sendMessage(player, "&6∞ &aYou hooked &e%s&a!", hookedEntity.getName());
                    return;
                }

                hook.teleport(location);
                distance += speed;
            }
        }.runTaskTimer(0, 1);
    }

    public boolean isHookBroken() {
        return hook.isDead() || !anchor.isLeashed();
    }

    private void breakHook() {
        remove();

        // Fx
        Chat.sendMessage(player, "&6∞ &cYour hook broke!");
        PlayerLib.playSound(player, Sound.ENTITY_LEASH_KNOT_BREAK, 0.0f);
    }

    private void retractHook() {
        extendTask.cancel();
        PlayerLib.playSound(player, Sound.ENTITY_LEASH_KNOT_PLACE, 0.0f);

        retractTask = new GameTask() {
            private final double step = 0.75d;

            @Override
            public void run() {
                if (isHookBroken()) {
                    breakHook();
                    return;
                }

                if (isHookToAnchorObstructed()) {
                    remove();
                    Chat.sendMessage(player, "&6∞ &cYour hook broke because of tear!");
                    PlayerLib.playSound(player, Sound.ENTITY_LEASH_KNOT_BREAK, 0.0f);
                    PlayerLib.playSound(player, Sound.ENTITY_LEASH_KNOT_BREAK, 2.0f);
                    return;
                }

                final Location playerLocation = player.getLocation();
                final Location location = hook.getLocation();
                final Vector vector = location.toVector().subtract(playerLocation.toVector()).normalize().multiply(step);

                playerLocation.add(vector);
                //player.teleport(playerLocation);

                player.setVelocity(vector);

                if (playerLocation.distanceSquared(location) <= 1d) {
                    remove();
                }

            }
        }.runTaskTimer(0, 1);
    }

    public void remove() {
        anchor.remove();
        hook.remove();

        Nulls.runIfNotNull(extendTask, GameTask::cancelIfActive);
        Nulls.runIfNotNull(syncTask, GameTask::cancelIfActive);
        Nulls.runIfNotNull(retractTask, GameTask::cancelIfActive);
    }

    private LivingEntity createEntity() {
        return Entities.SLIME.spawn(player.getLocation(), self -> {
            self.setSize(1);
            self.setGravity(false);
            self.setInvulnerable(true);
            self.setInvisible(true);
            self.setSilent(true);
            self.setAI(false);

            EntityUtils.setCollision(self, EntityUtils.Collision.DENY);
        });
    }

    private GrappleHookTalent talent() {
        return (GrappleHookTalent) Talents.GRAPPLE.getTalent();
    }

}
