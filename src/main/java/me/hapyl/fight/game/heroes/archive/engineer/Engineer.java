package me.hapyl.fight.game.heroes.archive.engineer;

import me.hapyl.fight.game.entity.GamePlayer;
import me.hapyl.fight.game.heroes.*;
import me.hapyl.fight.game.loadout.HotbarSlots;
import me.hapyl.fight.game.talents.Talent;
import me.hapyl.fight.game.talents.Talents;
import me.hapyl.fight.game.talents.archive.engineer.Construct;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.util.Nulls;
import me.hapyl.fight.util.collection.player.PlayerMap;
import me.hapyl.spigotutils.module.inventory.ItemBuilder;
import me.hapyl.spigotutils.module.math.Numbers;
import org.bukkit.Material;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Engineer extends Hero implements DisabledHero {

    public final int IRON_RECHARGE_RATE = 1;
    public final int MAX_IRON = 10;

    private final PlayerMap<Construct> constructs = PlayerMap.newMap();
    private final PlayerMap<Integer> playerIron = PlayerMap.newMap();

    public Engineer() {
        super("Engineer");

        setArchetype(Archetype.STRATEGY);

        setItem("55f0bfea3071a0eb37bcc2ca6126a8bdd79b79947734d86e26e4d4f4c7aa9");
    }

    @Override
    public void onDeath(@Nonnull GamePlayer player) {
        Nulls.runIfNotNull(constructs.remove(player), Construct::remove);
    }

    @Nullable
    public Construct getConstruct(GamePlayer player) {
        return constructs.get(player);
    }

    /**
     * This removes the current construct if exists and refunds 50% of the cost.
     *
     * @param player - Player.
     */
    public void destruct(GamePlayer player) {
        final Construct construct = constructs.remove(player);

        if (construct == null) {
            return;
        }

        Heroes.ENGINEER.getHero(Engineer.class).addIron(player, (int) (construct.getCost() * 0.25));
        construct.remove();
    }

    @Override
    public UltimateCallback useUltimate(@Nonnull GamePlayer player) {
        return UltimateCallback.OK;
    }

    public int getIron(GamePlayer player) {
        return playerIron.computeIfAbsent(player, v -> 0);
    }

    public void subtractIron(GamePlayer player, int amount) {
        addIron(player, -amount);
    }

    public void addIron(GamePlayer player, int amount) {
        playerIron.compute(player, (p, i) -> Numbers.clamp(i == null ? amount : i + amount, 0, MAX_IRON));
        updateIron(player);
    }

    public void updateIron(GamePlayer player) {
        final PlayerInventory inventory = player.getInventory();

        player.setItem(HotbarSlots.HERO_ITEM, ItemBuilder.of(Material.IRON_INGOT, "&aIron", "You iron to build your structures!")
                .setAmount(playerIron.getOrDefault(player, 1))
                .asIcon());
    }

    @Override
    public void onStart() {
        new GameTask() {
            @Override
            public void run() {
                Heroes.ENGINEER.getAlivePlayers().forEach(player -> {
                    addIron(player, 1);
                });
            }
        }.runTaskTimer(IRON_RECHARGE_RATE, IRON_RECHARGE_RATE);
    }

    @Override
    public Talent getFirstTalent() {
        return Talents.ENGINEER_SENTRY.getTalent();
    }

    @Override
    public Talent getSecondTalent() {
        return Talents.ENGINEER_TURRET.getTalent();
    }

    @Nullable
    @Override
    public Talent getThirdTalent() {
        return Talents.ENGINEER_RECALL.getTalent();
    }

    @Override
    public Talent getPassiveTalent() {
        return Talents.ENGINEER_PASSIVE.getTalent();
    }

    @Nullable
    public Construct removeConstruct(GamePlayer player) {
        final Construct construct = constructs.remove(player);

        if (construct == null) {
            return null;
        }

        construct.remove();
        player.sendMessage("&aYour previous %s was removed!", getName());
        return construct;
    }

    public void setConstruct(GamePlayer player, Construct construct) {
        constructs.put(player, construct);
        construct.runTaskTimer(0, 1);
    }
}