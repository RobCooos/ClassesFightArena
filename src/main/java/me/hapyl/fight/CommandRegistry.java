package me.hapyl.fight;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import me.hapyl.fight.build.NamedSignReader;
import me.hapyl.fight.cmds.*;
import me.hapyl.fight.database.Database;
import me.hapyl.fight.database.PlayerDatabase;
import me.hapyl.fight.database.rank.PlayerRank;
import me.hapyl.fight.game.*;
import me.hapyl.fight.game.attribute.temper.Temper;
import me.hapyl.fight.game.cosmetic.CosmeticCollection;
import me.hapyl.fight.game.cosmetic.crate.Crates;
import me.hapyl.fight.game.entity.GameEntities;
import me.hapyl.fight.game.entity.GameEntity;
import me.hapyl.fight.game.entity.GamePlayer;
import me.hapyl.fight.game.entity.LivingGameEntity;
import me.hapyl.fight.game.entity.cooldown.Cooldown;
import me.hapyl.fight.game.entity.cooldown.CooldownData;
import me.hapyl.fight.game.heroes.Heroes;
import me.hapyl.fight.game.heroes.archive.bloodfield.BatCloud;
import me.hapyl.fight.game.heroes.archive.bloodfield.Bloodfiend;
import me.hapyl.fight.game.heroes.archive.bloodfield.BloodfiendData;
import me.hapyl.fight.game.heroes.archive.dark_mage.AnimatedWither;
import me.hapyl.fight.game.heroes.archive.doctor.ElementType;
import me.hapyl.fight.game.heroes.archive.engineer.Engineer;
import me.hapyl.fight.game.lobby.LobbyItems;
import me.hapyl.fight.game.lobby.StartCountdown;
import me.hapyl.fight.game.playerskin.PlayerSkin;
import me.hapyl.fight.game.profile.PlayerProfile;
import me.hapyl.fight.game.reward.DailyReward;
import me.hapyl.fight.game.talents.archive.engineer.Construct;
import me.hapyl.fight.game.talents.archive.juju.Orbiting;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.game.task.TickingGameTask;
import me.hapyl.fight.game.team.GameTeam;
import me.hapyl.fight.game.ui.display.DamageDisplay;
import me.hapyl.fight.game.ui.splash.SplashText;
import me.hapyl.fight.gui.LegacyAchievementGUI;
import me.hapyl.fight.gui.styled.profile.DeliveryGUI;
import me.hapyl.fight.util.Collect;
import me.hapyl.fight.util.Utils;
import me.hapyl.fight.ux.Message;
import me.hapyl.spigotutils.module.chat.Chat;
import me.hapyl.spigotutils.module.chat.Gradient;
import me.hapyl.spigotutils.module.chat.LazyEvent;
import me.hapyl.spigotutils.module.chat.gradient.Interpolators;
import me.hapyl.spigotutils.module.command.*;
import me.hapyl.spigotutils.module.entity.Entities;
import me.hapyl.spigotutils.module.hologram.Hologram;
import me.hapyl.spigotutils.module.inventory.ItemBuilder;
import me.hapyl.spigotutils.module.inventory.gui.GUI;
import me.hapyl.spigotutils.module.locaiton.LocationHelper;
import me.hapyl.spigotutils.module.math.Cuboid;
import me.hapyl.spigotutils.module.math.Numbers;
import me.hapyl.spigotutils.module.math.nn.IntInt;
import me.hapyl.spigotutils.module.player.PlayerLib;
import me.hapyl.spigotutils.module.reflect.DataWatcherType;
import me.hapyl.spigotutils.module.reflect.Reflect;
import me.hapyl.spigotutils.module.reflect.glow.Glowing;
import me.hapyl.spigotutils.module.reflect.npc.HumanNPC;
import me.hapyl.spigotutils.module.reflect.npc.NPCPose;
import me.hapyl.spigotutils.module.util.*;
import net.minecraft.world.entity.Entity;
import org.bson.Document;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class CommandRegistry extends DependencyInjector<Main> {

    private final CommandProcessor processor;

    public CommandRegistry(Main main) {
        super(main);
        this.processor = new CommandProcessor(main);

        register(new HeroCommand("hero"));
        register(new GameCommand("cf"));
        register(new ReportCommandCommand("report"));
        register(new ParticleCommand("part"));
        register(new GameEffectCommand("gameEffect"));
        register(new MapCommand("map"));
        register(new ModeCommand("mode"));
        register(new AdminCommand("admin"));
        register(new DebugBooster("debugBooster"));
        register(new SettingCommand("setting"));
        register(new TutorialCommand("tutorial"));
        register(new TeamCommand("team"));
        register(new TestWinConditionCommand("testWinCondition"));
        register(new TriggerWinCommand("triggerWin"));
        register(new DummyCommand("dummy"));
        register(new CooldownCommand("cooldown"));
        register(new ExperienceCommand("experience"));
        register(new AchievementCommand("achievement"));
        register(new CosmeticCommand("cosmetic"));
        register(new SyncDatabaseCommand("syncDatabase"));
        register(new CastSpellCommand("cast"));
        register(new UpdateParkourLeaderboardCommand("updateParkourLeaderboard"));
        register(new ModifyParkourCommand("modifyParkour"));
        register(new InterruptCommand("interrupt"));
        register(new TestDatabaseCommand("testdatabase"));
        register(new EquipCommand("equip"));
        register(new HeadCommand("head"));
        register(new RankCommand("rank"));
        register(new DebugPlayerCommand("debugPlayer"));
        register(new DebugAchievementCommand("debugAchievement"));
        register(new ProfileCommand("profile"));
        register(new GVarCommand("gvar"));
        register(new PlayerAttributeCommand("playerAttribute"));
        register(new SnakeBuilderCommand("snakeBuilder"));
        register(new CrateCommandCommand("crate"));
        register(new GlobalConfigCommand("globalConfig"));
        register(new SimplePlayerCommand("delivery") {
            @Override
            protected void execute(Player player, String[] args) {
                new DeliveryGUI(player);
            }
        });

        register(new SimpleAdminCommand("lastHapylGUI") {
            @Override
            protected void execute(CommandSender sender, String[] args) {
                final Player hapyl = Bukkit.getPlayer("hapyl");

                if (hapyl == null) {
                    Chat.sendMessage(sender, "&chapyl is not online!");
                    return;
                }

                final GUI lastGUI = GUI.getPlayerLastGUI(hapyl);
                Debug.info("lastGUI=" + (lastGUI == null ? "NONE!" : lastGUI.getName()));
            }
        });

        register("viewLegacyAchievementGUI", (player, args) -> {
            new LegacyAchievementGUI(player);
        });

        register("getLobbyItems", (player, args) -> {
            LobbyItems.giveAll(player);
            Chat.sendMessage(player, "&aThere you go!");
        });

        register("startAndCancelCountdown", (player, args) -> {
            final Manager manager = Manager.current();
            manager.createStartCountdown(DebugData.FORCE);
            manager.stopStartCountdown(player);

            Chat.sendMessage(player, "&aDone!");
        });

        register(new SimplePlayerAdminCommand("entity") {
            @Override
            protected void execute(Player player, String[] args) {
                final GameEntities entity = getArgument(args, 0).toEnum(GameEntities.class);

                if (entity == null) {
                    Chat.sendMessage(player, "&cInvalid entity!");
                    return;
                }

                entity.spawn(player.getLocation());
                Chat.sendMessage(player, "&aSpawned %s!", entity.type);
            }

            @Nullable
            @Override
            protected List<String> tabComplete(CommandSender sender, String[] args) {
                return completerSort(GameEntities.values(), args);
            }
        });

        register("testKnockback360", (player, args) -> {
            final TickingGameTask task = new TickingGameTask() {
                @Override
                public void run(int tick) {
                    if (tick >= 360) {
                        Chat.sendMessage(player, "&aFinished!");
                        cancel();
                        return;
                    }

                    player.sendHurtAnimation(tick);
                }
            };
            task.setIncrement(15);
            task.runTaskTimer(0, 1);
        });

        register("damageSelf", (player, args) -> {
            final GamePlayer gamePlayer = CF.getPlayer(player);

            if (gamePlayer == null) {
                Chat.sendMessage(player, "&cNo handle!");
                return;
            }

            final double damage = args.length > 0 ? Numbers.getDouble(args[0], 1.0d) : 1.0d;

            gamePlayer.setLastDamager(gamePlayer);
            gamePlayer.damage(damage, EnumDamageCause.ENTITY_ATTACK);
            Chat.sendMessage(player, "&aDamaged!");
        });

        register(new SimplePlayerAdminCommand("testBatCloud") {

            private BatCloud batCloud;

            @Override
            protected void execute(Player player, String[] args) {
                if (batCloud != null) {
                    batCloud.remove();
                    batCloud = null;

                    Chat.sendMessage(player, "&cRemoved!");
                    return;
                }

                batCloud = new BatCloud(player);
                Chat.sendMessage(player, "&aSpawned!");
            }
        });

        register("deleteGamePlayer", (player, args) -> {
            final PlayerProfile profile = PlayerProfile.getOrCreateProfile(player);
            profile.resetGamePlayer();

            Chat.sendMessage(player, "&aDone!");
        });

        register("dumpColor", (player, args) -> {
            final ItemStack item = player.getInventory().getItemInMainHand();
            final ItemMeta meta = item.getItemMeta();

            if (meta instanceof LeatherArmorMeta colorMeta) {
                final org.bukkit.Color color = colorMeta.getColor();
                final int red = color.getRed();
                final int green = color.getGreen();
                final int blue = color.getBlue();

                Chat.sendClickableHoverableMessage(
                        player,
                        LazyEvent.suggestCommand(red + ", " + green + ", " + blue),
                        LazyEvent.showText("&eClick to copy color!"),
                        "&aColor: " + color + " &6&lCLICK TO COPY RGB"
                );
            }

            if (meta instanceof ArmorMeta armorMeta) {
                final ArmorTrim trim = armorMeta.getTrim();

                if (trim != null) {
                    final TrimPattern pattern = trim.getPattern();
                    final TrimMaterial material = trim.getMaterial();

                    final String patternKey = pattern.getKey().getKey().toUpperCase();
                    final String materialKey = material.getKey().getKey().toUpperCase();

                    Chat.sendClickableHoverableMessage(
                            player,
                            LazyEvent.suggestCommand(patternKey + ", " + materialKey),
                            LazyEvent.showText("&eClick to copy armor trim!"),
                            "&aTrim: " + patternKey + " x " + materialKey + " &6&lCLICK TO COPY TRIM"
                    );
                }
            }

        });

        register(new SwiftTeleportCommand());

        register(new SimplePlayerCommand("cancelCountdown") {
            @Override
            protected void execute(Player player, String[] args) {
                final Manager manager = Manager.current();
                final StartCountdown countdown = manager.getStartCountdown();

                if (countdown == null) {
                    Chat.sendMessage(player, "&cNothing to cancel!");
                    return;
                }

                countdown.cancelByPlayer(player);
            }
        });

        register("debugCollection", (player, args) -> {
            final CosmeticCollection collection = Enums.byName(CosmeticCollection.class, args[0]);

            if (collection == null) {
                Message.error(player, "Invalid collection.");
                return;
            }

            Message.info(player, collection.getItems().toString());
        });

        register(new SimplePlayerAdminCommand("skin") {

            private final String textureUrl = "https://textures.minecraft.net/texture/";

            @Override
            protected void execute(Player player, String[] args) {
                final String arg = getArgument(args, 0).toString();

                if (arg.equalsIgnoreCase("reset")) {
                    PlayerSkin.reset(player);
                    Message.success(player, "Reset your skin!");
                    return;
                }

                final String url = arg.contains(textureUrl) ? arg : textureUrl + arg;
                final byte[] encoded = Base64.getEncoder().encode(url.getBytes(StandardCharsets.UTF_8));

                Debug.info(encoded);

                new PlayerSkin(new String(encoded), "").apply(player);
                Message.success(player, "Set your skin!");
            }
        });

        register("debugCrate", (player, args) -> {
            final Crates crate = Enums.byName(Crates.class, args.length == 0 ? "null" : args[0]);

            if (crate == null) {
                Message.error(player, "Cannot find crate named {}.", args[0]);
                return;
            }

            Message.info(player, crate.getCrate().toString());
        });

        register(new SimplePlayerAdminCommand("testOrbiting") {
            private Orbiting orbiting;

            @Override
            protected void execute(Player player, String[] args) {
                if (orbiting == null) {
                    orbiting = new Orbiting(3, Material.ARROW) {
                        @Override
                        public @Nonnull Location getAnchorLocation() {
                            return player.getLocation();
                        }
                    };

                    orbiting.setMatrix(
                            0.5000f,
                            0.5000f,
                            0.7071f,
                            0.0000f,
                            -0.7071f,
                            0.7071f,
                            -0.0000f,
                            0.0000f,
                            -0.5000f,
                            -0.5000f,
                            0.7071f,
                            0.0000f,
                            0.0000f,
                            0.0000f,
                            0.0000f,
                            1.0000f
                    );
                    orbiting.addMissing(player.getLocation());
                    orbiting.runTaskTimer(0, 1);

                    Chat.sendMessage(player, "&aSpawned!");
                    return;
                }

                switch (args[0].toLowerCase()) {
                    case "delete" -> {
                        orbiting.removeAll();
                        orbiting = null;

                        Chat.sendMessage(player, "&cDeleted!");
                    }
                    case "remove" -> {
                        orbiting.remove();

                        Chat.sendMessage(player, "&aRemoved!");
                    }
                    case "add" -> {
                        orbiting.add(player.getLocation());

                        Chat.sendMessage(player, "&aAdded!");
                    }
                    case "tp" -> {
                        orbiting.forEach(stand -> {
                            stand.teleport(stand.getLocation().add(0, 1, 0));
                        });

                        Chat.sendMessage(player, "&aTeleported!");
                    }
                }
            }
        });

        register("dumpEntities", (player, args) -> {
            final Set<GameEntity> entities = CF.getEntities();

            Chat.sendMessage(player, "&aEntity Dump (%s)", entities.size());

            boolean lighterColor = true;
            for (GameEntity entity : entities) {
                final String className = entity.getClass().getSimpleName();
                final String name = entity.getName();

                Chat.sendMessage(player, (lighterColor ? "&b " : "&3 ") + className + ":" + name);
                lighterColor = !lighterColor;

                // Glow duh
                if (entity instanceof LivingGameEntity livingGameEntity) {
                    livingGameEntity.addPotionEffect(PotionEffectType.GLOWING, 60, 1);
                }
            }
        });

        final PlayerSkin bloodfiendSkin = new PlayerSkin(
                "ewogICJ0aW1lc3RhbXAiIDogMTY2MTQwMDI0MTc2NiwKICAicHJvZmlsZUlkIiA6ICI4YTg3NGJhNmFiZDM0ZTc5OTljOWM1ODMwYWYyY2NmNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZXphMTExIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzVhYTI5ZWE5NjE3NTdkYzNjOTBiZmFiZjMwMmM1YWJlOWQzMDhmYjRhN2QzODY0ZTU3ODhhZDJjYzkxNjBhYTIiCiAgICB9CiAgfQp9",
                "u7oD4NYj9J7UzMV/GZ3oScp3E6ci7+YI3DsDlTzVfsHKB5yWNEyZPttL09dMDWyJY1kdC8hsK8i5xhF5BaC/2pj/f3SNndzkrflEYrwUr8/1GVXpejIEVpb+SNqImpjsxWY3bLVQHaQ47WjMzvfrQ/gaEMKp3vDmjqST4gWKPxyk6hEHAudA1evE95QSjKX+ayMc822WQPOlPsqcFIZ/f/HYivYl9FQ4HbSyRfK2iI3Ibb0Mwg7BDcJuvkxdnIpkwBz1Hu3SH77dcpXZtLvIBc7dy41zJOMhUzyqkFFVrid5GvgTgb2o+iJ9mSNfVxN9khpG2q15lofdfIseijpq3QP2rAhdl3uX7DqT/CzOzfXP/9FGQaGuYySkNRlbt1WLfWJN9sHWK/jyz1nhV+JwJvwg/uV4Cor9q1jr01cv/FsWIUwSHLnXndIOEileCKnqlo6G/FtTU4Rgd1C5CryBUhY1WMc+HPk38wmWo6HzNOlhT1HltiPjb4kpSUP+vz5LTtplOqwomw/XBp/wuXuS2ijzCVo6lovtUzra5lsGa9EijHPreXt2dEHy68bTZBt2Os4BeWCMTz58d4wvSvC/hHNXdd/asx1CcW288HFxWRxoNLLawanDILCZLdRln4MwlGP1IruOuK0wJOkP3kxqHJdCL51psBPWDpPTzW0VC9c="
        );

        register("testSetSkin", (player, args) -> {
            if (args.length > 0 && args[0].equals("reset")) {
                PlayerSkin.reset(player);
                Chat.sendMessage(player, "&eReset!");
                return;
            }

            bloodfiendSkin.apply(player);
            Chat.sendMessage(player, "&aDone!");
        });

        register(new SimplePlayerAdminCommand("testLine") {
            @Override
            protected void execute(Player player, String[] strings) {
                final Location start = player.getLocation();
                final Location end = player.getTargetBlockExact(100).getLocation().add(0.5d, 0.5d, 0.5d);

                final double step = 0.25d;
                final double distance = start.distance(end);
                final Vector vector = end.clone().subtract(start).toVector().normalize().multiply(step);

                final Location location = start.clone();

                for (double d = 0.0d; d < distance; d += step) {
                    final double traveled = d / distance;
                    final double y = Math.sin(traveled * Math.PI * 3);

                    location.add(vector);
                    location.add(0, y, 0);

                    PlayerLib.spawnParticle(location, Particle.VILLAGER_HAPPY, 1);
                    location.subtract(0, y, 0);
                }

                Chat.sendMessage(player, "&aDrawn!");
            }
        });

        register("testAddSucculence", (player, args) -> {
            final Bloodfiend bloodfiend = Heroes.BLOODFIEND.getHero(Bloodfiend.class);
            final BloodfiendData data = bloodfiend.getData(player);

            data.addSucculence(CF.getPlayer(player));
            Chat.sendMessage(player, "&aDone!");
        });

        register(new SimplePlayerAdminCommand("debugCooldown") {
            @Override
            protected void execute(Player player, String[] args) {
                final Cooldown cooldown = getArgument(args, 0).toEnum(Cooldown.class);
                final long duration = getArgument(args, 1).toLong();

                if (cooldown == null) {
                    Message.Error.INVALID_ENUMERABLE_ARGUMENT.send(player, Arrays.toString(Cooldown.values()));
                    return;
                }

                final GamePlayer gamePlayer = CF.getPlayer(player);

                if (gamePlayer == null) {
                    Message.error(player, "&cCannot use outside a game.");
                    return;
                }

                if (duration > 0) {
                    gamePlayer.startCooldown(cooldown, duration);
                    Message.success(player, "Started cooldown!");
                    return;
                }

                final CooldownData data = gamePlayer.getCooldown().getData(cooldown);

                if (data == null) {
                    Message.error(player, "&cYou don't have this cooldown!");
                    return;
                }

                player.sendMessage(data.toString());
            }
        });

        register(new SimplePlayerAdminCommand("velocty") {
            @Override
            protected void execute(Player player, String[] args) {
                final double x = getArgument(args, 0).toDouble();
                final double y = getArgument(args, 1).toDouble();
                final double z = getArgument(args, 2).toDouble();

                if (x == 0 && y == 0 && z == 0) {
                    Message.error(player, "At least one vector must be positive!");
                    return;
                }

                player.setVelocity(new Vector(x, y, z));
                player.sendMessage(ChatColor.GREEN + "Whoosh!");
            }
        });

        register("testTransformationRotation", (player, args) -> {
            if (args.length != 8) {
                Chat.sendMessage(
                        player,
                        "&cInvalid usage! /testTransformationRotation (double0) (double1) (double2) (double3) (double4) (double5) (double6) (double7)"
                );
                return;
            }

            final double a = Validate.getDouble(args[0]);
            final double b = Validate.getDouble(args[1]);
            final double c = Validate.getDouble(args[2]);
            final double d = Validate.getDouble(args[3]);

            final double e = Validate.getDouble(args[4]);
            final double f = Validate.getDouble(args[5]);
            final double g = Validate.getDouble(args[6]);
            final double h = Validate.getDouble(args[7]);

            Entities.ITEM_DISPLAY.spawn(player.getLocation(), self -> {
                self.setItemStack(ItemBuilder.playerHeadUrl("2c8c8f382667bf59f164106849c00e6dfd9ad00a72670b9de99589e4dcd00900").asIcon());
                self.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
                self.setTransformation(new Transformation
                        (
                                new Vector3f(0, 0, 0),
                                new Quaternionf(a, b, c, d),
                                new Vector3f(1, 1, 1),
                                new Quaternionf(e, f, g, h)
                        ));

                self.setCustomName("(%s, %s, %s, %s) (%s, %s, %s, %s)".formatted(a, b, c, d, e, f, g, h));
                self.setCustomNameVisible(true);
            });

            Chat.sendMessage(player, "&aSpawned!");
        });

        register("setCdMultiplier", (player, args) -> {
            final GamePlayer gamePlayer = GamePlayer.getExistingPlayer(player);

            if (gamePlayer == null) {
                Chat.sendMessage(player, "&cNot in a game.");
                return;
            }

            final double modifier = Validate.getDouble(args[0]);
            gamePlayer.setCooldownModifier(modifier);

            Chat.sendMessage(player, "&aSet cooldown modifier to " + modifier);
        });

        register(new SimplePlayerAdminCommand("simulateDeathMessage") {
            @Override
            protected void execute(Player player, String[] strings) {
                final EnumDamageCause cause = getArgument(strings, 0).toEnum(EnumDamageCause.class);
                final double distance = getArgument(strings, 1).toDouble();
                final GamePlayer gamePlayer = CF.getPlayer(player);

                final String format = cause.getRandomIfMultiple().format(gamePlayer, gamePlayer, distance);
                Chat.sendMessage(player, ChatColor.RED + format);
            }

            @Nullable
            @Override
            protected List<String> tabComplete(CommandSender sender, String[] args) {
                return completerSort(EnumDamageCause.values(), args);
            }
        });

        register("recipes", (player, args) -> {
            if (args.length == 0) {
                Chat.sendMessage(player, "&cMissing argument, either 'reset' or 'clear'.");
                return;
            }

            final String arg = args[0];

            if (arg.equalsIgnoreCase("reset")) {
                Bukkit.resetRecipes();
                Chat.broadcast("&aReset recipes!");
            }
            else if (arg.equalsIgnoreCase("clear")) {
                Bukkit.clearRecipes();
                Chat.broadcast("&aCleared recipes!");
            }
            else {
                Chat.sendMessage(player, "&cInvalid usage!");
            }
        });

        register("dumpBlockNamesCSV", (player, args) -> {
            Runnables.runAsync(() -> {
                final File path = new File(Main.getPlugin().getDataFolder(), "element_types.csv");

                try (FileWriter writer = new FileWriter(path)) {
                    for (Material material : Material.values()) {
                        if (material.isBlock()) {
                            writer.append(material.name().toUpperCase()).append(",").append("NULL");
                            writer.append("\n");
                        }
                    }

                    Runnables.runSync(() -> {
                        Chat.sendMessage(player, "&aDumped into &e%s&a!", path.getAbsolutePath());
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        register("testEternaItemBuilderAddTrim", (player, args) -> {
            player.getInventory()
                    .addItem(ItemBuilder.of(Material.DIAMOND_CHESTPLATE).setArmorTrim(TrimPattern.EYE, TrimMaterial.DIAMOND).asIcon());
        });

        register("getUuidName", (player, args) -> {
            try {
                final UUID uuid = UUID.fromString(args[0]);

                Chat.sendMessage(player, "&a%s belongs to %s", uuid.toString(), Bukkit.getOfflinePlayer(uuid).getName());
            } catch (Exception e) {
                Chat.sendMessage(player, "&cProvide a valid uuid!");
            }
        });

        register(new SimplePlayerAdminCommand("nextTrim") {

            // bro just make this a fucking enum
            private final TrimPattern[] PATTERNS =
                    {
                            TrimPattern.SENTRY,
                            TrimPattern.DUNE,
                            TrimPattern.COAST,
                            TrimPattern.WILD,
                            TrimPattern.WARD,
                            TrimPattern.EYE,
                            TrimPattern.VEX,
                            TrimPattern.TIDE,
                            TrimPattern.SNOUT,
                            TrimPattern.RIB,
                            TrimPattern.SPIRE,
                            TrimPattern.WAYFINDER,
                            TrimPattern.SHAPER,
                            TrimPattern.SILENCE,
                            TrimPattern.RAISER,
                            TrimPattern.HOST
                    };

            @Override
            protected void execute(Player player, String[] strings) {
                final String string = getArgument(strings, 0).toString().toLowerCase();

                switch (string) {
                    case "helmet" -> nextTrim(player, EquipmentSlot.HEAD);
                    case "chestplate", "chest" -> nextTrim(player, EquipmentSlot.CHEST);
                    case "leggings", "legs" -> nextTrim(player, EquipmentSlot.LEGS);
                    case "boots" -> nextTrim(player, EquipmentSlot.FEET);
                    default -> {
                        Chat.sendMessage(player, "&cInvalid argument, accepting: [helmet, chestplate, chest, leggings, legs, boots]");
                    }
                }
            }

            private void nextTrim(Player player, EquipmentSlot slot) {
                final PlayerInventory inventory = player.getInventory();
                final ItemStack item = inventory.getItem(slot);

                if (item == null) {
                    Chat.sendMessage(player, "&cNot a valid item.");
                    return;
                }

                if (!(item.getItemMeta() instanceof ArmorMeta meta)) {
                    Chat.sendMessage(player, "&cCannot apply trim to this piece!");
                    return;
                }

                final ArmorTrim trim = meta.getTrim();
                TrimPattern pattern = trim == null ? PATTERNS[0] : trim.getPattern();

                for (int i = 0; i < PATTERNS.length; i++) {
                    if (PATTERNS[i] == pattern) {
                        pattern = i >= PATTERNS.length + 1 ? PATTERNS[0] : PATTERNS[i + 1];
                        break;
                    }
                }

                meta.setTrim(new ArmorTrim(trim == null ? TrimMaterial.QUARTZ : trim.getMaterial(), pattern));
                item.setItemMeta(meta);

                Chat.sendMessage(player, "&aSet %s pattern.", pattern.getKey().getKey());
            }
        });

        register("readElementTypesCSV", (player, args) -> {
            Runnables.runAsync(() -> {
                final File file = new File(Main.getPlugin().getDataFolder(), "element_types.csv");

                if (!file.exists()) {
                    Chat.sendMessage(player, "&cFile 'element_types.csv' doesn't exists!");
                    return;
                }

                final Map<ElementType, Set<Material>> mapped = Maps.newHashMap();

                try (var reader = new Scanner(file)) {
                    int index = 1;

                    while (reader.hasNextLine()) {
                        final String line = reader.nextLine();
                        final String[] split = line.split(",");

                        final Material material = Enums.byName(Material.class, split[0]);
                        final ElementType elementType = Enums.byName(ElementType.class, split[1]);

                        if (material == null) {
                            Chat.sendMessage(player, "&4ERROR @ %s! &cMaterial %s is invalid!", index, split[0]);
                            reader.close();
                            return;
                        }

                        if (elementType == null) {
                            Chat.sendMessage(player, "&4ERROR @ %s! &cType %s is invalid!", index, split[1]);
                            reader.close();
                            return;
                        }

                        // Don't care about null
                        if (elementType == ElementType.NULL) {
                            continue;
                        }

                        mapped.compute(elementType, (t, set) -> {
                            if (set == null) {
                                set = Sets.newHashSet();
                            }

                            set.add(material);

                            return set;
                        });

                        index++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Write to file
                final File fileOut = new File(Main.getPlugin().getDataFolder(), "element_types.mapped");

                try (var writer = new FileWriter(fileOut)) {
                    for (ElementType type : ElementType.values()) {
                        final Set<Material> materials = mapped.getOrDefault(type, Sets.newHashSet());

                        writer.append("//").append(String.valueOf(type)).append("\n");

                        int index = 0;
                        for (Material material : materials) {
                            if (index++ != 0) {
                                writer.append(",\n");
                            }
                            writer.append("     Material.").append(material.name());
                        }

                        writer.append("\n");
                    }

                    Runnables.runSync(() -> Chat.sendMessage(player, "&aDumped into &e%s&a!", fileOut.getAbsolutePath()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        });

        register("debugLevelUpConstruct", (player, args) -> {
            final Construct construct = Heroes.ENGINEER.getHero(Engineer.class).getConstruct(player);

            if (construct == null) {
                Chat.sendMessage(player, "&cNo construct!");
                return;
            }

            construct.levelUp();
            Chat.sendMessage(player, "&aLevelled up!");
        });

        register(new SimplePlayerCommand("resourcePack") {
            @Override
            protected void execute(Player player, String[] args) {
                final PlayerProfile profile = PlayerProfile.getProfile(player);

                if (profile == null) {
                    Chat.sendMessage(player, "&cCouldn't find your profile! That's a bug you should report.");
                    return;
                }

                profile.promptResourcePack();
            }
        });

        register("testAchievementToast", (player, args) -> {
        });

        register(new SimplePlayerAdminCommand("testSplashText") {

            private SplashText splash;

            @Override
            protected void execute(Player player, String[] args) {
                if (splash == null) {
                    splash = new SplashText(player);
                }
                else if (args.length == 0) {
                    splash.remove();
                    Chat.sendMessage(player, "&aRemoved!");
                    return;
                }

                final int startLine = getArgument(args, 0).toInt();

                splash.create(startLine, Arrays.copyOfRange(args, 1, args.length));

                Chat.sendMessage(player, "&aDone!");
            }
        });

        register(new SimpleAdminCommand("colorMeThis") {
            @Override
            protected void execute(CommandSender sender, String[] args) {
                if (args.length == 0) {
                    Chat.sendMessage(sender, "&cColor you what?");
                    return;
                }

                Chat.sendMessage(sender, "&aThere you go!");
                Chat.sendMessage(sender, Chat.arrayToString(args, 0));
            }
        });

        register(new SimplePlayerAdminCommand("debugSnakeBlock") {

            private BlockDisplay display;

            @Override
            protected void execute(Player player, String[] args) {
                if (display != null) {
                    display.remove();
                }

                final float transformation = getArgument(args, 0).toFloat();

                display = Entities.BLOCK_DISPLAY.spawn(player.getLocation(), self -> {
                    self.setBlock(Material.STONE.createBlockData());
                    self.setTransformation(new Transformation(
                            new Vector3f(transformation, transformation, transformation),
                            new AxisAngle4f(0.0f, 0.0f, 0.0f, 0.0f),
                            new Vector3f(0.25f, 0.25f, 0.25f),
                            new AxisAngle4f(0.0f, 0.0f, 0.0f, 0.0f)
                    ));
                });

                Chat.sendMessage(player, "&aSpawned with %s.", transformation);
            }
        });

        register(new SimplePlayerAdminCommand("displayDamage") {
            @Override
            protected void execute(Player player, String[] args) {
                // displayDamage (damage) (crit)
                final double damage = getArgument(args, 0).toDouble(10.0d);
                final boolean isCrit = getArgument(args, 1).toString().equalsIgnoreCase("true");

                new DamageDisplay(damage, isCrit).display(LocationHelper.getInFront(player.getLocation(), 1.0d));
            }
        });

        register(new SimplePlayerAdminCommand("debugTextDisplayOpaque") {

            private final Set<TextDisplay> set = Sets.newHashSet();

            @Override
            protected void execute(Player player, String[] args) {
                set.forEach(TextDisplay::remove);
                set.clear();

                final Location location = player.getLocation();

                for (var ref = new Object() {
                    byte i = Byte.MIN_VALUE;
                }; ref.i < Byte.MAX_VALUE; ref.i++) {
                    Entities.TEXT_DISPLAY.spawn(location, self -> {
                        self.setBillboard(Display.Billboard.CENTER);
                        self.setSeeThrough(true);
                        self.setTextOpacity(ref.i);
                        self.setText("&a" + ref.i);
                    });

                    location.add(0.0d, 0.25d, 0.0d);
                }

                Chat.sendMessage(player, "&aDone!");
            }
        });

        register(new SimplePlayerAdminCommand("temperStat") {
            @Override
            protected void execute(Player player, String[] args) {
                // $ <stat> <amount> <duration>
                // $ temper [value] [duration]
                final GamePlayer gamePlayer = GamePlayer.getExistingPlayer(player);

                if (gamePlayer == null) {
                    Chat.sendMessage(player, "&cCannot use outside a game.");
                    return;
                }

                final Temper temper = getArgument(args, 0).toEnum(Temper.class);
                final double value = getArgument(args, 1).toDouble(0.2d);
                final int duration = getArgument(args, 2).toInt(100);

                if (temper == null) {
                    Message.error(player, "Invalid temper!");
                    return;
                }

                temper.temper(gamePlayer, value, duration);
                Chat.sendMessage(player, "&aDone!");
            }

            @Nullable
            @Override
            protected List<String> tabComplete(CommandSender sender, String[] args) {
                if (args.length == 1) {
                    return completerSort(Temper.values(), args);
                }

                return null;
            }
        });

        register(new SimplePlayerAdminCommand("simulateSelfDeath") {
            @Override
            protected void execute(Player player, String[] args) {
                final GamePlayer gamePlayer = GamePlayer.getExistingPlayer(player);

                if (gamePlayer == null) {
                    Chat.sendMessage(player, "&cMust be in game to use this.");
                    return;
                }

                gamePlayer.setLastDamager(CF.getPlayer(player));
                gamePlayer.die(true);
            }
        });

        register(new SimplePlayerAdminCommand("testEternaClone") {
            @Override
            protected void execute(Player player, String[] strings) {
                new Cuboid(
                        BukkitUtils.defLocation(-9, 64, -271),
                        BukkitUtils.defLocation(-5, 66, -269)
                ).cloneBlocksTo(BukkitUtils.defLocation(
                        -2,
                        64,
                        -270
                ), true);

                Chat.sendMessage(player, "<color=#359751>Done!</>");
            }
        });

        register(new SimplePlayerAdminCommand("drawCircleAlong") {
            @Override
            protected void execute(Player player, String[] args) {
                final String string = getArgument(args, 0).toString();

                if (!string.equalsIgnoreCase("x") && !string.equalsIgnoreCase("z")) {
                    Chat.sendMessage(player, "&cShould be either along X or Z, not " + string);
                    return;
                }

                final double radius = 3.0d;
                final boolean isX = string.equalsIgnoreCase("x");
                final Location location = player.getLocation();

                for (double d = 0.0d; d < Math.PI * 2; d += Math.PI / 16) {
                    final double x = isX ? Math.sin(d) * radius : 0.0d;
                    final double y = Math.cos(d) * radius;
                    final double z = !isX ? Math.sin(d) * radius : 0.0d;

                    location.add(x, y, z);

                    // Do something
                    player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 1);

                    location.subtract(x, y, z);
                }
            }
        });

        register(new SimpleAdminCommand("ram") {

            private final long GIGABYTE = 1_048_576L;

            @Override
            protected void execute(CommandSender sender, String[] args) {
                Chat.sendMessage(
                        sender,
                        "&6Current Memory Usage: &a%s/%s mb (Max: %s mb)",
                        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / GIGABYTE,
                        Runtime.getRuntime().totalMemory() / GIGABYTE,
                        Runtime.getRuntime().maxMemory() / GIGABYTE
                );

                final OperatingSystemMXBean mx = ManagementFactory.getOperatingSystemMXBean();

                if (mx instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                    double cpuLoad = sunOsBean.getCpuLoad();
                    Chat.sendMessage(sender, "&6CPU Load: &a%.1f%%" + (cpuLoad <= 0.0d ? " &cNot supported" : ""), cpuLoad * 100);
                }
            }
        });

        register(new SimplePlayerAdminCommand("gradient") {
            @Override
            protected void execute(Player player, String[] args) {
                // gradient #FROM #TO BOLD<bool> ...Text

                final Color from = Color.decode(getArgument(args, 0).toString());
                final Color to = Color.decode(getArgument(args, 1).toString());
                final boolean isBold = getArgument(args, 2).toString().equalsIgnoreCase("true");

                final String string = Chat.arrayToString(args, 3);
                final Gradient gradient = new Gradient(string);

                if (isBold) {
                    gradient.makeBold();
                }

                Chat.sendMessage(player, "&a&lOutput:");

                Chat.sendMessage(player, "&aLinear:");
                Chat.sendMessage(player, "");
                Chat.sendMessage(player, gradient.rgb(from, to, Interpolators.LINEAR));
                Chat.sendMessage(player, "");
                copyCommand(
                        player,
                        "new Gradient(%s).rgb(%s, %s, Interpolators.LINEAR)",
                        isBold,
                        string,
                        colorToString(from),
                        colorToString(to)
                );

                Chat.sendMessage(player, "&aQuadratic Fast -> Slow:");
                Chat.sendMessage(player, "");
                Chat.sendMessage(player, gradient.rgb(from, to, Interpolators.QUADRATIC_FAST_TO_SLOW));
                Chat.sendMessage(player, "");
                copyCommand(
                        player,
                        "new Gradient(%s).rgb(%s, %s, Interpolators.QUADRATIC_FAST_TO_SLOW)",
                        isBold,
                        string,
                        colorToString(from),
                        colorToString(to)
                );

                Chat.sendMessage(player, "&aQuadratic Slow -> Fast:");
                Chat.sendMessage(player, "");
                Chat.sendMessage(player, gradient.rgb(from, to, Interpolators.QUADRATIC_SLOW_TO_FAST));
                Chat.sendMessage(player, "");
                copyCommand(
                        player,
                        "new Gradient(%s).rgb(%s, %s, Interpolators.QUADRATIC_SLOW_TO_FAST)",
                        isBold,
                        string,
                        colorToString(from),
                        colorToString(to)
                );

            }

            private String colorToString(Color color) {
                return "%s, %s, %s".formatted(color.getRed(), color.getGreen(), color.getBlue());
            }

            private void copyCommand(Player player, String command, boolean bold, Object... format) {
                if (bold) {
                    command = command.replaceFirst("\\.", ".makeBold().");
                }

                command = command.formatted(format);

                Chat.sendClickableHoverableMessage(
                        player,
                        LazyEvent.copyToClipboard(command),
                        LazyEvent.showText("&eClick to copy code"),
                        "&e&lCLICK TO COPY"
                );
            }
        });


        register(new SimplePlayerAdminCommand("debugDamageData") {
            @Override
            protected void execute(Player player, String[] strings) {
                final LivingGameEntity targetEntity = Collect.targetEntity(player, 20.0d, 0.9d, e -> e.isNot(player));

                if (targetEntity == null) {
                    Chat.sendMessage(player, CF.getPlayer(player).getData());
                    return;
                }

                Chat.sendMessage(player, targetEntity.getData());
            }
        });

        register(new SimplePlayerAdminCommand("readSigns") {

            private Queue<Sign> queue;

            @Override
            protected void execute(Player player, String[] strings) {
                final NamedSignReader reader = new NamedSignReader(player.getWorld());

                if (queue == null) {
                    queue = reader.readAsQueue();
                    Chat.sendMessage(player, "&aFound %s signs. Use the command again to teleport to the next one.", queue.size());
                    return;
                }

                if (queue.peek() == null) {
                    Chat.sendMessage(player, "&cNo more signs!");
                    queue = null;
                    return;
                }

                final Sign sign = queue.poll();
                final String line = sign.getLine(0).replace("[", "").replace("]", "").toUpperCase();
                final Location location = sign.getLocation().add(0.5d, 0.0d, 0.5d); // center it
                final String locationString = BukkitUtils.locationToString(location);

                Chat.sendMessage(player, "");
                Chat.sendMessage(player, "&aNext: &l" + line.toUpperCase());

                Chat.sendClickableHoverableMessage(
                        player,
                        LazyEvent.runCommand("tp %s %s %s", location.getX(), location.getY(), location.getZ()),
                        LazyEvent.showText("&eClick to teleport!"),
                        "&6&lCLICK TO TELEPORT"
                );

                Chat.sendClickableHoverableMessage(
                        player,
                        LazyEvent.copyToClipboard("%s %s %s".formatted(location.getX(), location.getY(), location.getZ())),
                        LazyEvent.showText("&eClick to copy coordinates!"),
                        "&6&lCLICK TO COPY COORDINATES"
                );

                Chat.sendClickableHoverableMessage(
                        player,
                        LazyEvent.copyToClipboard(line.equalsIgnoreCase("spawn")
                                ? "addLocation(%s, 0, 0)".formatted(locationString)
                                : "addPackLocation(PackType.%s, %s)".formatted(line, locationString)),
                        LazyEvent.showText("&eClick to copy code!"),
                        "&6&lCLICK TO COPY CODE"
                );

                Chat.sendClickableHoverableMessage(
                        player,
                        LazyEvent.runCommand(getName()),
                        LazyEvent.showText("&aClick to show next sign!"),
                        "&a&lNEXT"
                );
            }

        });

        register(new SimpleAdminCommand("listProfiles") {
            @Override
            protected void execute(CommandSender commandSender, String[] strings) {
                Manager.current().listProfiles();
            }
        });

        register(new CFCommand("testHologramHeights", PlayerRank.ADMIN) {

            private List<Hologram> holograms = Lists.newArrayList();
            private GameTask task;

            @Override
            protected void execute(Player player, String[] args, PlayerRank rank) {
                final double offsetY = getArgument(args, 0).toDouble(0.0d);
                final Location absoluteLocation = player.getLocation();
                final Location location = player.getLocation().subtract(0, offsetY, 0);

                if (task != null) {
                    task.cancel();
                    task = null;
                }

                holograms.forEach(Hologram::destroy);
                holograms.clear();

                task = new GameTask() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 10; i++) {
                            absoluteLocation.add(i, 0, 0);
                            PlayerLib.spawnParticle(absoluteLocation, Particle.VILLAGER_HAPPY, 1);
                            absoluteLocation.subtract(i, 0, 0);
                        }
                    }
                }.runTaskTimer(0, 1);

                for (int i = 0; i < 10; i++) {
                    location.add(1, 0, 0);

                    holograms.add(new Hologram()
                            .setLines(createArray(i + 1))
                            .create(location)
                            .showAll());
                }

                Chat.sendMessage(player, "&aDone!");
            }

            private String[] createArray(int size) {
                final String[] array = new String[size];

                for (int i = 0; i < size; i++) {
                    array[i] = "test" + i;
                }

                return array;
            }

        });

        register(new CFCommand("testChestAnimation", PlayerRank.ADMIN) {
            @Override
            protected void execute(Player player, String[] args, PlayerRank rank) {
                // <command> (open, close)
                final String argument = getArgument(args, 0).toString().toLowerCase();

                final Block targetBlock = player.getTargetBlockExact(10);

                if (targetBlock == null) {
                    Message.error(player, "Not looking at a block.");
                    return;
                }

                final Material type = targetBlock.getType();

                if (type != Material.CHEST && type != Material.TRAPPED_CHEST && type != Material.ENDER_CHEST) {
                    Message.error(player, "Not looking at a chest!");
                    return;
                }

                switch (argument) {
                    case "open" -> {
                        Utils.playChestAnimation(targetBlock, true);
                        Message.success(player, "Playing open animation.");
                    }

                    case "close" -> {
                        Utils.playChestAnimation(targetBlock, false);
                        Message.success(player, "Playing close animation.");
                    }

                    default -> Message.error(player, "Invalid argument! Must be either 'open' or 'close'!");
                }
            }
        });

        register(new SimplePlayerAdminCommand("respawnGamePacks") {
            @Override
            protected void execute(Player player, String[] args) {
                final IGameInstance instance = Manager.current().getGameInstance();

                if (instance == null) {
                    Chat.sendMessage(player, "&cNo game instance.");
                    return;
                }

                final IntInt i = new IntInt();

                instance.getMap().getMap().getGamePacks().forEach(pack -> {
                    pack.getActivePacks().forEach(activePack -> {
                        i.increment();
                        activePack.createEntity();
                    });
                });

                Chat.sendMessage(player, "&aRespawned %s packs.", i);
            }
        });

        register(new SimplePlayerAdminCommand("riptide") {

            private HumanNPC npc;
            private org.bukkit.entity.Entity entity;

            @Override
            protected void execute(Player player, String[] args) {
                if (npc != null) {
                    entity.remove();
                    entity = null;
                    npc.remove();
                    npc = null;
                    Chat.sendMessage(player, "&aRemoved!");
                    return;
                }

                final Location location = player.getLocation();
                location.setYaw(90f);
                location.setPitch(90f);

                final Entities<? extends org.bukkit.entity.Entity> toSpawn = Entities.byName(args[0]);

                if (toSpawn == null) {
                    Chat.sendMessage(player, "&cInvalid entity = " + args[0]);
                    return;
                }

                final HumanNPC npc = new HumanNPC(location.clone().subtract(0.0, GVar.get("riptide", 1d), 0.0), "", player.getName());
                entity = toSpawn.spawn(location.clone().subtract(0.0, GVar.get("riptide2", 0.0d), 0.0d), self -> {
                    self.setGravity(false);
                });

                npc.bukkitEntity().setInvisible(true);
                npc.setCollision(false);
                npc.showAll();

                npc.setDataWatcherByteValue(8, (byte) 0x04);
                npc.updateDataWatcher();

                this.npc = npc;
                Chat.sendMessage(player, "&aSpawned!");

                new GameTask() {
                    @Override
                    public void run() {
                        if (entity == null) {
                            this.cancel();
                            return;
                        }

                        final Location entityLocation = entity.getLocation();
                        entityLocation.add(0.0, GVar.get("riptide3", 0.0d), 0.0);
                        entityLocation.setYaw(90f);
                        entityLocation.setPitch(90f);

                        npc.teleport(entityLocation);
                    }
                }.runTaskTimer(0, 1);
            }
        });

        register(new SimplePlayerAdminCommand("stopglow") {
            @Override
            protected void execute(Player player, String[] strings) {
                final Pig spawn = Entities.PIG.spawn(player.getLocation());

                Glowing.glowInfinitly(spawn, ChatColor.GOLD, player);

                Runnables.runLater(() -> {
                    Glowing.stopGlowing(player, spawn);
                    Chat.sendMessage(player, "Stop glowing");
                }, 60);
            }
        });

        register(new SimplePlayerAdminCommand("testTitleAnimation") {
            @Override
            protected void execute(Player player, String[] args) {
                new TitleAnimation();
            }
        });

        register(new SimpleAdminCommand("debugTeams") {
            @Override
            protected void execute(CommandSender sender, String[] args) {
                for (GameTeam team : GameTeam.values()) {
                    Debug.info("%s = %s", team.getName(), team.listMembers());
                }
            }
        });

        register(new SimplePlayerAdminCommand("dumpBlockHardness") {
            @Override
            protected void execute(Player player, String[] strings) {
                Runnables.runAsync(() -> {
                    final Map<String, Float> hardness = Maps.newHashMap();

                    for (Material material : Material.values()) {
                        if (material.isBlock()) {
                            hardness.put(material.name(), material.getHardness());
                        }
                    }

                    final LinkedHashMap<String, Float> sorted = hardness.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

                    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    final String json = gson.toJson(sorted);
                    final File path = new File(Main.getPlugin().getDataFolder(), "hardness.json");

                    hardness.clear();
                    sorted.clear();

                    try (FileWriter writer = new FileWriter(path)) {
                        writer.write(json);

                        Runnables.runSync(() -> {
                            Chat.sendMessage(player, "&aDumped into &e%s&a!", path.getAbsolutePath());
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        register(new SimplePlayerAdminCommand("resetDaily") {
            @Override
            protected void execute(Player player, String[] strings) {
                final PlayerDatabase database = PlayerDatabase.getDatabase(player);
                database.dailyRewardEntry.setLastDaily(System.currentTimeMillis() - DailyReward.MILLIS_WHOLE_DAY);

                Chat.sendMessage(player, "&aReset daily!");
            }
        });

        register(new SimplePlayerAdminCommand("setAttackSpeed") {
            @Override
            protected void execute(Player player, String[] args) {
                final double value = getArgument(args, 0).toDouble();

                if (value == 0.0d) {
                    Chat.sendMessage(player, "&cInvalid value.");
                    return;
                }

                final ItemStack item = player.getInventory().getItemInMainHand();
                final ItemMeta meta = item.getItemMeta();

                if (meta == null) {
                    Chat.sendMessage(player, "&cItem has no meta.");
                    return;
                }

                meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
                meta.addAttributeModifier(
                        Attribute.GENERIC_ATTACK_SPEED,
                        new AttributeModifier("SPEED", value, AttributeModifier.Operation.ADD_NUMBER)
                );

                item.setItemMeta(meta);

                Chat.sendMessage(player, "&aSuccess! Set attack speed to " + value);
            }
        });

        register(new SimplePlayerAdminCommand("skullBlockFace") {
            @Override
            protected void execute(Player player, String[] strings) {
                final Block block = player.getTargetBlockExact(100);

                if (block == null) {
                    Chat.sendMessage(player, "&cNo block in sight.");
                    return;
                }

                final BlockState state = block.getState();
                if (!(state instanceof Skull skull)) {
                    Chat.sendMessage(player, "&cTarget block is not a skull.");
                    return;
                }

                Chat.sendMessage(player, "&aBlock Face: &l" + skull.getRotation().name());
            }
        });

        register(new SimplePlayerAdminCommand("getTextBlockTestItem") {
            @Override
            protected void execute(Player player, String[] strings) {
                player.getInventory().addItem(ItemBuilder.of(Material.FEATHER).addTextBlockLore("""
                        This is a text block lore test, and this should be the first paragraph.
                                                        
                        &a;;Where this is the second one, and it's also all green!
                                                        
                                 
                        Two paragraphs, wow!
                                                        
                        &c;;And I know your name, %s!
                        """, player.getName()).asIcon());
            }
        });

        register(new SimplePlayerAdminCommand("spawnWither") {

            @Override
            protected void execute(Player player, String[] args) {
                if (args.length != 3) {
                    Chat.sendMessage(player, "&cForgot (from:Int), (to:Int) and (speed:Long).");
                    return;
                }

                final int from = Validate.getInt(args[0]);
                final int to = Validate.getInt(args[1]);
                final long speed = Validate.getLong(args[2]);

                new AnimatedWither(LocationHelper.getInFront(player.getLocation(), 6)) {

                    @Override
                    public void onInit(@Nonnull Wither wither) {
                        wither.setSilent(true);
                    }

                    @Override
                    public void onStart() {
                        Chat.sendMessage(player, "&aStarted %s->%s with speed %s", from, to, speed);
                    }

                    @Override
                    public void onStop() {
                        Chat.sendMessage(player, "&aStopped");
                        doLater(wither::remove, 60);
                    }

                    @Override
                    public void onTick(int tick) {
                        Chat.sendMessage(player, "&a>>" + getInvul());
                    }
                }.startAnimation(from, to, speed);
            }

        });

        register(new SimplePlayerAdminCommand("asyncDbTest") {

            private final Document FILTER = new Document("_dev", "hapyl");
            Document document;

            @Override
            protected void execute(Player player, String[] args) {
                final MongoCollection<Document> collection = getPlugin().database.getPlayers();
                document = collection.find(FILTER).first();

                if (document == null) {
                    document = new Document(FILTER);
                    collection.insertOne(document);
                }

                if (args.length >= 1) {
                    final String arg0 = args[0];

                    if (arg0.equalsIgnoreCase("dump")) {
                        document.forEach((k, v) -> {
                            Debug.info("%s = %s", k, v);
                        });
                        return;
                    }

                    if (args.length >= 2) {
                        final String arg1 = args[1];

                        if (arg0.equalsIgnoreCase("get")) {
                            final String get = document.get(arg1, "null");

                            Chat.sendMessage(player, "&e%s = &6%s", arg1, get);
                        }
                        else if (arg0.equalsIgnoreCase("set")) {
                            if (args.length < 3) {
                                Chat.sendMessage(player, "Forgot the value, stupid.");
                                return;
                            }

                            final String toSet = args[2];

                            Runnables.runAsync(() -> {
                                document = collection.findOneAndUpdate(document, Updates.set(arg1, toSet));
                                Chat.sendMessage(player, "&aSet and update %s.", toSet);
                            });
                        }
                    }

                    return;
                }

                Chat.sendMessage(player, "&cInvalid usage, idiot.");
            }
        });

        register(new SimplePlayerAdminCommand("dropDatabase") {
            @Override
            protected void execute(Player player, String[] strings) {
                final Database database = Main.getPlugin().getDatabase();

                if (!database.isDevelopment()) {
                    Chat.sendMessage(player, "&cCannot drop PROD database!");
                    return;
                }

                final MongoDatabase mongoDatabase = database.getDatabase();

                for (String string : mongoDatabase.listCollectionNames()) {
                    mongoDatabase.getCollection(string).deleteMany(new Document());
                }

                Chat.sendMessage(player, "&aDropped database!");

                // Reload player database
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    PlayerDatabase.getDatabase(onlinePlayer).load();
                }
            }
        });

        register(new SimplePlayerAdminCommand("spawnme") {

            HumanNPC npc;

            @Override
            protected void execute(Player player, String[] strings) {
                if (npc != null) {

                    if (strings.length > 0) {
                        Chat.sendMessage(player, "&aApplying skin...");
                        npc.setSkinAsync(strings[0]);
                        return;
                    }

                    npc.remove();
                    npc = null;
                    Chat.sendMessage(player, "&aRemoved!");
                    return;
                }

                npc = new HumanNPC(player.getLocation(), player.getName(), player.getName());
                npc.setLookAtCloseDist(5);
                npc.addDialogLine("Hello {player}, my name is {name} and I'm here as a test!", 40);
                npc.addDialogLine("I'm located at {location}", 20);
                npc.addDialogLine("That's it then, bye &c❤");
                npc.setInteractionDelay(60);

                npc.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                npc.setPose(NPCPose.CROUCHING);

                npc.show(player);

                Chat.sendMessage(player, "&aSpawned!");
            }
        });

        register(new SimplePlayerAdminCommand("spawnDancingPiglin") {
            @Override
            protected void execute(Player player, String[] args) {
                final Piglin piglin = Entities.PIGLIN.spawn(player.getLocation());
                final Entity minecraftEntity = Reflect.getMinecraftEntity(piglin);

                piglin.setCustomName(new Gradient("Dancing Piglin").rgb(Color.PINK, Color.RED, Interpolators.LINEAR));
                piglin.setCustomNameVisible(true);
                piglin.setImmuneToZombification(true);
                Chat.sendMessage(player, "&aSpawned!");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (piglin.isDead()) {
                            this.cancel();
                            return;
                        }

                        Reflect.setDataWatcherValue(minecraftEntity, DataWatcherType.BOOL, 19, true);
                    }
                }.runTaskTimer(main, 0, 1);
            }
        });

        // these are small shortcuts not feeling creating a class D:

        register(tinyCommand("start", (player, args) -> {
            player.performCommand("cf start " + Chat.arrayToString(args, 0));
        }));

        register(new SimpleAdminCommand("stop") {
            // true -> stop server, false -> stop game instance

            @Override
            protected void execute(CommandSender sender, String[] args) {
                if (sender instanceof ConsoleCommandSender) {
                    Bukkit.dispatchCommand(sender, "minecraft:stop");
                    return;
                }

                final String arg = getArgument(args, 0).toString();
                Bukkit.dispatchCommand(sender, (arg.equalsIgnoreCase("server") || arg.equalsIgnoreCase("s")) ? "minecraft:stop" : "cf stop");

                Chat.sendMessage(sender, "&6&lWARNING &eDeprecated usage of &n/stop&e command, prefer &n/cf stop&e!");
            }
        });

    }

    private SimplePlayerCommand tinyCommand(String name, Action.AB<Player, String[]> action) {
        return new SimplePlayerCommand(name) {
            @Override
            protected void execute(Player player, String[] strings) {
                action.use(player, strings);
            }
        };
    }

    private void register(String name, BiConsumer<Player, String[]> consumer) {
        processor.registerCommand(new SimplePlayerAdminCommand(name) {
            @Override
            protected void execute(Player player, String[] args) {
                consumer.accept(player, args);
            }
        });
    }

    private void register(SimpleCommand command) {
        processor.registerCommand(command);
    }

}
