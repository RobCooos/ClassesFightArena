package me.hapyl.fight.game.heroes.archive.nightmare;

import me.hapyl.fight.CF;
import me.hapyl.fight.game.effect.GameEffectType;
import me.hapyl.fight.game.entity.GamePlayer;
import me.hapyl.fight.game.heroes.Archetype;
import me.hapyl.fight.game.heroes.Hero;
import me.hapyl.fight.game.heroes.equipment.Equipment;
import me.hapyl.fight.game.talents.Talent;
import me.hapyl.fight.game.talents.Talents;
import me.hapyl.fight.game.talents.UltimateTalent;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.game.weapons.Weapon;
import me.hapyl.fight.util.collection.player.PlayerMap;
import me.hapyl.spigotutils.module.player.PlayerLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;

public class Nightmare extends Hero {

    private final PlayerMap<OmenDebuff> omenDebuffMap = PlayerMap.newMap();

    public Nightmare() {
        super("Nightmare");

        setArchetype(Archetype.DAMAGE);

        setDescription("A spirit from the worst nightmares, blinds enemies and strikes from behind!");
        setItem("79c55e0e4af71824e8da68cde87de717b214f92e9949c4b16da22b357f97b1fc");

        setWeapon(new Weapon(Material.NETHERITE_SWORD)
                .setName("Omen")
                .setDescription("A sword that is capable of splitting dreams in half.")
                .setDamage(7.0d));

        final Equipment equipment = getEquipment();
        equipment.setChestPlate(50, 0, 153);
        equipment.setLeggings(40, 0, 153);
        equipment.setBoots(30, 0, 153);

        setUltimate(new UltimateTalent(
                "Your Worst Nightmare",
                "Applies the &e&lParanoia &7effect to all alive opponents for {duration}.",
                55
        ).setDuration(240)
                .setCooldownSec(30)
                .setItem(Material.BLACK_DYE)
                .setSound(Sound.ENTITY_WITCH_CELEBRATE, 0.0f));

    }

    @Override
    public void onStart(@Nonnull GamePlayer player) {
        new GameTask() {
            @Override
            public void run() {
            }
        }.runTaskTimer(0, 1);
    }

    // Moved light level test in runnable
    @Override
    public void onStart() {
        new GameTask() {
            @Override
            public void run() {
               CF.getPlayers().forEach(player -> {
                    if (validatePlayer(player.getPlayer())) {
                        final Location location = player.getPlayer().getLocation();
                        if (location.getBlock().getLightLevel() <= 7) {
                            PlayerLib.spawnParticle(location, Particle.LAVA, 2, 0.15d, 0.15d, 0.15d, 0);
                            PlayerLib.addEffect(player.getPlayer(), PotionEffectType.SPEED, 30, 1);
                            PlayerLib.addEffect(player.getPlayer(), PotionEffectType.INCREASE_DAMAGE, 30, 0);
                        }
                    }
                });
            }
        }.runTaskTimer(0, 20);
    }

    @Override
    public void useUltimate(@Nonnull GamePlayer player) {
        CF.getAlivePlayers().forEach(alive -> {
            if (alive.compare(player)) {
                return;
            }
            alive.addEffect(GameEffectType.PARANOIA, getUltimateDuration(), true);
        });
    }

    @Override
    public Talent getFirstTalent() {
        return Talents.PARANOIA.getTalent();
    }

    @Override
    public Talent getSecondTalent() {
        return Talents.SHADOW_SHIFT.getTalent();
    }

    @Override
    public Talent getPassiveTalent() {
        return Talents.IN_THE_SHADOWS.getTalent();
    }
}
