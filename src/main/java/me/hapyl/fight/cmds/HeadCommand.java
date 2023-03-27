package me.hapyl.fight.cmds;

import me.hapyl.spigotutils.module.chat.Chat;
import me.hapyl.spigotutils.module.command.SimplePlayerAdminCommand;
import me.hapyl.spigotutils.module.inventory.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HeadCommand extends SimplePlayerAdminCommand {
    public HeadCommand(String name) {
        super(name);

        setUsage("/head <minecraft-url>");
    }

    @Override
    protected void execute(Player player, String[] args) {
        if (args.length == 0) {
            sendInvalidUsageMessage(player);
            return;
        }

        final ItemStack itemStack = ItemBuilder.playerHeadUrl(args[0])
                .setName("3781c252df29412dd4693848e3856ba5ddf116198e5c8748b28b46a85e983")
                .asIcon();

        player.getInventory().addItem(itemStack);
        Chat.sendMessage(player, "&aAdded head to your inventory!");
    }
}
