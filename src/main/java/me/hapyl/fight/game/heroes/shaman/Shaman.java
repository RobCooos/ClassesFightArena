package me.hapyl.fight.game.heroes.shaman;

import me.hapyl.eterna.module.math.Tick;
import me.hapyl.fight.database.key.DatabaseKey;
import me.hapyl.fight.event.custom.GameDamageEvent;
import me.hapyl.fight.event.custom.GameEntityHealEvent;
import me.hapyl.fight.game.Named;
import me.hapyl.fight.game.attribute.AttributeType;
import me.hapyl.fight.game.attribute.HeroAttributes;
import me.hapyl.fight.game.attribute.temper.Temper;
import me.hapyl.fight.game.attribute.temper.TemperInstance;
import me.hapyl.fight.game.effect.EffectType;
import me.hapyl.fight.game.entity.GamePlayer;
import me.hapyl.fight.game.entity.LivingGameEntity;
import me.hapyl.fight.game.heroes.*;
import me.hapyl.fight.game.heroes.equipment.Equipment;
import me.hapyl.fight.game.talents.Talent;
import me.hapyl.fight.game.talents.TalentRegistry;
import me.hapyl.fight.game.talents.TalentType;
import me.hapyl.fight.game.talents.UltimateTalent;
import me.hapyl.fight.game.talents.shaman.TotemTalent;
import me.hapyl.fight.game.team.GameTeam;
import me.hapyl.fight.game.ui.UIComponent;
import me.hapyl.fight.util.Collect;
import me.hapyl.fight.util.collection.player.PlayerDataMap;
import me.hapyl.fight.util.collection.player.PlayerMap;
import me.hapyl.fight.util.displayfield.DisplayField;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;

public class Shaman extends Hero implements PlayerDataHandler<ShamanData>, UIComponent, Listener {

    private final PlayerDataMap<ShamanData> shamanData = PlayerMap.newDataMap(ShamanData::new);

    private final double damageIncreasePerOverheal = 0.05;
    private final double maxOverhealUse = 10;
    private final double maxOverhealDistance = 25;

    public Shaman(@Nonnull DatabaseKey key) {
        super(key, "Shaman");

        setAffiliation(Affiliation.THE_JUNGLE);
        setArchetypes(Archetype.SUPPORT);
        setGender(Gender.MALE);

        setDescription("""
                An orc from the jungle. Always rumbles about something.
                """
        );

        setWeapon(new ShamanWeapon());
        setItem("a90515c41b3e131b623cc04978f101aab2e5b82c892890df991b7c079f91d2bd");

        final HeroAttributes attributes = getAttributes();

        attributes.setHealth(75);
        attributes.setAttack(50);
        attributes.setDefense(75);
        attributes.setVitality(50); // to balance self-healing
        attributes.setMending(200);
        attributes.setEffectResistance(30);

        final Equipment equipment = getEquipment();
        equipment.setChestPlate(110, 94, 74);
        equipment.setLeggings(57, 40, 90);

        setUltimate(new ShamanUltimate());
    }

    @EventHandler()
    public void handleOverhealGain(GameEntityHealEvent ev) {
        if (!(ev.getHealer() instanceof GamePlayer player)) {
            return;
        }

        if (!validatePlayer(player)) {
            return;
        }

        final double excessHealing = ev.getExcessHealing();

        if (excessHealing <= 0) {
            return;
        }

        final ShamanData data = getPlayerData(player);
        data.increaseOverheal(excessHealing);
    }

