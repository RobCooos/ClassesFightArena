package me.hapyl.fight.game.setting;

import me.hapyl.fight.game.Manager;
import me.hapyl.fight.game.profile.PlayerProfile;
import me.hapyl.spigotutils.module.chat.Chat;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public enum Setting {

    SPECTATE(10, Material.ENDER_EYE, "Spectate", "Whenever you will spectate the game instead of playing it."),
    CHAT_PING(11, Material.GOLD_INGOT, "Chat Notification", "Whenever you will hear a ping in chat if someone mentions you.", true),
    RANDOM_HERO(12, Material.TOTEM_OF_UNDYING, "Always Random Hero", "Whenever you start the game with a random hero every time."),

    SEE_OTHERS_CONTRAIL(14, Material.FIREWORK_ROCKET, "See Others Contrail", "Whenever you will see other players contrails.", true),
    SEE_NOTIFICATIONS(15, Material.PAPER, "See Notifications", "Whenever you will see notifications.", true),

    SHOW_YOURSELF_AS_TEAMMATE(
            28,
            Material.PLAYER_HEAD,
            "Show Yourself as Teammate",
            "Whenever you will see yourself as a teammate in tab list."
    ),

    ;

    private final Material material;
    private final String name;
    private final String info;
    private final boolean def;
    private final int slot;

    Setting(int slot, Material material, String name, String info, boolean def) {
        this.slot = slot;
        this.name = name;
        this.material = material;
        this.info = info;
        this.def = def;
    }

    Setting(int slot, Material material, String name, String info) {
        this(slot, material, name, info, false);
    }

    public int getSlot() {
        return slot;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public boolean getDefaultValue() {
        return def;
    }

    public String getPath() {
        return name();
    }

    public String getInfo() {
        return info;
    }

    public boolean isEnabled(Player player) {
        return PlayerProfile.getOrCreateProfile(player).getDatabase().getSettings().getValue(this);
    }

    public void setEnabled(Player player, boolean flag) {
        if (isEnabled(player) == flag) {
            Chat.sendMessage(player, "&c%s is already %s!", this.getName(), flag ? "enabled" : "disabled");
            return;
        }

        Manager.current().getOrCreateProfile(player).getDatabase().getSettings().setValue(this, flag);
        Chat.sendMessage(player, "%s%s is now %s.", flag ? "&a" : "&c", this.getName(), flag ? "enabled" : "disabled");
    }

    public boolean isDisabled(Player player) {
        return !isEnabled(player);
    }

    public String getPathLegacy() {
        return "setting." + getPath();
    }
}
