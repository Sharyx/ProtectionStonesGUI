package me.sharyxxx.protectionstonesgui.Utils;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Function;

record ItemPrevention(int maxStackSize, Function<ItemStack, Boolean> condition, int ...slots) {

    public boolean containsSlot(int slot) {
        return Arrays.stream(slots).anyMatch(correspondingSlot -> correspondingSlot == slot);
    }

}
