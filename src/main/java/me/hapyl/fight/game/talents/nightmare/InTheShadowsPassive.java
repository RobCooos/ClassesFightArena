package me.hapyl.fight.game.talents.nightmare;

import me.hapyl.fight.database.key.DatabaseKey;
import me.hapyl.fight.game.attribute.AttributeType;
import me.hapyl.fight.game.talents.PassiveTalent;
import org.bukkit.Material;

import javax.annotation.Nonnull;

public class InTheShadowsPassive extends PassiveTalent {
    public InTheShadowsPassive(@Nonnull DatabaseKey key) {
        super(key, "In the Shadows");

        setDescription("""
                While in moody light, your %s&7 and %s&7 increases.
                """.formatted(AttributeType.ATTACK, AttributeType.SPEED)
        );

        setItem(Material.DRIED_KELP);
    }
}
