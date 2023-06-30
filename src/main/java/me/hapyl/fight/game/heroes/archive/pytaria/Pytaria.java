package me.hapyl.fight.game.heroes.archive.pytaria;

import me.hapyl.fight.event.DamageInput;
import me.hapyl.fight.event.DamageOutput;
import me.hapyl.fight.game.EnumDamageCause;
import me.hapyl.fight.game.GamePlayer;
import me.hapyl.fight.game.IGamePlayer;
import me.hapyl.fight.game.attribute.AttributeType;
import me.hapyl.fight.game.attribute.HeroAttributes;
import me.hapyl.fight.game.attribute.PlayerAttributes;
import me.hapyl.fight.game.heroes.*;
import me.hapyl.fight.game.talents.Talent;
import me.hapyl.fight.game.talents.Talents;
import me.hapyl.fight.game.talents.UltimateTalent;
import me.hapyl.fight.game.talents.archive.pytaria.FlowerBreeze;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.game.weapons.Weapon;
import me.hapyl.fight.util.Collect;
import me.hapyl.spigotutils.module.chat.Chat;
import me.hapyl.spigotutils.module.entity.Entities;
import me.hapyl.spigotutils.module.inventory.ItemBuilder;
import me.hapyl.spigotutils.module.player.PlayerLib;
import me.hapyl.spigotutils.module.util.BukkitUtils;
import org.bukkit.*;
import org.bukkit.entity.Bee;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;

public class Pytaria extends Hero {

    private final int healthRegenPercent = 40;
    private final int ultimateWindup = 60;
    private final double CRIT_MULTIPLIER = 8.0d;
    private final double ATTACK_MULTIPLIER = 6.5d;

    private final ItemStack[] armorColors = {
            createChestplate(255, 128, 128), // 1
            createChestplate(255, 77, 77),   // 2
            createChestplate(255, 26, 26),   // 3
            createChestplate(179, 0, 0),     // 4
            createChestplate(102, 0, 0)      // 5
    };

    public Pytaria() {
        super("Pytaria");

        setRole(Role.ASSASSIN);
        setArchetype(Archetype.DAMAGE);

        setDescription("Beautiful, but deadly opponent with addiction to flowers. She suffered all her youth, which at the end, made her only stronger.");
        setItem("7bb0752f9fa87a693c2d0d9f29549375feb6f76952da90d68820e7900083f801");

        final HeroAttributes attributes = getAttributes();
        attributes.setValue(AttributeType.HEALTH, 125.0d);
        attributes.setValue(AttributeType.ATTACK, 0.9d);
        attributes.setValue(AttributeType.CRIT_CHANCE, 0.2d);
        attributes.setValue(AttributeType.CRIT_DAMAGE, 0.4d);

        setWeapon(new Weapon(Material.ALLIUM).setName("Annihilallium").setDamage(8.0).setDescription("A beautiful flower, nothing more."));

        final HeroEquipment equipment = getEquipment();
        equipment.setChestplate(222, 75, 85);
        equipment.setLeggings(54, 158, 110);
        equipment.setBoots(179, 204, 204);

        setUltimate(new UltimateTalent("Feel the Breeze", 60)
                .setCooldownSec(50)
                .setDuration(60)
                .appendDescription("""
                        Summon a blooming Bee in front of Pytaria.
                                        
                        The Bee will lock on a closest enemy and charge.
                                        
                        Once charged, unleashes damage in small AoE and regenerates {healthRegenPercent}%% &7of Pytaria's missing health.
                        """, healthRegenPercent)
                .appendAttributeDescription("Health Regeneration", healthRegenPercent)
                .appendAttributeDescription("Ultimate Windup", ultimateWindup)
                .setSound(Sound.ENTITY_BEE_DEATH, 0.0f)
                .setTexture("d4579f1ea3864269c2148d827c0887b0c5ed43a975b102a01afb644efb85ccfd"));
    }

    @Override
    public void useUltimate(Player player) {
        final IGamePlayer gp = GamePlayer.getPlayer(player);
        final double health = gp.getHealth();
        final double maxHealth = gp.getMaxHealth();
        final double missingHp = (maxHealth - health) * healthRegenPercent / maxHealth;

        // FIXME (hapyl): 023, May 23, 2023: don't need this
        final double finalDamage = calculateDamage(player, 30.0d);

        final Location location = player.getLocation();
        final Vector vector = location.getDirection();
        location.add(vector.setY(0).multiply(5));
        location.add(0, 7, 0);

        final Bee bee = Entities.BEE.spawn(location, me -> {
            me.setSilent(true);
            me.setAI(false);
        });

        final LivingEntity nearestEntity = Collect.nearestLivingEntityPrioritizePlayers(location, 50, check -> check != player);
        PlayerLib.playSound(location, Sound.ENTITY_BEE_LOOP_AGGRESSIVE, 1.0f);

        new GameTask() {
            private int windupTime = ultimateWindup;

            @Override
            public void run() {
                final Location lockLocation = nearestEntity == null ? location.clone().subtract(0, 9, 0) : nearestEntity.getEyeLocation();
                final Location touchLocation = drawLine(location.clone(), lockLocation.clone());

                // BOOM
                if (windupTime-- <= 0) {
                    PlayerLib.stopSound(Sound.ENTITY_BEE_LOOP_AGGRESSIVE);
                    PlayerLib.spawnParticle(location, Particle.EXPLOSION_NORMAL, 5, 0.2, 0.2, 0.2, 0.1f);
                    PlayerLib.playSound(location, Sound.ENTITY_BEE_DEATH, 1.5f);
                    bee.remove();
                    cancel();

                    Collect.nearbyLivingEntities(touchLocation, 1.5d).forEach(victim -> {
                        GamePlayer.damageEntity(victim, finalDamage, player, EnumDamageCause.FEEL_THE_BREEZE);
                    });

                    PlayerLib.spawnParticle(touchLocation, Particle.EXPLOSION_LARGE, 3, 0.5, 0, 0.5, 0);
                    PlayerLib.playSound(touchLocation, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.25f);
                }
            }
        }.runTaskTimer(0, 1);

        Chat.sendMessage(player, "&a&l][ &a%s healed for &c%s❤ &a!", this.getName(), BukkitUtils.decimalFormat(missingHp));
        gp.heal(missingHp);
    }

