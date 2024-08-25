package me.hapyl.fight.game.talents.engineer;


import me.hapyl.fight.game.talents.PassiveTalent;
import me.hapyl.fight.registry.Key;
import org.bukkit.Material;

import javax.annotation.Nonnull;

public class MagneticAttractionPassive extends PassiveTalent {
    public MagneticAttractionPassive(@Nonnull Key key) {
        super(key, "Magnetic Attraction");

        setDescription("""
            Every few seconds you'll receive an Iron Ingot.
            Use it to build stuff!
            """
        );

        setItem(Material.IRON_INGOT);
    }
}
