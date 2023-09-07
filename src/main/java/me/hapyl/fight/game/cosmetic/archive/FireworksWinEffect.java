package me.hapyl.fight.game.cosmetic.archive;

import com.google.common.collect.Sets;
import me.hapyl.fight.game.cosmetic.Display;
import me.hapyl.fight.game.cosmetic.WinCosmetic;
import me.hapyl.fight.game.cosmetic.Rarity;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;
import java.util.Set;

public class FireworksWinEffect extends WinCosmetic {

    private final Set<Firework> fireworks;

    public FireworksWinEffect() {
        super("Fireworks", "Let's celebrate this win!", Rarity.COMMON);

        fireworks = Sets.newHashSet();

        setStep(5);
        setMaxTimes(18);
    }

    @Override
    public void onStart(Display display) {

    }

    @Override
    public void onStop(Display display) {
        fireworks.forEach(Entity::remove);
        fireworks.clear();
    }

    @Override
    public void tickTask(Display display, int tick) {
        final Location location = display.getLocation();

        final int randomX = new Random().nextInt(10);
        final int randomY = new Random().nextInt(5);
        final int randomZ = new Random().nextInt(10);

        final boolean negativeX = new Random().nextBoolean();
        final boolean negativeZ = new Random().nextBoolean();

        final Location cloned = location.clone().add(
                negativeX ? -randomX : randomX,
                randomY,
                negativeZ ? -randomZ : randomZ
        );
        if (cloned.getWorld() == null) {
            return;
        }

        fireworks.add(cloned.getWorld().spawn(cloned, Firework.class, me -> {
            final FireworkMeta meta = me.getFireworkMeta();
            meta.setPower(2);
            //new FireworkEffect(true, true, getRandomColors(), getRandomColors(), FireworkEffect.Type.BURST))
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BURST)
                    .withColor(getRandomColor())
                    .withFade(getRandomColor())
                    .withTrail()
                    .build());
            me.setFireworkMeta(meta);
        }));
    }

    private Color getRandomColor() {
        return Color.fromRGB(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
    }
}
