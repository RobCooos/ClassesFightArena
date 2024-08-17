package me.hapyl.fight.game.challenge;

import me.hapyl.fight.game.heroes.Archetype;
import me.hapyl.fight.game.heroes.Hero;
import me.hapyl.fight.game.heroes.HeroRegistry;
import me.hapyl.fight.game.profile.PlayerProfile;

import javax.annotation.Nonnull;
import java.util.List;

public class ArchetypeChallenge extends Challenge {

    private final Archetype archetype;

    public ArchetypeChallenge(String name, Archetype archetype) {
        super(name, "Play {} games as %s&7.".formatted(archetype));

        this.archetype = archetype;

        setMin(2);
        setMax(4);
    }

    @Override
    public boolean canGenerate(@Nonnull PlayerProfile profile) {
        final List<Hero> playable = HeroRegistry.playable();

        playable.removeIf(hero -> hero.getArchetypes().excludes(archetype));
        playable.removeIf(hero -> hero.isLocked(profile.getPlayer()));

        return !playable.isEmpty();
    }
}
