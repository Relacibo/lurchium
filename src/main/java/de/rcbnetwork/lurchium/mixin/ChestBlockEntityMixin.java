package de.rcbnetwork.lurchium.mixin;

import de.rcbnetwork.lurchium.ChestBlockEntityWithCustomEvents;
import de.rcbnetwork.lurchium.events.ChestBlockEntityLoadedCallback;
import de.rcbnetwork.lurchium.events.ChestBreakEvent;
import de.rcbnetwork.lurchium.events.ChestInventoryChangedEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

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
        boolean loaded = world.isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getY()));
        if (loaded && chestBreakEvent != null) {
            chestBreakEvent.trigger(world);
        }
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        ChestBlockEntity entity = (ChestBlockEntity) (Object) this;
        ChestBlockEntityLoadedCallback.EVENT.invoker().interact(world, this.getPos(), entity, this.getCachedState());
    }
}
