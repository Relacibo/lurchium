package de.rcbnetwork.lurchium.mixin;

import de.rcbnetwork.lurchium.ServersideObjectRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "fromTag", at=@At("HEAD"), cancellable = true)
    private static void handleFromTag(CompoundTag tag, CallbackInfoReturnable<ItemStack> info) {
        CompoundTag t = tag.getCompound("tag");
        String lurchium = t != null ? t.getString("lurchium") : null;
        if (lurchium == null) {
            return;
        }
        info.setReturnValue(ServersideObjectRegistry.createItemStackOf(new Identifier(lurchium)));
    }
}