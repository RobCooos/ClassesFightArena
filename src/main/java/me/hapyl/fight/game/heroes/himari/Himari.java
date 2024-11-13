package me.hapyl.fight.game.heroes.himari;

import me.hapyl.fight.game.entity.GamePlayer;
import me.hapyl.fight.game.heroes.*;
import me.hapyl.fight.game.heroes.equipment.Equipment;
import me.hapyl.fight.game.talents.Talent;
import me.hapyl.fight.game.talents.TalentRegistry;
import me.hapyl.fight.game.talents.UltimateTalent;
import me.hapyl.fight.game.weapons.Weapon;
import me.hapyl.fight.registry.Key;
import me.hapyl.fight.util.collection.player.PlayerDataMap;
import me.hapyl.fight.util.collection.player.PlayerMap;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class Himari extends Hero implements Listener, PlayerDataHandler<HimariData> {

  private final PlayerDataMap<HimariData> playerData = PlayerMap.newDataMap(player -> new HimariData(player, this));

    public Himari(@Nonnull Key key) {
        super(key,"Himari");

        setArchetypes(Archetype.DAMAGE);
        setGender(Gender.FEMALE);

        setDescription("""
                (Make description later)
                (remind me to pay off those xp bottles to hapyl)
                """);
        setItem("23172927c6518ee184a1466d5f1ea81b989ced61a5d5159e3643bb9caf9c189f");

        setWeapon(new Weapon(Material.ENCHANTED_BOOK)
                .setName("Teachings of Freedom")
                .setDescription("""
                        &8;;Default Weapon. No luck needed.
                        A book that contains a lot of teachings and theory.
                        There are many pages, some of them &f&lglow&7 as you observe more.
                        """
                        //  (she skipped a lot of lessons btw, fuck dr.ed)
                ).setDamage(5.0d));

        final Equipment equipment = getEquipment();
        equipment.setChestPlate(128, 128, 128);
        equipment.setLeggings(100, 100, 100);
        equipment.setBoots(0, 0, 0);

    setUltimate(new HimariUltimate());
    }


    @Override
    public Talent getFirstTalent() {
        return TalentRegistry.LUCKY_DAY;
    }

    @Override
    public Talent getSecondTalent() {
        return TalentRegistry.DEAD_EYE;
    }

    public Talent getThirdTalent(){
        return TalentRegistry.SPIKE_BARRIER;
    }

    @Override
    public Talent getPassiveTalent() {
        return null;
    }

    @Override
    public @NotNull PlayerDataMap<HimariData> getDataMap() {
        return playerData;
    }

    private class HimariUltimate extends UltimateTalent {
        public HimariUltimate() {
            super(Himari.this,"A message to Behold", 60);
            setDescription("""
                    Instantly charges a shot, which draws a circle in a large area.
                    At the end of the timer, a huge damage will go through the circle 3 times, before stopping.
                    Keep out. It damages you too.
                    """);

            setItem(Material.IRON_SWORD);
            setDurationSec(5);
            setCooldownSec(30);
        //    setSound(Sound.BLOCK_ANVIL_USE, 0.25f);

        }

        @Nonnull
        @Override
        public UltimateResponse useUltimate(@Nonnull GamePlayer player) {

            return null;
        }
    }
}
