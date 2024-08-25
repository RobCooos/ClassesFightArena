package me.hapyl.fight.game.talents.vortex;


import me.hapyl.fight.game.talents.PassiveTalent;
import me.hapyl.fight.registry.Key;
import org.bukkit.Material;

import javax.annotation.Nonnull;

public class EyesOfTheGalaxyPassive extends PassiveTalent {
    public EyesOfTheGalaxyPassive(@Nonnull Key key) {
        super(key, "Eyes of the Galaxy");

        setDescription("""
                Astral Stars you place will glow different colors:____&eYellow &7indicates a placed star.____&bAqua &7indicates closest star that will be consumed upon teleport.
                
                &aGreen &7indicates star you will blink to upon teleport.
                """
        );

        setItem(Material.ENDER_EYE);
    }
}
