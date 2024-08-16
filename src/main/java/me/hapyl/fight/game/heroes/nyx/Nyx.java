package me.hapyl.fight.game.heroes.nyx;

import me.hapyl.fight.event.custom.AttributeTemperEvent;
import me.hapyl.fight.game.Named;
import me.hapyl.fight.game.entity.GamePlayer;
import me.hapyl.fight.game.entity.LivingGameEntity;
import me.hapyl.fight.game.heroes.*;
import me.hapyl.fight.game.heroes.equipment.Equipment;
import me.hapyl.fight.game.talents.*;
import me.hapyl.fight.game.talents.nyx.NyxPassive;
import me.hapyl.fight.game.ui.UIComponent;
import me.hapyl.fight.util.collection.player.PlayerDataMap;
import me.hapyl.fight.util.collection.player.PlayerMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Nyx extends Hero implements Listener, PlayerDataHandler<NyxData>, UIComponent {

    private final PlayerDataMap<NyxData> nyxDataMap = PlayerMap.newDataMap(NyxData::new);

    public Nyx(@Nonnull Heroes handle) {
        super(handle, "Nyx");

        setArchetypes(Archetype.SUPPORT, Archetype.HEXBANE, Archetype.DEFENSE, Archetype.POWERFUL_ULTIMATE);
        setAffiliation(Affiliation.THE_WITHERS);
        setGender(Gender.FEMALE);

        final Equipment equipment = getEquipment();

        setItem("757240b2e096de5d541b860a06fa29809e08d5952bcf4bb38e19ca12aac09ef2");

        setDescription("""
                &8&o;;Chaos... brings victory...
                """);

        setUltimate(new NyxUltimate());
    }

    @EventHandler()
    public void handleAttributeChange(AttributeTemperEvent ev) {
        final LivingGameEntity entity = ev.getEntity();
        final LivingGameEntity applier = ev.getApplier();

        if (!(applier instanceof GamePlayer playerApplier) || ev.isBuff()) {
            return;
        }

        final GamePlayer nyx = getNyx(playerApplier);

        // No nyx on the team
        if (nyx == null) {
            return;
        }

        getPassiveTalent().execute(nyx, playerApplier, entity);

        // Decrease stack
        getPlayerData(nyx).decrementChaosStacks();
    }

    @Nonnull
    @Override
    public PlayerDataMap<NyxData> getDataMap() {
        return nyxDataMap;
    }

    @Override
    public Talent getFirstTalent() {
        return Talents.WITHER_ROSE_PATH.getTalent();
    }

    @Override
    public Talent getSecondTalent() {
        return Talents.CHAOS_GROUND.getTalent();
    }

    @Override
    public NyxPassive getPassiveTalent() {
        return (NyxPassive) Talents.NYX_PASSIVE.getTalent();
    }

    @Nonnull
    @Override
    public String getString(@Nonnull GamePlayer player) {
        final NyxData data = getPlayerData(player);
        final int chaosStacks = data.getChaosStacks();

        return Named.THE_CHAOS.prefix(chaosStacks);
    }

    @Nullable
    private GamePlayer getNyx(@Nonnull GamePlayer player) {
        if (validateNyx(player)) {
            return player;
        }

        return player.getTeam().getPlayers()
                .stream()
                .filter(this::validateNyx)
                .findFirst()
                .orElse(null);
    }

    private boolean validateNyx(GamePlayer player) {
        final NyxPassive passive = getPassiveTalent();

        return validatePlayer(player)
                && getPlayerData(player).getChaosStacks() > 0
                && !passive.hasCd(player);
    }

    private class NyxUltimate extends OverchargeUltimateTalent {
        public NyxUltimate() {
            super("nyx ultimate", 2, 4);

            setDescription("""
                    Does some very cool stuff and deals big damage.
                    """);

            setOverchargeDescription("""
                    Increases the damage by &a+69420%%&7!
                    """);
        }

        @Nonnull
        @Override
        public UltimateResponse useUltimate(@Nonnull GamePlayer player, @Nonnull ChargeType type) {
            player.sendMessage("type=" + type.name());

            return UltimateResponse.OK;
        }
    }
}
