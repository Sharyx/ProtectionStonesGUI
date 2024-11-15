package me.sharyxxx.protectionstonesgui.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class FoxInventory {

    private final PreventionOptions preventionOptions = new PreventionOptions(this);
    private Consumer<Player> onOpenTask = null;
    private Consumer<Player> onCloseTask = null;

    private final JavaPlugin plugin;
    private final Inventory inventory;
    private final HashMap<Integer, Consumer<Player>> leftClickCallbacks = new HashMap<>();
    private final HashMap<Integer, Consumer<Player>> rightClickCallbacks = new HashMap<>();
    private final Listener preventListener = new Listener() {
        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if(!event.getInventory().equals(inventory) || event.getClickedInventory() == null) return;
            if(event.getClickedInventory().equals(inventory)) {
                if(event.isLeftClick() && leftClickCallbacks.containsKey(event.getSlot()))
                    leftClickCallbacks.get(event.getSlot()).accept((Player) event.getWhoClicked());
                if(event.isRightClick() && rightClickCallbacks.containsKey(event.getSlot()))
                    rightClickCallbacks.get(event.getSlot()).accept((Player) event.getWhoClicked());
                if(preventionOptions.isSlotPrevented(event.getSlot())) {
                    event.setCancelled(true);
                    return;
                }
                ItemStack cursor = event.getCursor();
                if(cursor == null || cursor.getType() == Material.AIR) return;
                if(preventionOptions.isItemPrevented(event.getSlot(), cursor)) event.setCancelled(true);
            } else {
                if(preventionOptions.isBottomPrevented()) event.setCancelled(true);
                if(event.isShiftClick() && (event.getCurrentItem() != null || event.getCurrentItem().getType() == Material.AIR) && event.getCurrentItem().getItemMeta() != null) {
                    event.setCancelled(true);
                    ItemStack leftover = addItemPreventive(event.getCurrentItem());
                    event.getClickedInventory().setItem(event.getSlot(), leftover);
                }
            }
        }
        @EventHandler
        public void onDrag(InventoryDragEvent event) {
            if(event.getInventory().equals(inventory)) event.setCancelled(true);
        }
    };
    private final Listener inventoryListener = new Listener() {
        @EventHandler
        public void onOpen(InventoryOpenEvent event) {
            Inventory openedInventory = event.getInventory();
            if(openedInventory == inventory && onOpenTask != null) onOpenTask.accept((Player) event.getPlayer());
        }
        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            Inventory closedInventory = event.getInventory();
            if(closedInventory == inventory && onCloseTask != null) onCloseTask.accept((Player) event.getPlayer());
        }
    };

    private boolean transferItem(ItemStack from, Inventory inv, int slot) {
        if(from.getAmount() == 0) return true;
        ItemStack to = inv.getItem(slot);
        int maxAmount = from.getMaxStackSize();
        int fromAmount = from.getAmount();
        int toAmount = to == null ? 0 : to.getAmount();
        int toTransfer = maxAmount - toAmount;
        if(toTransfer == 0) return false;
        if(toTransfer > fromAmount) toTransfer = fromAmount;
        from.setAmount(fromAmount - toTransfer);
        inv.setItem(slot, new ItemStack(from.getType(), toAmount + toTransfer));
        return from.getAmount() <= 0;
    }

    /**
     * Gets new {@link FoxInventory} builder
     *
     * @param plugin JavaPlugin needed for registering prevention listeners
     * @param rowSize size of the minecraft inventory (in rows)
     * @return new builder
     */
    public static Builder getBuilder(JavaPlugin plugin, int rowSize) {
        return new Builder(plugin, rowSize);
    }

    /**
     * Gets new {@link FoxInventory} builder
     *
     * @param plugin JavaPlugin needed for registering prevention listeners
     * @param rowSize size of the minecraft inventory (in rows)
     * @param title Title of created inventory
     * @return new builder
     */
    public static Builder getBuilder(JavaPlugin plugin, int rowSize, String title) {
        return new Builder(plugin, rowSize, title);
    }

    public FoxInventory(JavaPlugin plugin, int rowSize) {
        this(plugin, rowSize, null);
    }

    public FoxInventory(JavaPlugin plugin, int rowSize, String title) {
        if(rowSize < 1) rowSize = 1;
        if(rowSize > 6) rowSize = 6;
        this.plugin = plugin;
        if(title == null) this.inventory = Bukkit.createInventory(null, rowSize * 9);
        else this.inventory = Bukkit.createInventory(null, rowSize * 9, title);
        Bukkit.getPluginManager().registerEvents(inventoryListener, plugin);
        Bukkit.getPluginManager().registerEvents(preventListener, plugin);
    }

    /**
     * Gets Bukkit inventory
     *
     * @return bukkit inventory
     */
    public Inventory getBukkitInventory() {
        return inventory;
    }

    /**
     * Action to do right before inventory open
     *
     * @param onOpen consumer with player which opened the inventory
     */
    public void onOpen(Consumer<Player> onOpen) {
        onOpenTask = onOpen;
    }

    /**
     * Action to do right before inventory close
     *
     * @param onClose consumer with player which closed the inventory
     */
    public void onClose(Consumer<Player> onClose) {
        onCloseTask = onClose;
    }

    public void setPrevention() {
        preventionOptions.setAllSlots();
        preventionOptions.preventBottomInventory(true);
    }

    public void setPrevention(Consumer<PreventionOptions> configurator) {
        configurator.accept(preventionOptions);
    }

    /**
     * Clears item from inventory from provided slot
     *
     * @param slot item slot to clear
     */
    public void clearSlot(int slot) {
        inventory.setItem(slot, new ItemStack(Material.AIR));
    }

    /**
     * Clears all items from inventory from provided slots
     *
     * @param slots item slots to clear
     */
    public void clearSlots(int ...slots) {
        Arrays.stream(slots).forEach(this::clearSlot);
    }

    /**
     * Clears all items from inventory in provided slot range (from start slot to end slot)
     *
     * @param startSlot start slot (inclusive)
     * @param endSlot end slot (exclusive)
     */
    public void clearSlots(int startSlot, int endSlot) {
        clearSlots(IntStream.range(startSlot, endSlot).toArray());
    }

    public void setItem(Material material, int slot, @Nullable String name, @Nullable List<String> lore) {
        setItem(new ItemStack(material), slot, name, lore, null, null);
    }

    public void setItem(ItemStack item, int slot, @Nullable String name, @Nullable List<String> lore) {
        setItem(item, slot, name, lore, null, null);
    }

    public void setItem(Material material, int slot, @Nullable String name, @Nullable List<String> lore, Consumer<Player> clickCallback) {
        setItem(new ItemStack(material), slot, name, lore, clickCallback, clickCallback);
    }

    public void setItem(ItemStack item, int slot, @Nullable String name, @Nullable List<String> lore, Consumer<Player> clickCallback) {
        setItem(item, slot, name, lore, clickCallback, clickCallback);
    }

    public void setItem(Material material, int slot, @Nullable String name, @Nullable List<String> lore, Consumer<Player> leftClickCallback, Consumer<Player> rightClickCallback) {
        setItem(new ItemStack(material), slot, name, lore, leftClickCallback, rightClickCallback);
    }

    public void setItem(ItemStack item, int slot, @Nullable String name, @Nullable List<String> lore, Consumer<Player> leftClickCallback, Consumer<Player> rightClickCallback) {
        if(item == null) return;
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            if(name != null) meta.setDisplayName(name);
            if(lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        if(leftClickCallback != null) leftClickCallbacks.put(slot, leftClickCallback);
        if(rightClickCallback != null) rightClickCallbacks.put(slot, rightClickCallback);
        inventory.setItem(slot, item);
    }

    public void fill(Material material, int... slots) {
        fill(new ItemStack(material), slots);
    }

    public void fill(ItemStack item, int... slots) {
        Arrays.stream(slots).forEach(slot -> setItem(item, slot, null, null));
    }

    public void fill(Material material, Consumer<Player> clickCallback, int... slots) {
        fill(new ItemStack(material), clickCallback, slots);
    }

    public void fill(ItemStack item, Consumer<Player> clickCallback, int... slots) {
        Arrays.stream(slots).forEach(slot -> setItem(item, slot, null, null, clickCallback));
    }

    public void fill(Material material, Consumer<Player> leftClickCallback, Consumer<Player> rightClickCallback, int... slots) {
        fill(new ItemStack(material), slots);
    }

    public void fill(ItemStack item, Consumer<Player> leftClickCallback, Consumer<Player> rightClickCallback, int... slots) {
        Arrays.stream(slots).forEach(slot -> setItem(item, slot, null, null, leftClickCallback, rightClickCallback));
    }

    // Off builder methods

    /**
     * Sets name to item on provided slot
     *
     * @param slot slot of the item
     * @param itemName name for the item
     */
    public void setNameInItem(int slot, String itemName) {
        if(itemName == null || itemName.equals("")) return;
        ItemStack item = inventory.getItem(slot);
        if(item == null) return;
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return;
        meta.setDisplayName(itemName);
        item.setItemMeta(meta);
    }

    /**
     * Sets lore to item on provided slot
     *
     * @param slot slot of the item
     * @param lore lore for the item
     */
    public void setLoreInItem(int slot, @Nullable List<String> lore) {
        if(lore == null) return;
        ItemStack item = inventory.getItem(slot);
        if(item == null) return;
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return;
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private int firstEmptyPreventive(ItemStack item) {
        ItemStack[] inventory = getBukkitInventory().getStorageContents();
        for(int i = 0; i < inventory.length; i++) {
            if(inventory[i] == null && !preventionOptions.isSlotPrevented(i) && !preventionOptions.isItemPrevented(i, item)) return i;
        }
        return -1;
    }

    private int firstPartialPreventive(ItemStack item) {
        if(item == null) return -1;
        ItemStack[] inventory = getBukkitInventory().getStorageContents();
        ItemStack filteredItem = item.clone();
        for(int i = 0; i < inventory.length; i++) {
            ItemStack cItem = inventory[i];
//            Bukkit.getLogger().info("stackSize" + preventionOptions.getMaxStackLimit(i));
            if(cItem != null && cItem.getAmount() < (preventionOptions.hasMaxStackLimit(i) ? preventionOptions.getMaxStackLimit(i) : cItem.getMaxStackSize()) &&
                cItem.isSimilar(filteredItem) && !preventionOptions.isSlotPrevented(i) && !preventionOptions.isItemPrevented(i, item)
            ) return i;
        }
        return -1;
    }

    public ItemStack addItemPreventive(ItemStack item) {
        if (item == null)
        {
            return null;
        }
        ItemStack leftover = null;

        while(true) {
            int firstPartial = firstPartialPreventive(item);

            if(firstPartial == -1) {
                int firstFree = firstEmptyPreventive(item);

                if(firstFree == -1) {
                    leftover = item;
                    break;
                } else {
                    int maxStackSize = preventionOptions.hasMaxStackLimit(firstFree) ? preventionOptions.getMaxStackLimit(firstFree) : getBukkitInventory().getMaxStackSize();
//                    Bukkit.getLogger().info("stackSize: " + maxStackSize);
                    if(item.getAmount() > maxStackSize) {
                        ItemStack stack = item.clone();
                        stack.setAmount(maxStackSize);
                        getBukkitInventory().setItem(firstFree, stack);
                        item.setAmount(item.getAmount() - maxStackSize);
                    } else {
                        getBukkitInventory().setItem(firstFree, item);
                        break;
                    }
                }
            } else {
                ItemStack partialItem = getBukkitInventory().getItem(firstPartial);

                int amount = item.getAmount();
                int partialAmount = partialItem.getAmount();
                int maxAmount = preventionOptions.hasMaxStackLimit(firstPartial) ? preventionOptions.getMaxStackLimit(firstPartial) : partialItem.getMaxStackSize();
//                Bukkit.getLogger().info("stackSize: " + maxAmount);

                if(amount + partialAmount <= maxAmount) {
                    partialItem.setAmount(amount + partialAmount);
                    getBukkitInventory().setItem(firstPartial, partialItem);
                    break;
                }

                partialItem.setAmount(maxAmount);
                getBukkitInventory().setItem(firstPartial, partialItem);
                item.setAmount(amount + partialAmount - maxAmount);
            }
        }
        return leftover;
    }

    /**
     * Unregisters all listeners connected to this {@link FoxInventory}
     */
    public void unregisterListeners() {
        HandlerList.unregisterAll(preventListener);
        HandlerList.unregisterAll(inventoryListener);
    }

    public static class Builder {

        FoxInventory foxInventory;

        private Builder(JavaPlugin plugin, int rowSize) {
            foxInventory = new FoxInventory(plugin, rowSize);
        }

        private Builder(JavaPlugin plugin, int rowSize, String title) {
            foxInventory = new FoxInventory(plugin, rowSize, title);
        }

        /**
         * @see FoxInventory#onOpen(Consumer)
         */
        public Builder onOpen(Consumer<Player> onOpen) {
            foxInventory.onOpen(onOpen);
            return this;
        }

        /**
         * @see FoxInventory#onClose(Consumer)
         */
        public Builder onClose(Consumer<Player> onClose) {
            foxInventory.onClose(onClose);
            return this;
        }

        /**
         * @see FoxInventory#setPrevention()
         */
        public Builder setPrevention() {
            foxInventory.setPrevention();
            return this;
        }

        /**
         * @see FoxInventory#setPrevention(Consumer)
         */
        public Builder setPrevention(Consumer<PreventionOptions> configurator) {
            foxInventory.setPrevention(configurator);
            return this;
        }

        /**
         * @see FoxInventory#clearSlot(int)
         */
        public Builder clearSlot(int slot) {
            foxInventory.clearSlot(slot);
            return this;
        }

        /**
         * @see FoxInventory#clearSlots(int...)
         */
        public Builder clearSlots(int ...slots) {
            foxInventory.clearSlots(slots);
            return this;
        }

        /**
         * @see FoxInventory#clearSlots(int, int)
         */
        public Builder clearSlots(int startSlot, int endSlot) {
            foxInventory.clearSlots(startSlot, endSlot);
            return this;
        }

        /**
         * @see FoxInventory#setItem(Material, int, String, List)
         */
        public Builder setItem(Material material, int slot, @Nullable String name, @Nullable List<String> lore) {
            foxInventory.setItem(material, slot, name, lore);
            return this;
        }

        /**
         * @see FoxInventory#setItem(ItemStack, int, String, List)
         */
        public Builder setItem(ItemStack item, int slot, @Nullable String name, @Nullable List<String> lore) {
            foxInventory.setItem(item, slot, name, lore);
            return this;
        }

        /**
         * @see FoxInventory#setItem(Material, int, String, List, Consumer)
         */
        public Builder setItem(Material material, int slot, @Nullable String name, @Nullable List<String> lore, Consumer<Player> clickCallback) {
            foxInventory.setItem(material, slot, name, lore, clickCallback);
            return this;
        }

        /**
         * @see FoxInventory#setItem(ItemStack, int, String, List, Consumer)
         */
        public Builder setItem(ItemStack item, int slot, @Nullable String name, @Nullable List<String> lore, Consumer<Player> clickCallback) {
            foxInventory.setItem(item, slot, name, lore, clickCallback);
            return this;
        }

        /**
         * @see FoxInventory#setItem(Material, int, String, List, Consumer, Consumer)
         */
        public Builder setItem(Material material, int slot, @Nullable String name, @Nullable List<String> lore, Consumer<Player> leftClickCallback, Consumer<Player> rightClickCallback) {
            foxInventory.setItem(material, slot, name, lore, leftClickCallback, rightClickCallback);
            return this;
        }

        /**
         * @see FoxInventory#setItem(Material, int, String, List, Consumer, Consumer)
         */
        public Builder setItem(ItemStack item, int slot, @Nullable String name, @Nullable List<String> lore, Consumer<Player> leftClickCallback, Consumer<Player> rightClickCallback) {
            foxInventory.setItem(item, slot, name, lore,  leftClickCallback, rightClickCallback);
            return this;
        }

        /**
         * @see FoxInventory#fill(Material, int...)
         */
        public Builder fill(Material material, int... slots) {
            foxInventory.fill(material, slots);
            return this;
        }

        /**
         * @see FoxInventory#fill(ItemStack, int...)
         */
        public Builder fill(ItemStack item, int... slots) {
            foxInventory.fill(item, slots);
            return this;
        }

        /**
         * @see FoxInventory#fill(Material, Consumer, int...)
         */
        public Builder fill(Material material, Consumer<Player> clickCallback, int... slots) {
            foxInventory.fill(material, clickCallback, slots);
            return this;
        }

        /**
         * @see FoxInventory#fill(ItemStack, Consumer, int...)
         */
        public Builder fill(ItemStack item, Consumer<Player> clickCallback, int... slots) {
            foxInventory.fill(item, clickCallback, slots);
            return this;
        }

        /**
         * @see FoxInventory#fill(Material, Consumer, Consumer, int...)
         */
        public Builder fill(Material material, Consumer<Player> leftClickCallback, Consumer<Player> rightClickCallback, int... slots) {
            foxInventory.fill(material, leftClickCallback, rightClickCallback, slots);
            return this;
        }

        /**
         * @see FoxInventory#fill(ItemStack, Consumer, Consumer, int...)
         */
        public Builder fill(ItemStack item, Consumer<Player> leftClickCallback, Consumer<Player> rightClickCallback, int... slots) {
            foxInventory.fill(item, leftClickCallback, rightClickCallback, slots);
            return this;
        }

        public FoxInventory build() {
            return foxInventory;
        }

        public Inventory buildBukkitInventory() {
            return foxInventory.inventory;
        }
    }

}
