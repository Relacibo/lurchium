package de.rcbnetwork.lurchium.events;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ActionResult;
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

    public void trigger(GenericContainerScreenHandler screenHandler, World world, BlockPos pos, Entity entity, ChestBlockEntity chestBlockEntitiy, Slot slot, ItemStack stack) {
        for (ChestInventoryChangedListener listener : listeners())
            listener.inventoryChanged(screenHandler, world, pos, entity, chestBlockEntitiy, slot, stack);
    }

    public interface ChestInventoryChangedListener {
        ActionResult inventoryChanged(GenericContainerScreenHandler screenHandler, World world, BlockPos pos, Entity entity, ChestBlockEntity chestBlockEntitiy, Slot slot, ItemStack stack);
    }
}
