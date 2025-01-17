package me.hapyl.fight.game.heroes.storage;

import io.netty.util.internal.ThreadLocalRandom;
import me.hapyl.fight.event.DamageInput;
import me.hapyl.fight.event.DamageOutput;
import me.hapyl.fight.game.*;
import me.hapyl.fight.game.heroes.Hero;
import me.hapyl.fight.game.heroes.HeroEquipment;
import me.hapyl.fight.game.heroes.Heroes;
import me.hapyl.fight.game.heroes.Role;
import me.hapyl.fight.game.heroes.storage.extra.CauldronEffect;
import me.hapyl.fight.game.heroes.storage.extra.Effect;
import me.hapyl.fight.game.talents.Talent;
import me.hapyl.fight.game.talents.Talents;
import me.hapyl.fight.game.talents.UltimateTalent;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.game.ui.UIComponent;
import me.hapyl.fight.game.weapons.Weapon;
import me.hapyl.fight.util.RandomTable;
import me.hapyl.fight.util.Utils;
import me.hapyl.spigotutils.module.chat.Chat;
import me.hapyl.spigotutils.module.math.Numbers;
import me.hapyl.spigotutils.module.player.PlayerLib;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Sound.ENTITY_WITCH_AMBIENT;
import static org.bukkit.Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR;
import static org.bukkit.potion.PotionEffectType.*;

public class Alchemist extends Hero implements UIComponent, PlayerElement {

    private final RandomTable<Effect> positiveEffects = new RandomTable<>();
    private final RandomTable<Effect> negativeEffects = new RandomTable<>();
    private final Map<Player, Integer> toxinLevel = new HashMap<>();
    private final Map<Player, CauldronEffect> cauldronEffectMap = new HashMap<>();

    public Alchemist() {
        super("Alchemist");
        setRole(Role.STRATEGIST);
        setInfo(
                "An alchemist who was deceived by creation of the abyss. In return of help received an Abyssal Bottle that creates potions from the &0&lvoid &7itself."
        );
        setItem("661691fb01825b9d9ec1b8f04199443146aa7d5627aa745962c0704b6a236027");

        setWeapon(new Weapon(Material.STICK).addEnchant(Enchantment.KNOCKBACK, 1)
                .setName("Stick")
                .setDamage(5.0d)
                .setDescription("Turns out that a stick used in brewing can also be used in battle."));

        final HeroEquipment equipment = getEquipment();
        equipment.setChestplate(31, 5, 3);

        positiveEffects.add(new Effect("made you &lFASTER", PotionEffectType.SPEED, 30, 2))
                .add(new Effect("gave you &lJUMP BOOST", PotionEffectType.JUMP, 30, 1))
                .add(new Effect("made you &lSTRONGER", PotionEffectType.INCREASE_DAMAGE, 30, 3))
                .add(new Effect("gave you &lRESISTANCE", PotionEffectType.DAMAGE_RESISTANCE, 30, 1))
                .add(new Effect("healed half of your missing health", 30) {
                    @Override
                    public void affect(Player player) {
                        final IGamePlayer gp = GamePlayer.getPlayer(player);
                        double missingHealth = gp.getMaxHealth() - gp.getHealth();
                        gp.heal(missingHealth / 2d);
                    }
                });

        negativeEffects.add(new Effect("&lpoisoned you", PotionEffectType.POISON, 15, 0))
                .add(new Effect("&lblinded you", PotionEffectType.BLINDNESS, 15, 0))
                .add(new Effect("&lis withering your blood", PotionEffectType.WITHER, 7, 0))
                .add(new Effect("&lslowed you", PotionEffectType.SLOW, 15, 2))
                .add(new Effect("&lmade you weaker", PotionEffectType.WEAKNESS, 15, 0))
                .add(new Effect("&lis... confusing?", PotionEffectType.CONFUSION, 15, 0));

        setUltimate(new UltimateTalent(
                "Alchemical Madness",
                "Call upon the darkest spells to cast random &c&lNegative &7effect on your foes for &b15s &7and random &a&lPositive &7effect on yourself for &b30s&7.",
                50
        ).setCdSec(30).setItem(Material.FERMENTED_SPIDER_EYE).setSound(ENTITY_WITCH_AMBIENT, 0.5f));
    }

    @Override
    public void useUltimate(Player player) {
        final Effect positiveEffect = positiveEffects.getRandomElement();
        final Effect negativeEffect = negativeEffects.getRandomElement();

        positiveEffect.applyEffects(player);
        Utils.getEnemyPlayers(player).forEach(alivePlayer -> negativeEffect.applyEffects(alivePlayer.getPlayer()));
    }

