package me.hapyl.fight.game.heroes.storage;

import me.hapyl.fight.game.heroes.ClassEquipment;
import me.hapyl.fight.game.heroes.Hero;
import me.hapyl.fight.game.heroes.Role;
import me.hapyl.fight.game.heroes.storage.extra.GravityGun;
import me.hapyl.fight.game.heroes.storage.extra.PhysGun;
import me.hapyl.fight.game.talents.Talent;
import me.hapyl.fight.game.talents.Talents;
import me.hapyl.fight.game.talents.UltimateTalent;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.game.weapons.Weapon;
import me.hapyl.fight.util.ItemStacks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class DrEd extends Hero {

    private final Weapon ultimateWeapon = new PhysGun();

    public DrEd() {
        super("Dr. Ed");
        this.setRole(Role.STRATEGIST);
        this.setInfo("Simple named scientist with not so simple inventions...");
        this.setItem(Material.GLASS_BOTTLE);

        final ClassEquipment equipment = this.getEquipment();
        equipment.setHelmet(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I1MWU5NmJkZGQxNzc5OTJkNjgyNzhjOWQ1ZjFlNjg1YjYwZmJiOTRhYWE3MDkyNTllOWYyNzgxYzc2ZjgifX19");
        equipment.setChestplate(179, 204, 204);
        equipment.setLeggings(148, 184, 184);
        equipment.setBoots(71, 107, 107);

        this.setWeapon(new GravityGun());

        // Grants Dr. Ed an upgraded version of his \"" + Abilities.GRAVITY_GUN.getName() + "\" &7for &b" + (this.ultimateTime / 20) + "s&7. This device is capable of capturing entities' flesh and energy, allowing to manipulate them.
        // Grants Dr. Ed an upgraded version of {} &7for &b{}s &7that is capable of capturing entities' flesh and energy, allowing to manipulate them.
        this.setUltimate(new UltimateTalent(
                "Upgrades People, Upgrades!",
                String.format(
                        "Grants Dr. Ed an upgraded version of &a%s &7for {duration} that is capable of capturing entities' flesh and energy, allowing to manipulate them.",
                        getWeapon().getName()
                ), 70
        ).setDuration(200).setItem(Material.GOLDEN_HORSE_ARMOR));
    }

    @Override
    public void useUltimate(Player player) {
        final PlayerInventory inventory = player.getInventory();
        inventory.setItem(4, ultimateWeapon.getItem());
        inventory.setHeldItemSlot(4);

        GameTask.runLater(() -> {
            inventory.setItem(4, ItemStacks.AIR);
            inventory.setHeldItemSlot(0);
        }, getUltimateDuration());
    }

    @Override
    public Talent getFirstTalent() {
        return Talents.CONFUSION_POTION.getTalent();
    }

    @Override
    public Talent getSecondTalent() {
        return null;
    }

    @Override
    public Talent getPassiveTalent() {
        return null;
    }
}
