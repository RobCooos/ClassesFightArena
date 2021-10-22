package kz.hapyl.fight.cmds;

import kz.hapyl.fight.game.Manager;
import kz.hapyl.fight.game.gamemode.Modes;
import kz.hapyl.fight.gui.ModeSelectGUI;
import kz.hapyl.spigotutils.module.command.SimplePlayerAdminCommand;
import kz.hapyl.spigotutils.module.util.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ModeCommand extends SimplePlayerAdminCommand {

	public ModeCommand(String str) {
		super(str);
		this.setUsage("mode " + Arrays.toString(Modes.values()));
	}

	@Override
	protected void execute(Player player, String[] args) {
		if (args.length == 0) {
			new ModeSelectGUI(player);
			return;
		}

		final Modes mode = Validate.getEnumValue(Modes.class, args[0]);
		if (mode == null) {
			this.sendInvalidUsageMessage(player);
			return;
		}

		Manager.current().setCurrentMode(mode);

	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return completerSort(arrayToList(Modes.values()), args);
	}

}