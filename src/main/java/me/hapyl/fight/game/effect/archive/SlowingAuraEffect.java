package me.hapyl.fight.game.effect.archive;

import me.hapyl.fight.game.effect.GameEffect;
import me.hapyl.fight.game.entity.LivingGameEntity;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;

public class SlowingAuraEffect extends GameEffect {

    public final int COOLDOWN_MODIFIER = 2;

    public SlowingAuraEffect() {
        super("Slowing Aura");
        setDescription("Slows players and increases talent cooldowns.");
        setPositive(false);
    }

    @Override
    public void onStart(@Nonnull LivingGameEntity entity) {
        entity.addPotionEffect(PotionEffectType.SLOW, 10000, 1);
    }

    @Override
    public void onStop(@Nonnull LivingGameEntity entity) {
        entity.removePotionEffect(PotionEffectType.SLOW);
    }

    @Override
    public void onTick(@Nonnull LivingGameEntity entity, int tick) {
    }
}
