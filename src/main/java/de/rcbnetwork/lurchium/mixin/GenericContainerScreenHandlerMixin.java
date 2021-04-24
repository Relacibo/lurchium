package de.rcbnetwork.lurchium.mixin;

import de.rcbnetwork.lurchium.ChestBlockEntityWithCustomEvents;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GenericContainerScreenHandler.class)
public class GenericContainerScreenHandlerMixin {
    @Shadow
    Inventory inventory;

    @Unique
    PlayerInventory playerInventory;

    @Shadow
    int rows;

    @Inject(method="<init>(Lnet/minecraft/screen/ScreenHandlerType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;I)V", at=@At("RETURN"))
    void handleInit(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows, CallbackInfo info) {
        this.playerInventory = playerInventory;
    }

    @Inject(method="*", at=@At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;setStack(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
    void onSetStack(CallbackInfoReturnable info) {
        if (!(inventory instanceof ChestBlockEntity)) {
            return;
        }
        PlayerEntity player = playerInventory.player;
        World world = player.world;
        ChestBlockEntity chestBlockEntitiy = (ChestBlockEntity) inventory;
        ChestBlockEntityWithCustomEvents chestBlockEntitiyWCE = (ChestBlockEntityWithCustomEvents)chestBlockEntitiy;
        BlockPos pos = chestBlockEntitiy.getPos();
        chestBlockEntitiyWCE.getInventoryChangedEvent().trigger((GenericContainerScreenHandler)(Object)this, world, pos, player, chestBlockEntitiy);
    }

}
