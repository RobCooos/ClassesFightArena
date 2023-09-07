package me.hapyl.fight.game.heroes;

import me.hapyl.fight.util.ItemStacks;
import me.hapyl.spigotutils.module.inventory.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Represents armor equipment that can be equipped to entities.
 */
public class Equipment {

    private final static ItemStack NULL_ITEM = ItemStacks.AIR;

    private final ItemStack[] armor;

    public Equipment() {
        this.armor = new ItemStack[4];
    }

    public Equipment setChestplate(Material material, TrimPattern pattern, TrimMaterial trimMaterial) {
        this.armor[1] = new ItemBuilder(material).setArmorTrim(pattern, trimMaterial).cleanToItemSack();
        return this;
    }

    public Equipment setLeggings(Material material, TrimPattern pattern, TrimMaterial trimMaterial) {
        this.armor[2] = new ItemBuilder(material).setArmorTrim(pattern, trimMaterial).cleanToItemSack();
        return this;
    }

    public Equipment setBoots(Material material, TrimPattern pattern, TrimMaterial trimMaterial) {
        this.armor[3] = new ItemBuilder(material).setArmorTrim(pattern, trimMaterial).cleanToItemSack();
        return this;
    }

    /**
     * @deprecated Use {@link Hero#setItem(String)}
     */
    @Deprecated
    public void setTexture(String texture64) {
        this.setHelmet(ItemBuilder.playerHeadUrl(texture64).cleanToItemSack());
    }

    public Equipment setHelmet(int red, int green, int blue) {
        return this.setHelmet(ItemBuilder.leatherHat(Color.fromRGB(red, green, blue)).cleanToItemSack());
    }

    public Equipment setChestplate(int red, int green, int blue) {
        return this.setChestplate(ItemBuilder.leatherTunic(Color.fromRGB(red, green, blue)).cleanToItemSack());
    }

    public Equipment setLeggings(int red, int green, int blue) {
        return this.setLeggings(ItemBuilder.leatherPants(Color.fromRGB(red, green, blue)).cleanToItemSack());
    }

    public Equipment setBoots(int red, int green, int blue) {
        return this.setBoots(ItemBuilder.leatherBoots(Color.fromRGB(red, green, blue)).cleanToItemSack());
    }

    public Equipment setChestplate(int red, int green, int blue, TrimPattern pattern, TrimMaterial material) {
        return this.setChestplate(ItemBuilder.leatherTunic(Color.fromRGB(red, green, blue))
                .setArmorTrim(pattern, material)
                .cleanToItemSack());
    }

    public Equipment setLeggings(int red, int green, int blue, TrimPattern pattern, TrimMaterial material) {
        return this.setLeggings(ItemBuilder.leatherPants(Color.fromRGB(red, green, blue)).setArmorTrim(pattern, material).cleanToItemSack());
    }

    public Equipment setBoots(int red, int green, int blue, TrimPattern pattern, TrimMaterial material) {
        return this.setBoots(ItemBuilder.leatherBoots(Color.fromRGB(red, green, blue)).setArmorTrim(pattern, material).cleanToItemSack());
    }

    @Nonnull
    public ItemStack getHelmet() {
        return this.itemOrNull(this.armor[0]);
    }

    public Equipment setHelmet(ItemStack stack) {
        this.armor[0] = stack;
        return this;
    }

    @Deprecated
    public Equipment setHelmet(Material material) {
        this.armor[0] = new ItemBuilder(material).cleanToItemSack();
        return this;
    }

    public void setHelmet(Color color) {
        this.setHelmet(color.getRed(), color.getGreen(), color.getBlue());
    }

    @Nonnull
    public ItemStack getChestplate() {
        return this.itemOrNull(this.armor[1]);
    }

    public Equipment setChestplate(Material material) {
        this.armor[1] = new ItemBuilder(material).cleanToItemSack();
        return this;
    }

    public Equipment setChestplate(ItemStack stack) {
        this.armor[1] = stack;
        return this;
    }

    public void setChestplate(Color color) {
        this.setChestplate(color.getRed(), color.getGreen(), color.getBlue());
    }

    @Nonnull
    public ItemStack getLeggings() {
        return this.itemOrNull(this.armor[2]);
    }

    public Equipment setLeggings(Material material) {
        this.armor[2] = new ItemBuilder(material).cleanToItemSack();
        return this;
    }

    public Equipment setLeggings(ItemStack stack) {
        this.armor[2] = stack;
        return this;
    }

    public void setLeggings(Color color) {
        this.setLeggings(color.getRed(), color.getGreen(), color.getBlue());
    }

    @Nonnull
    public ItemStack getBoots() {
        return this.itemOrNull(this.armor[3]);
    }

    public Equipment setBoots(Material material) {
        this.armor[3] = new ItemBuilder(material).cleanToItemSack();
        return this;
    }

    public Equipment setBoots(ItemStack stack) {
        this.armor[3] = stack;
        return this;
    }

    public void setBoots(Color color) {
        this.setBoots(color.getRed(), color.getGreen(), color.getBlue());
    }

    public void equip(@Nonnull LivingEntity entity) {
        equipArmor(entity);

        if (entity instanceof Player player1) {
            player1.updateInventory();
        }
    }

    public void equipArmor(LivingEntity entity) {
        final EntityEquipment equipment = entity.getEquipment();

        if (equipment != null) {
            equipment.setHelmet(this.armorOrNull(0));
            equipment.setChestplate(this.armorOrNull(1));
            equipment.setLeggings(this.armorOrNull(2));
            equipment.setBoots(this.armorOrNull(3));
        }
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public void setArmor(ItemStack[] stack) {
        if (stack.length == 4) {
            System.arraycopy(stack, 0, this.armor, 0, this.armor.length);
        }
    }

    private ItemStack create(Material material, Consumer<ItemBuilder> consumer) {
        final ItemBuilder builder = ItemBuilder.of(material);

        consumer.accept(builder);

        // Default names etc
        builder.addTextBlockLore("""
                """);

        return builder.asIcon();
    }

    private ItemStack armorOrNull(int index) {
        return this.itemOrNull(this.armor[index]);
    }

    private ItemStack itemOrNull(ItemStack s) {
        return s == null ? NULL_ITEM : s;
    }

}