    @Override
    public DamageOutput processDamageAsDamager(DamageInput input) {
        final LivingEntity victim = input.getEntity();
        final Player player = input.getPlayer();
        final CauldronEffect effect = cauldronEffectMap.get(player);

        if (effect == null || effect.getEffectHits() <= 0 || victim == null) {
            return null;
        }

        final PotionEffectType randomEffect = getRandomEffect();
        victim.addPotionEffect(new PotionEffect(randomEffect, 20, 3));
        effect.decrementEffectPotions();

        Chat.sendMessage(
                player,
                "&c¤ &eVenom Touch applied &l%s &eto %s. &l%s &echarges left.",
                Chat.capitalize(randomEffect.getName()),
                victim.getName(),
                effect.getEffectHits()
        );
        PlayerLib.playSound(player.getLocation(), ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 2.0f);
        return null;
    }

    // some effects aren't really allowed so
    private PotionEffectType getRandomEffect() {
        final PotionEffectType value = PotionEffectType.values()[ThreadLocalRandom.current().nextInt(PotionEffectType.values().length)];
        return (value == BAD_OMEN || value == HEAL || value == HEALTH_BOOST || value == REGENERATION || value == ABSORPTION ||
                value == SATURATION || value == LUCK || value == UNLUCK || value == HERO_OF_THE_VILLAGE) ? getRandomEffect() : value;
    }

    public CauldronEffect getEffect(Player player) {
        return this.cauldronEffectMap.get(player);
    }

    public void startCauldronBoost(Player player) {
        this.cauldronEffectMap.put(player, new CauldronEffect());
    }

    @Override
    public void onStart(Player player) {
        toxinLevel.put(player, 0);
    }

    @Override
    public void onStop() {
        toxinLevel.clear();
        cauldronEffectMap.clear();
    }

    @Override
    public void onDeath(Player player) {
        cauldronEffectMap.remove(player);
        toxinLevel.remove(player);
    }

    private int getToxinLevel(Player player) {
        return toxinLevel.getOrDefault(player, 0);
    }

    private void setToxinLevel(Player player, int i) {
        toxinLevel.put(player, Numbers.clamp(i, 0, 100));
    }

    private boolean isToxinLevelBetween(Player player, int a, int b) {
        final int toxinLevel = getToxinLevel(player);
        return toxinLevel >= a && toxinLevel < b;
    }

    @Override
    public void onStart() {
        new GameTask() {
            @Override
            public void run() {
                Manager.current().getCurrentGame().getAlivePlayers(Heroes.ALCHEMIST).forEach(gp -> {
                    final Player player = gp.getPlayer();
                    if (isToxinLevelBetween(player, 50, 75)) {
                        PlayerLib.addEffect(player, POISON, 20, 2);
                    }
                    else if (isToxinLevelBetween(player, 75, 90)) {
                        PlayerLib.addEffect(player, WITHER, 20, 1);
                    }
                    else if (getToxinLevel(player) >= 100) {
                        gp.setLastDamageCause(EnumDamageCause.TOXIN);
                        gp.die(true);
                    }
                    setToxinLevel(player, getToxinLevel(player) - 1);
                });
            }
        }.runTaskTimer(0, 10);
    }

    @Override
    public Talent getFirstTalent() {
        return Talents.POTION.getTalent();
    }

    @Override
    public Talent getSecondTalent() {
        return Talents.CAULDRON.getTalent();
    }

    @Override
    public Talent getPassiveTalent() {
        return Talents.INTOXICATION.getTalent();
    }

    public void addToxin(Player player, int value) {
        setToxinLevel(player, getToxinLevel(player) + value);
    }

    @Override
    public @Nonnull String getString(Player player) {
        final int toxinLevel = getToxinLevel(player);
        return getToxinColor(player) + "☠ &l" + toxinLevel + "%%";
    }

    private String getToxinColor(Player player) {
        if (isToxinLevelBetween(player, 30, 50)) {
            return "&e";
        }
        else if (isToxinLevelBetween(player, 50, 75)) {
            return "&6";
        }
        else if (isToxinLevelBetween(player, 75, 90)) {
            return "&c";
        }
        else if (isToxinLevelBetween(player, 90, 100)) {
            return "&4";
        }

        return "&a";
    }


}
