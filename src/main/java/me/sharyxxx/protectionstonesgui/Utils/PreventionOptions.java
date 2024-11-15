package me.sharyxxx.protectionstonesgui.Utils;


import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PreventionOptions {

    private final FoxInventory foxInventory;
    private final Set<Integer> slots = new HashSet<>();
    private final List<ItemPrevention> itemPreventions = new ArrayList<>();
    private boolean preventBottomInventory = false;

    PreventionOptions(FoxInventory foxInventory) {
        this.foxInventory = foxInventory;
    }

    private Optional<ItemPrevention> getItemPrevention(int slot) {
        return itemPreventions.stream().filter(itemPrevention -> itemPrevention.containsSlot(slot)).findFirst();
    }

    // Interface for FoxInventory

    boolean isSlotPrevented(int slot) {
        return slots.contains(slot);
    }

    boolean isItemPrevented(int slot, ItemStack item) {
        Optional<ItemPrevention> itemPrevention = getItemPrevention(slot);
        return itemPrevention.isPresent() && !itemPrevention.get().condition().apply(item);
    }

    boolean isBottomPrevented() {
        return preventBottomInventory;
    }

    boolean hasMaxStackLimit(int slot) {
        Optional<ItemPrevention> itemPrevention = getItemPrevention(slot);
        return itemPrevention.isPresent() && itemPrevention.get().maxStackSize() != -1;
    }

    int getMaxStackLimit(int slot) {
        return getItemPrevention(slot).map(ItemPrevention::maxStackSize).orElse(-1);
    }

    // API

    public void setAllSlots() {
        slots.clear();
        slots.addAll(IntStream.range(0, foxInventory.getBukkitInventory().getSize()).boxed().collect(Collectors.toSet()));
    }

    public void setNoneSlots() {
        slots.clear();
    }

    public void includeSlots(int ...includedSlots) {
        Arrays.stream(includedSlots).boxed().forEach(slots::add);
    }

    public void excludeSlots(int ...excludedSlots) {
        Arrays.stream(excludedSlots).boxed().forEach(slots::remove);
    }

    public void preventBottomInventory(boolean prevent) {
        this.preventBottomInventory = prevent;
    }

    public void setConditionalSlots(Function<ItemStack, Boolean> condition, int ...slots) {
        setConditionalSlots(-1, condition, slots);
    }

    public void setConditionalSlots(int maxStackSize, Function<ItemStack, Boolean> condition, int ...slots) {
        if(slots.length == 0) return;
        itemPreventions.add(new ItemPrevention(maxStackSize, condition, slots));
    }
}