    private Location drawLine(Location start, Location end) {
        double distance = start.distance(end);
        Vector vector = end.toVector().subtract(start.toVector()).normalize().multiply(0.5d);

        for (double i = 0.0D; i < distance; i += 0.5) {
            start.add(vector);
            if (start.getWorld() == null) {
                continue;
            }
            if (!start.getBlock().getType().isAir()) {
                final Location cloned = start.add(0, 0.15, 0);
                start.getWorld().spawnParticle(Particle.FLAME, cloned, 3, 0.1, 0.1, 0.1, 0.02);
                return cloned;
            }
            //start.getWorld().playSound(start, Sound.BLOCK_BAMBOO_HIT, SoundCategory.RECORDS, 1.0f, 2.0f);
            start.getWorld().spawnParticle(Particle.REDSTONE, start, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 0.5f));
        }
        return start;
    }

    @Override
    public void onStart() {
        new GameTask() {
            @Override
            public void run() {
                Heroes.PYTARIA.getAlivePlayers().forEach(gamePlayer -> {
                    recalculateStats(gamePlayer);

                    //final Player player = gamePlayer.getPlayer();
                    //final Item item = player.getWorld().dropItemNaturally(
                    //        player.getLocation(),
                    //        new ItemStack(CollectionUtils.randomElement(Tag.SMALL_FLOWERS.getValues(), Material.POPPY))
                    //);
                    //
                    //item.setPickupDelay(10000);
                    //item.setTicksLived(5980);
                });
            }
        }.runTaskTimer(0, 5);
    }

    @Override
    public DamageOutput processDamageAsDamager(DamageInput input) {
        //return new DamageOutput(calculateDamage(input.getPlayer(), input.getDamage()));
        return null;
    }

    @Override
    public DamageOutput processDamageAsVictim(DamageInput input) {
        return null;
    }

    public void recalculateStats(@Nonnull IGamePlayer gamePlayer) {
        final PlayerAttributes attributes = gamePlayer.getAttributes();

        final double maxHealth = gamePlayer.getMaxHealth();
        final double health = gamePlayer.getHealth();
        final double factor = (maxHealth - health) / maxHealth / 10;

        final double critIncrease = CRIT_MULTIPLIER * factor;
        final double attackIncrease = ATTACK_MULTIPLIER * factor;

        attributes.reset(AttributeType.CRIT_CHANCE);
        attributes.reset(AttributeType.ATTACK);
        attributes.reset(AttributeType.DEFENSE);

        attributes.addSilent(AttributeType.CRIT_CHANCE, critIncrease);
        attributes.addSilent(AttributeType.ATTACK, attackIncrease);
        attributes.subtractSilent(AttributeType.DEFENSE, critIncrease);
    }

    private ItemStack createChestplate(int red, int green, int blue) {
        return ItemBuilder.leatherTunic(Color.fromRGB(red, green, blue)).cleanToItemSack();
    }

    private void updateChestplateColor(Player player) {
        final PlayerInventory inventory = player.getInventory();
        final IGamePlayer gp = GamePlayer.getPlayer(player);
        final double missingHealth = gp.getMaxHealth() - gp.getHealth();

        if (isBetween(missingHealth, 0, 10)) {
            inventory.setChestplate(armorColors[0]);
        }
        else if (isBetween(missingHealth, 10, 20)) {
            inventory.setChestplate(armorColors[1]);
        }
        else if (isBetween(missingHealth, 20, 30)) {
            inventory.setChestplate(armorColors[2]);
        }
        else if (isBetween(missingHealth, 30, 40)) {
            inventory.setChestplate(armorColors[3]);
        }
        else {
            inventory.setChestplate(armorColors[4]);
        }
    }

    private boolean isBetween(double value, double min, double max) {
        return value >= min && value < max;
    }

    public double calculateDamage(Player player, double damage) {
        final IGamePlayer gp = GamePlayer.getPlayer(player);
        final double health = gp.getHealth();
        final double maxHealth = gp.getMaxHealth();
        //final double multiplier = ((maxHealth - health) / 10);
        //final double addDamage = (damage * 30 / 100) * multiplier;
        //
        //return Math.max(damage + addDamage, damage);

        return (health <= maxHealth / 2) ? damage * 1.5d : damage;

    }

    @Override
    public Talent getFirstTalent() {
        return Talents.FLOWER_ESCAPE.getTalent();
    }

    @Override
    public FlowerBreeze getSecondTalent() {
        return (FlowerBreeze) Talents.FLOWER_BREEZE.getTalent();
    }

    @Override
    public Talent getPassiveTalent() {
        return Talents.EXCELLENCY.getTalent();
    }
}