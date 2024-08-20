package me.hapyl.fight.game.heroes.nyx;

import me.hapyl.fight.game.weapons.Weapon;
import org.bukkit.Material;

public class NyxWeapon extends Weapon {
    public NyxWeapon() {
        super(Material.NETHERITE_SWORD);

        setName("Entropy's Edge");
        setDescription("""
                It's all chaos...
                """);

        setDamage(4.0d);
    }
}
