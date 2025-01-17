package me.hapyl.fight.cmds;

import me.hapyl.fight.game.Manager;
import me.hapyl.spigotutils.module.chat.Chat;
import me.hapyl.spigotutils.module.command.SimplePlayerCommand;
import org.bukkit.entity.Player;

public abstract class LobbyPlayerCommand extends SimplePlayerCommand {
    public LobbyPlayerCommand(String name) {
        super(name);
    }

    protected abstract void onCommand(Player player, String[] strings);

    @Override
    protected final void execute(Player player, String[] strings) {
        if (Manager.current().isGameInProgress()) {
            Chat.sendMessage(player, "&cCannot use this while in game!");
            return;
        }

        onCommand(player, strings);
    }
}
