package me.hapyl.fight.guesswho.gui;

import me.hapyl.eterna.module.inventory.ItemBuilder;
import me.hapyl.fight.game.color.Color;
import me.hapyl.fight.game.heroes.Hero;
import me.hapyl.fight.guesswho.GuessWhoPlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class GuessWhoSelectionGUI extends GuessWhoGUI {

    public GuessWhoSelectionGUI(GuessWhoPlayer data) {
        super(data, "Select Your Hero");

        openInventory();
    }

    @Nonnull
    @Override
    public ItemBuilder createItem(@Nonnull Hero enumHero) {
        return super.createItem(enumHero).addLore().addLore(Color.BUTTON + "Click to select!");
    }

    @Override
    public void onOpen() {
        data.sendMessage("Select your hero!");
    }

    @Override
    public void onClick(@Nonnull Hero hero) {
        final Player player = data.getPlayer();

        data.setGuessHero(hero);
        data.sendMessage("You have selected %s as your hero!".formatted(hero.getName()));

        player.closeInventory();
        data.getGame().checkSelected();
    }
}
