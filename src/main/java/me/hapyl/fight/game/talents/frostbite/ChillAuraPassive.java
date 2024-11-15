package me.hapyl.fight.game.talents.frostbite;


import me.hapyl.fight.game.attribute.AttributeType;
import me.hapyl.fight.game.talents.PassiveTalent;
import me.hapyl.fight.registry.Key;
import org.bukkit.Material;

import javax.annotation.Nonnull;

public class ChillAuraPassive extends PassiveTalent {
    public ChillAuraPassive(@Nonnull Key key) {
        super(key, "Chill Aura");

        setDescription("""
                You emmit a &bchill aura&7, that &bslows&7 and decreases enemies %s in small AoE.
                """.formatted(AttributeType.ATTACK_SPEED)
        );

        setItem(Material.LIGHT_BLUE_DYE);
    }
}