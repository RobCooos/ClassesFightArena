package me.hapyl.fight.game.heroes.storage.extra;

import com.google.common.collect.Lists;
import me.hapyl.fight.game.GamePlayer;
import me.hapyl.fight.game.heroes.Heroes;
import me.hapyl.fight.game.heroes.storage.Taker;
import me.hapyl.fight.game.talents.storage.taker.SpiritualBonesPassive;
import me.hapyl.fight.util.Nulls;
import me.hapyl.fight.util.Utils;
import me.hapyl.spigotutils.module.entity.Entities;
import me.hapyl.spigotutils.module.inventory.ItemBuilder;
import me.hapyl.spigotutils.module.player.PlayerLib;
import me.hapyl.spigotutils.module.reflect.Ticking;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;

import java.util.LinkedList;

public class SpiritualBones implements Ticking {

    private final Player player;
    private int bones;

    private float theta = 0.0f;
    private float yaw = 0.0f;

    private final LinkedList<ArmorStand> armorStands;

    public SpiritualBones(Player player) {
        this.player = player;
        this.bones = 0;
        this.armorStands = Lists.newLinkedList();
    }

    public Player getPlayer() {
        return player;
    }

    public int getBones() {
        return bones;
    }

    public void add(int amount) {
        add(amount, true);
    }

    public void add(int amount, boolean playFx) {
        bones = Math.min(bones + amount, talent().MAX_BONES);
        createBoneEntity(amount);

        if (playFx) {
            PlayerLib.playSound(player, Sound.ENTITY_SKELETON_AMBIENT, 0.0f);
        }
    }

    public void remove(int amount) {
        this.bones = Math.max(this.bones - amount, 0);
        Nulls.runIfNotNull(armorStands.pollLast(), entity -> {
            PlayerLib.spawnParticle(entity.getLocation().add(0.0d, 1.25d, 0.0d), Particle.EXPLOSION_NORMAL, 5, 0.2d, 0.2d, 0.2d, 0.015f);
            entity.remove();
        });
    }

    public void clearArmorStands() {
        Utils.clearCollection(armorStands);
        theta = 0.0f;
        yaw = 0.0f;
    }

    public void reset() {
        bones = 0;
    }

    @Override
    public void tick() {
        if (bones == 0 || !GamePlayer.getPlayer(player).isAlive() || armorStands.isEmpty()) {
            return;
        }

        // Fly bones
        final float offset = (float) ((Math.PI * 2) / armorStands.size());
        final Location location = player.getLocation();

        // Move a little lower to not disturb the vision
        location.subtract(0.0d, 0.3d, 0.0d);

        location.setYaw(yaw += 3.0f);
        location.setPitch(0.0f);

        int pos = 1;

        for (ArmorStand entity : armorStands) {
            final double x = Math.sin(theta + (offset * pos));
            final double z = Math.cos(theta + (offset * pos));
            final double y = -0.25d;

            location.add(x, y, z);
            location.setYaw(location.getYaw() + 1.0f);
            entity.teleport(location);
            location.subtract(x, y, z); // this is not needed?
            ++pos;
        }

        theta += 0.1f;
        if (theta > (Math.PI * 2)) {
            theta = 0;
        }
    }

    public double getDamageMultiplier() {
        return (talent().DAMAGE_AMPLIFIER_PER_BONE * bones);
    }

    public double getDamageReduction() {
        return (talent().DAMAGE_REDUCE_PER_BONE * bones);
    }

    public double getHealing() {
        return (talent().HEALING_PER_BONE * bones);
    }

    public void createBoneEntity(int amount) {
        for (int i = 0; i < amount; i++) {
            if (armorStands.size() >= talent().MAX_BONES) {
                return;
            }

            armorStands.offerLast(Entities.ARMOR_STAND_MARKER.spawn(player.getLocation(), self -> {
                self.setInvisible(true);
                self.setSilent(true);
                self.setHeadPose(new EulerAngle(0.0d, 0.0d, Math.toRadians(90.0d)));

                Utils.setEquipment(self, equipment -> equipment.setHelmet(ItemBuilder.of(Material.BONE).asIcon()));
            }));
        }
    }

    private SpiritualBonesPassive talent() {
        return Heroes.TAKER.getHero(Taker.class).getPassiveTalent();
    }

}
