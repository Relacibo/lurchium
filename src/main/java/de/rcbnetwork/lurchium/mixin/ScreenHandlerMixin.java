package de.rcbnetwork.lurchium.mixin;

import de.rcbnetwork.lurchium.ChestBlockEntityWithCustomEvents;
import de.rcbnetwork.lurchium.GenericContainerScreenHandlerWithCustomFields;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Inject(method="*", at=@At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;setStack(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
    void handleSetStack(CallbackInfoReturnable info) {
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
        chestBlockEntitiyWCE.getInventoryChangedEvent().trigger((GenericContainerScreenHandler)(Object)this, world, pos, player, chestBlockEntitiy);
    }
}
