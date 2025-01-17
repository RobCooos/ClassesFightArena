package me.hapyl.fight.cmds;

import me.hapyl.fight.game.GamePlayer;
import me.hapyl.fight.game.IGamePlayer;
import me.hapyl.spigotutils.module.chat.Chat;
import me.hapyl.spigotutils.module.command.SimplePlayerAdminCommand;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class DebugPlayerCommand extends SimplePlayerAdminCommand {
    public DebugPlayerCommand(String name) {
        super(name);
    }

    @Override
    protected void execute(Player player, String[] args) {
        final IGamePlayer gamePlayer = GamePlayer.getPlayer(player);

        if (!gamePlayer.isReal()) {
            Chat.sendMessage(player, "&cNo game instance found.");
            return;
        }

        Chat.sendMessage(player, "&c&lDEBUG:");

        boolean color = true;
        try {
            for (Field field : gamePlayer.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                String name = field.getName();
                String type = field.getType().getSimpleName();

                final Object valueRaw = field.get(gamePlayer);
                String value = valueRaw == null ? "null" : valueRaw.toString();

                if (Modifier.isStatic(field.getModifiers())) {
                    type = type + " &lSTATIC";
                }

                if (value.contains("@")) {
                    value = "@" + valueRaw.getClass().getSimpleName();
                }

                // name: Type = value
                if (color) {
                    Chat.sendMessage(player, "&f%s: &o%s &f= &f&l%s", name, type, value);
                }
                else {
                    Chat.sendMessage(player, "&e%s: &o%s &e= &e&l%s", name, type, value);
                }

                color = !color;
            }
        } catch (Exception e) {
            Chat.sendMessage(player, "&cError debugging player, see console.");
            e.printStackTrace();
        }
    }
}
