package de.rcbnetwork.lurchium.mixin;

import de.rcbnetwork.lurchium.ChestBlockEntityWithCustomEvents;
import de.rcbnetwork.lurchium.events.ChestBlockEntityLoadedCallback;
import de.rcbnetwork.lurchium.events.ChestBreakEvent;
import de.rcbnetwork.lurchium.events.ChestInventoryChangedEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public class ChestBlockEntityMixin extends BlockEntity implements ChestBlockEntityWithCustomEvents {
    public ChestBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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
        World world = ((ChestBlockEntity)(Object)this).getWorld();
        this.getChestBreakEvent().trigger(world);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        ChestBlockEntity entity = (ChestBlockEntity) (Object) this;
        ChestBlockEntityLoadedCallback.EVENT.invoker().interact(world, this.getPos(), entity);
    }
}
