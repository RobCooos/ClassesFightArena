package me.hapyl.fight.game.talents.storage.darkmage;

import me.hapyl.fight.game.GamePlayer;
import me.hapyl.fight.game.Response;
import me.hapyl.fight.game.effect.GameEffectType;
import me.hapyl.fight.game.heroes.HeroHandle;
import me.hapyl.fight.game.talents.Talent;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.util.Utils;
import me.hapyl.spigotutils.module.player.PlayerLib;
import me.hapyl.spigotutils.module.reflect.npc.HumanNPC;
import me.hapyl.spigotutils.module.reflect.npc.NPCPose;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import static org.bukkit.Sound.ENTITY_SQUID_SQUIRT;

public class ShadowClone extends Talent {
    public ShadowClone() {
        super(
                "Shadow Clone",
                "Creates a shadow clone of you at your current location and completely hides you. After a brief delay clone explodes, stunning and dealing damage to nearby players.",
                Material.NETHERITE_SCRAP
        );
        this.setCd(300);
    }

    @Override
    public Response execute(Player player) {
        if (HeroHandle.DARK_MAGE.isUsingUltimate(player)) {
            return Response.error("Unable to use while in ultimate form!");
        }

        final HumanNPC shadowClone = new HumanNPC(player.getLocation(), "", player.getName());

        GamePlayer.getPlayer(player).addEffect(GameEffectType.INVISIBILITY, 60);
        shadowClone.showAll();
        shadowClone.setEquipment(player.getEquipment());

        if (player.isSwimming()) {
            shadowClone.setPose(NPCPose.SWIMMING);
        }
        else if (player.isSneaking()) {
            shadowClone.setPose(NPCPose.CROUCHING);
        }

        new GameTask() {
            @Override
            public void run() {
                final Location location = shadowClone.getLocation();

                if (!HeroHandle.DARK_MAGE.isUsingUltimate(player)) {
                    Utils.showPlayer(player);
                }

                shadowClone.remove();

                PlayerLib.spawnParticle(location, Particle.SQUID_INK, 10, 0.1, 0.5, 0.1, 0.05f);
                PlayerLib.playSound(location, ENTITY_SQUID_SQUIRT, 0.25f);

                Utils.getPlayersInRange(location, 3.0d).forEach(target -> {
                    GamePlayer.damageEntity(target, 3.0d, player);
                    PlayerLib.addEffect(target, PotionEffectType.SLOW, 60, 2);
                    PlayerLib.addEffect(target, PotionEffectType.BLINDNESS, 60, 2);
                });

            }
        }.runTaskLater(60);

        return Response.OK;
    }
}
