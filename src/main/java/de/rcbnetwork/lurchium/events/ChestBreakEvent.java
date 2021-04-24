package de.rcbnetwork.lurchium.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ChestBreakEvent {
    private List<ChestBreakListener> _listeners;

    private List<ChestBreakListener> listeners() {
        if (_listeners == null) {
            _listeners = new ArrayList<>();
        }
        return _listeners;
    }

    public void addListener(ChestBreakListener toAdd) {
        listeners().add(toAdd);
    }

    public void removeListener(ChestBreakListener toRemove) {
        if (_listeners == null) {
            return;
        }
        listeners().remove(toRemove);
    }

    public void trigger() {
        for (ChestBreakListener listener : listeners())
            listener.chestBroken();
    }

    public interface ChestBreakListener {
        ActionResult chestBroken();
    }
}