    @EventHandler()
    public void handleOverhealDamage(GameDamageEvent ev) {
        final LivingGameEntity entity = ev.getEntity();

        if (!(ev.getDamager() instanceof LivingGameEntity damager)) {
            return;
        }

        final GameTeam team = damager.getTeam();

        if (team == null) {
            return;
        }

        for (GamePlayer player : team.getPlayers()) {
            if (!validatePlayer(player)) {
                continue;
            }

            if (damager.getLocation().distance(player.getLocation()) >= maxOverhealDistance) {
                continue;
            }

            final ShamanData data = getPlayerData(player);
            final double overhealCapped = Math.min(data.getOverheal(), maxOverhealUse);

            if (overhealCapped <= 0) {
                continue;
            }

            final double damageIncrease = 1 + overhealCapped * damageIncreasePerOverheal;

            ev.multiplyDamage(damageIncrease);
            data.decreaseOverheal(overhealCapped);

            // Spawn display to notify that the damage is increased
            entity.spawnBuffDisplay("&2🐍", 20);
            entity.playWorldSound(Sound.ENTITY_CAT_HISS, 1.25f);
            return;
        }
    }

    @Override
    @Nonnull
    public TotemTalent getFirstTalent() {
        return TalentRegistry.TOTEM;
    }

    @Override
    public Talent getSecondTalent() {
        return TalentRegistry.TOTEM_IMPRISONMENT;
    }

    @Override
    public Talent getThirdTalent() {
        return TalentRegistry.SHAMAN_MARK;
    }

    @Override
    public Talent getPassiveTalent() {
        return TalentRegistry.OVERHEAL;
    }

    @Nonnull
    @Override
    public String getString(@Nonnull GamePlayer player) {
        final ShamanData data = getPlayerData(player);

        return "%s &a%.0f".formatted(Named.OVERHEAL.getCharacter(), data.getOverheal()) + (data.isOverheadMaxed() ? " &lMAX!" : "");
    }

    @Nonnull
    @Override
    public PlayerDataMap<ShamanData> getDataMap() {
        return shamanData;
    }

    public class ShamanUltimate extends UltimateTalent {

        @DisplayField private final double increaseRadius = 7.5d;
        @DisplayField(percentage = true) private final double effectResIncrease = 0.5d;
        @DisplayField private final int effectResIncreaseDuration = Tick.fromSecond(12);

        private final TemperInstance temperInstance = Temper.SPIRITUAL_CLEANSING
                .newInstance()
                .increase(AttributeType.EFFECT_RESISTANCE, effectResIncrease);

        public ShamanUltimate() {
            super(Shaman.this, "Spiritual Cleansing", 45);

            setDescription("""
                    Instantly cleanse all &cnegative&7 effects from nearby &aallies&7.
                    
                    Also increase their %s for &b{effectResIncreaseDuration}&7.
                    """.formatted(AttributeType.EFFECT_RESISTANCE)
            );

            setType(TalentType.SUPPORT);
            setItem(Material.MILK_BUCKET);
            setSound(Sound.ENTITY_GOAT_SCREAMING_MILK, 0.0f);
            setCooldownSec(30);
        }

        @Nonnull
        @Override
        public UltimateResponse useUltimate(@Nonnull GamePlayer player) {
            Collect.nearbyEntities(player.getLocation(), increaseRadius).forEach(entity -> {
                if (!player.isSelfOrTeammate(entity)) {
                    return;
                }

                // Remove effects
                player.removeEffectsByType(EffectType.NEGATIVE);

                temperInstance.temper(entity, effectResIncreaseDuration);

                // Fx
                final Location location = entity.getLocation();

                entity.spawnWorldParticle(location, Particle.EFFECT, 20, 0.25d, 0.5d, 0.25d, 0.7f);
                entity.playWorldSound(Sound.ENTITY_WITCH_DRINK, 0.0f);

                if (player == entity) {
                    player.sendMessage(AttributeType.EFFECT_RESISTANCE.getCharacter() + " You cleansed yourself!");
                }
                else {
                    entity.sendMessage(AttributeType.EFFECT_RESISTANCE.getCharacter() + " &d%s cleansed you!".formatted(player.getName()));
                }

            });

            // Fx
            player.playWorldSound(Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.25f);
            player.playWorldSound(Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.25f);

            return UltimateResponse.OK;
        }
    }
}
