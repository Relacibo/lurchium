package de.rcbnetwork.lurchium.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ChestBlockEntityLoadedCallback {
    Event<ChestBlockEntityLoadedCallback> EVENT = EventFactory.createArrayBacked(ChestBlockEntityLoadedCallback.class,
            (listeners) -> (world, pos, entity) -> {
                for (ChestBlockEntityLoadedCallback listener : listeners) {
                    ActionResult result = listener.interact(world, pos, entity);
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult interact(World world, BlockPos pos, ChestBlockEntity entity);

}
