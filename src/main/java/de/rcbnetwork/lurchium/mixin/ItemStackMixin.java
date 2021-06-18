package de.rcbnetwork.lurchium.mixin;

import de.rcbnetwork.lurchium.ServersideObjectRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "fromTag", at=@At("HEAD"), cancellable = true)
    private static void handleFromTag(NbtCompound tag, CallbackInfoReturnable<ItemStack> info) {
        if (!tag.contains("tag")) {
            return;
        }
        NbtCompound t = tag.getCompound("tag");
        if (!t.contains("lurchium")) {
            return;
        }
        String lurchium = t.getString("lurchium");
        info.setReturnValue(ServersideObjectRegistry.createItemStackOf(new Identifier(lurchium)));
    }
}