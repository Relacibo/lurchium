package de.rcbnetwork.lurchium.events;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ChestInventoryChangedEvent {
    private List<ChestInventoryChangedListener> _listeners;

    private List<ChestInventoryChangedListener> listeners() {
        if (_listeners == null) {
            _listeners = new ArrayList<>();
        }
        return _listeners;
    }

    public void addListener(ChestInventoryChangedListener toAdd) {
        listeners().add(toAdd);
    }

    public void removeListener(ChestInventoryChangedListener toRemove) {
        if (_listeners == null) {
            return;
        }
        listeners().remove(toRemove);
    }

    public void trigger(GenericContainerScreenHandler screenHandler, World world, BlockPos pos, Entity entity, ChestBlockEntity chestBlockEntitiy) {
        for (ChestInventoryChangedListener listener : listeners())
            listener.inventoryChanged(screenHandler, world, pos, entity, chestBlockEntitiy);
    }

    public interface ChestInventoryChangedListener {
        ActionResult inventoryChanged(GenericContainerScreenHandler screenHandler, World world, BlockPos pos, Entity entity, ChestBlockEntity chestBlockEntitiy);
    }
}
