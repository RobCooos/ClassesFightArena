package me.hapyl.fight.game.talents.storage.knight;

import me.hapyl.fight.game.EnumDamageCause;
import me.hapyl.fight.game.GamePlayer;
import me.hapyl.fight.game.Response;
import me.hapyl.fight.game.talents.Talent;
import me.hapyl.fight.game.task.GameTask;
import me.hapyl.fight.util.Utils;
import me.hapyl.fight.util.displayfield.DisplayField;
import me.hapyl.spigotutils.module.player.PlayerLib;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Spear extends Talent {

    @DisplayField private final double radius = 1.5d;
    @DisplayField private final double damage = 4.0d;

    public Spear() {
        super(
                "Spear",
                "A knight without a spear is not a knight! Use your spear to dash forward and damage opponents on the way."
        );

        setItem(Material.TIPPED_ARROW, builder -> builder.setPotionColor(Color.GRAY));
        setDuration(15);
        setCd(100);
    }

	@Override
	public Response execute(Player player) {
		player.setVelocity(player.getLocation().getDirection().setY(0.0d).multiply(1.5d));

		new GameTask() {
            private int tick = getDuration();

			@Override
			public void run() {
				if (tick < 0) {
                    cancel();
					return;
				}

                Utils.getEntitiesInRange(player.getLocation(), radius).forEach(entity -> {
                    if (entity == player) {
                        return;
                    }

                    GamePlayer.damageEntity(entity, damage, player, EnumDamageCause.ENTITY_ATTACK);
                });

				--tick;
			}
		}.runTaskTimer(0, 1);

		// fx
		PlayerLib.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1.25f);
		return Response.OK;
	}
}
