package me.sharyxxx.protectionstonesgui.Utils;

import me.sharyxxx.protectionstonesgui.ProtectionStonesGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class InventorySchem {

    public static FoxInventory.Builder inventorySchemInit() {
        FoxInventory.Builder inventory = FoxInventory.getBuilder(ProtectionStonesGUI.getInstance(), 6, ChatColor.YELLOW + "ZARZĄDZAJ DZIAŁKAMI").setPrevention();

        for(int i = 0; i<=9; i++) {
            if(i%2==0) {
                inventory.setItem(Material.YELLOW_STAINED_GLASS_PANE, i, "", null);
            } else {
                inventory.setItem(Material.ORANGE_STAINED_GLASS_PANE, i, "", null);
            }
        }

        for(int i = 44; i<=53; i++) {
            if(i%2==0) {
                inventory.setItem(Material.YELLOW_STAINED_GLASS_PANE, i, "", null);
            } else {
                inventory.setItem(Material.ORANGE_STAINED_GLASS_PANE, i, "", null);
            }
        }

        inventory.setItem(Material.ORANGE_STAINED_GLASS_PANE, 17, "", null);
        inventory.setItem(Material.YELLOW_STAINED_GLASS_PANE, 18, "", null);
        inventory.setItem(Material.YELLOW_STAINED_GLASS_PANE, 26, "", null);
        inventory.setItem(Material.ORANGE_STAINED_GLASS_PANE, 27, "", null);
        inventory.setItem(Material.ORANGE_STAINED_GLASS_PANE, 35, "", null);
        inventory.setItem(Material.YELLOW_STAINED_GLASS_PANE, 36, "", null);

        return inventory;
    }
}
