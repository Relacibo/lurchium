package de.rcbnetwork.lurchium;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ServersideObjectRegistry {
    public static final Map<Identifier, LurchiumItem> ITEMS = new HashMap<>();
    public static final Map<Identifier, Block> BLOCKS = new HashMap<>();

    public static ItemStack createItemStackOf(Identifier id) {
        LurchiumItem item = ITEMS.get(id);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack((Item)item, 1);
        stack.getOrCreateTag().putString("lurchium", id.toString());
        if (item.getCustomName() != null) {
            stack.setCustomName(new LiteralText(item.getCustomName()));
        }
        return stack;
    }
}
