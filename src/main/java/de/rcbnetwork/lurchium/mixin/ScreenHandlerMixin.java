package de.rcbnetwork.lurchium.mixin;

import de.rcbnetwork.lurchium.ChestBlockEntityWithCustomEvents;
import de.rcbnetwork.lurchium.GenericContainerScreenHandlerWithCustomFields;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Unique
    void extendSetStackHelper(Slot slot, ItemStack stack) {
        if (!((Object)this instanceof GenericContainerScreenHandler)) {
            return;
        }

        Inventory inventory = ((GenericContainerScreenHandler)(Object)this).getInventory();
        if (!(inventory instanceof ChestBlockEntity)) {
            return;
        }
        PlayerEntity player = ((GenericContainerScreenHandlerWithCustomFields)(Object)this).getPlayerInventory().player;
        World world = player.world;
        ChestBlockEntity chestBlockEntitiy = (ChestBlockEntity) inventory;
        ChestBlockEntityWithCustomEvents chestBlockEntitiyWCE = (ChestBlockEntityWithCustomEvents)chestBlockEntitiy;
        BlockPos pos = chestBlockEntitiy.getPos();
        chestBlockEntitiyWCE.getInventoryChangedEvent().trigger((GenericContainerScreenHandler)(Object)this, world, pos, player, chestBlockEntitiy, slot, stack);
    }

    // Support quick insert
    @Redirect(method="*", at=@At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;setStack(Lnet/minecraft/item/ItemStack;)V"))
    void extendSetStack(Slot slot, ItemStack stack) {
        slot.setStack(stack);
        this.extendSetStackHelper(slot, stack);
    }

    // Support drag and drop
    @Redirect(method="*", at=@At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;insertStack(Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;"))
    ItemStack extendInsertStack(Slot slot, ItemStack stack, int count) {
        ItemStack itemStack;
        if (!stack.isEmpty() && slot.canInsert(stack)) {
            itemStack = slot.getStack();
            int i = Math.min(Math.min(count, stack.getCount()), slot.getMaxItemCount(stack) - itemStack.getCount());
            if (itemStack.isEmpty()) {
                itemStack = stack.split(i);
            } else if (ItemStack.canCombine(itemStack, stack)) {
                stack.decrement(i);
                itemStack.increment(i);
            }
            slot.setStack(itemStack);
            this.extendSetStackHelper(slot, itemStack);
        }
        return stack;
    }
}
