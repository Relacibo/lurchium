package de.rcbnetwork.lurchium.mixin;

import de.rcbnetwork.lurchium.Blocks.LurchyButton;
import de.rcbnetwork.lurchium.Items.LurchyButtonBlockItem;
import de.rcbnetwork.lurchium.Items.LurchysClock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Shadow
    Item item;

    @Inject(method = "<init>", at=@At("RETURN"))
    void initializeItemStack(CompoundTag tag, CallbackInfo info) {
        String lurchium = tag.getString("lurchium");
        if (lurchium == null) {
            return;
        }
        if (lurchium.equals("lurchys_clock")) {
            this.item = new LurchysClock();
        } else if (lurchium.equals("lurchy_button")) {
            this.item = new LurchyButtonBlockItem();
        }
    }
}