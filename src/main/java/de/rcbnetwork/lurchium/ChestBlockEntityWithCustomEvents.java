package de.rcbnetwork.lurchium;


import de.rcbnetwork.lurchium.events.ChestBreakEvent;
import de.rcbnetwork.lurchium.events.ChestInventoryChangedEvent;

public interface ChestBlockEntityWithCustomEvents {
    ChestInventoryChangedEvent getInventoryChangedEvent();
    ChestBreakEvent getChestBreakEvent();
}
