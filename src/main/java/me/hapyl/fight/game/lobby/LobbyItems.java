package me.hapyl.fight.game.lobby;

import me.hapyl.fight.game.Manager;
import me.hapyl.fight.game.cosmetic.Cosmetics;
import me.hapyl.fight.game.cosmetic.Type;
import me.hapyl.fight.game.cosmetic.gadget.Gadget;
import me.hapyl.fight.game.profile.PlayerProfile;
import me.hapyl.fight.gui.HeroSelectGUI;
import me.hapyl.fight.gui.GameManagementGUI;
import me.hapyl.fight.gui.styled.profile.PlayerProfileGUI;
import me.hapyl.eterna.module.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public enum LobbyItems {

    HERO_SELECT(new LobbyItem(Material.TOTEM_OF_UNDYING, 1, "Hero Selector", "Select from arsenal of unique heroes!") {
        @Override
        public void onClick(Player player) {
            new HeroSelectGUI(player);
        }
    }),

    GAME_MANAGEMENT(new LobbyItem(Material.BOOK, 2, "Game Management", "Manage game settings such as selecting maps, modes etc.") {
        @Override
        public void onClick(Player player) {
            new GameManagementGUI(player);
        }
    }),

    PLAYER_PROFILE(new LobbyItem(Material.PLAYER_HEAD, 4, "Profile", "Click to browse your profile!") {
        @Override
        public void onClick(Player player) {
            new PlayerProfileGUI(player);
        }

        @Override
        public void modifyItem(Player player, ItemBuilder builder) {
            builder.setSkullOwner(player.getName());
        }
    }),

    START_GAME(new LobbyItem(Material.CLOCK, 7, "Start Vote", "Click to start a vote to start the game!") {
        @Override
        public void onClick(Player player) {
            final Manager manager = Manager.current();
            final StartCountdown countdown = manager.getStartCountdown();

            if (countdown != null) {
                countdown.cancelByPlayer(player);
                manager.stopStartCountdown(player);
                return;
            }

            manager.createStartCountdown();
        }
    }),
    ;

    private final LobbyItem lobbyItem;

    LobbyItems(LobbyItem lobbyItem) {
        this.lobbyItem = lobbyItem;
    }

    @Nonnull
    public LobbyItem getItem() {
        return lobbyItem;
    }

    public void give(@Nonnull Player player) {
        lobbyItem.give(player);
    }

    public static void giveAll(@Nonnull Player player) {
        for (LobbyItems value : values()) {
            value.give(player);
        }

        // Give gadget
        final Cosmetics selectedGadget = Cosmetics.getSelected(player, Type.GADGET);

        if (selectedGadget != null) {
            if (selectedGadget.getCosmetic() instanceof Gadget gadget) {
                gadget.give(player);
            }
        }

        // Give fast access items
        PlayerProfile.getProfileOptional(player).ifPresent(profile -> {
            profile.getFastAccess().update();
        });

    }
}
