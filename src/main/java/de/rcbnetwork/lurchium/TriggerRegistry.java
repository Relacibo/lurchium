package de.rcbnetwork.lurchium;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class TriggerRegistry {
    private static TriggerRegistry _instance;

    public static TriggerRegistry instance() {
        if (_instance == null) {
            _instance = new TriggerRegistry();
        }
        return _instance;
    }

    public BiConsumer<World, PlayerEntity> onTimerReset = (world, player) -> { };

    public BiConsumer<World, PlayerEntity> onTimerStart = (world, player) -> { };
}
