package me.hapyl.fight.game.talents.storage.km;

import me.hapyl.fight.game.EnumDamageCause;
import me.hapyl.fight.game.Response;
import me.hapyl.fight.game.talents.Talent;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.util.Nulls;
import me.hapyl.fight.util.Utils;
import me.hapyl.spigotutils.module.player.PlayerLib;
import me.hapyl.spigotutils.module.util.BukkitUtils;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;

public class ShellGrande extends Talent {

    private final ItemStack fireworkStarDef = new ItemStack(Material.FIREWORK_STAR);
    private final ItemStack fireworkStarRed;
    private final int explosionDelay = 40;

    public ShellGrande() {
        super("Grenade");
        this.addDescription(
                "Throw a grenade that bounce off walls. Explodes after &b%ss &7in medium AoE dealing significant damage.____&e&lSNEAK &7while throwing to toss closer.".formatted(
                        BukkitUtils.roundTick(explosionDelay)));
        this.setItem(Material.FIREWORK_STAR);
        this.setCdSec(11);

        fireworkStarRed = new ItemStack(Material.FIREWORK_STAR);
        Nulls.runIfNotNull((FireworkEffectMeta) fireworkStarRed.getItemMeta(), meta -> {
            meta.setEffect(FireworkEffect.builder().withColor(Color.RED).build());
            fireworkStarRed.setItemMeta(meta);
        });

    }


    @Override
    public Response execute(Player player) {
        final Location location = player.getLocation();

        // Fx
        PlayerLib.playSound(location, Sound.ENTITY_EGG_THROW, 0.0f);

        final Item item = player.getWorld().dropItem(location, fireworkStarDef);
        item.setVelocity(player.getEyeLocation()
                               .getDirection()
                               .multiply(!player.isSneaking() ? 1.0f : 0.2f)
                               .setY(0.5f));
        item.setPickupDelay(5000);

        GameTask.runTaskTimerTimes((task, tick) -> {
            if (tick % 10 == 0) {
                item.setItemStack(fireworkStarRed);
            }
            else if (tick % 5 == 0) {
                item.setItemStack(fireworkStarDef);
            }

            if (tick == 0) {
                item.remove();
                Utils.createExplosion(item.getLocation(), 5.0d, 20.0d, player, EnumDamageCause.ENTITY_EXPLOSION, null);
            }

        }, 1, 40);

        return Response.OK;
    }
}
