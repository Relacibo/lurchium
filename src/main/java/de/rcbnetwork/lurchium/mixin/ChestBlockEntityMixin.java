package de.rcbnetwork.lurchium.mixin;

import de.rcbnetwork.lurchium.ChestBlockEntityWithCustomEvents;
import de.rcbnetwork.lurchium.events.ChestBlockEntityLoadedCallback;
import de.rcbnetwork.lurchium.events.ChestBreakEvent;
import de.rcbnetwork.lurchium.events.ChestInventoryChangedEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.block.ChestAnimationProgress;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlockEntity.class)
public class ChestBlockEntityMixin extends BlockEntity implements ChestBlockEntityWithCustomEvents {
    public ChestBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Unique
    private ChestInventoryChangedEvent chestInventoryChangedEvent;

    @Unique
    private ChestBreakEvent chestBreakEvent;

    @Unique
    public ChestInventoryChangedEvent getInventoryChangedEvent() {
        if (chestInventoryChangedEvent == null) {
            this.chestInventoryChangedEvent = new ChestInventoryChangedEvent();
        }
        return chestInventoryChangedEvent;
    }

    @Unique
    public ChestBreakEvent getChestBreakEvent() {
        if (chestBreakEvent == null) {
            this.chestBreakEvent = new ChestBreakEvent();
        }
        return chestBreakEvent;
    }


    @Override
    public void markRemoved() {
        super.markRemoved();
        this.getChestBreakEvent().trigger();
    }

    @Override
    public void setLocation(World world, BlockPos pos) {
        super.setLocation(world, pos);
        ChestBlockEntity entity = (ChestBlockEntity) (Object) this;
        ChestBlockEntityLoadedCallback.EVENT.invoker().interact(world, pos, entity);
    }
}
