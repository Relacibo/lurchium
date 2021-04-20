package de.rcbnetwork.lurchium;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.rcbnetwork.lurchium.Items.LurchysClock;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;


import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Lurchium implements ModInitializer {
    public final String TIMER_ROOT_COMMAND = "lurchium";

    private long oldTime = -1;

    @Override
    public void onInitialize() {
        ServersideObjectRegistry.ITEMS.put(new Identifier("lurchium", "lurchys_clock"), new LurchysClock());
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) -> dispatcher.register(literal(TIMER_ROOT_COMMAND)
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                        .then(literal("give_clock")
                                .then(argument("players", EntityArgumentType.players())
                                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                        .executes(this::executeGiveLurchysClock))
                                .executes(this::executeGiveLurchysClock))
                        .then(literal("start_timer")
                                .executes(this::executeStartTimer))
                        .then(literal("reset_timer")
                                .executes(this::executeResetTimer)))
        );

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
        });
        ServerWorldEvents.LOAD.register((s, world) -> {
            Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
            ServerTickEvents.END_WORLD_TICK.register(w -> this.onTick(w, store));
        });

    }

    private int executeStartTimer(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        startTimer(world);
        return 1;
    }

    private int executeResetTimer(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        resetTimer(world);
        return 1;
    }


    private void startTimer(World world) {
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        store.startTimeStamp = world.getTime();
    }

    private void resetTimer(World world) {
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        store.startTimeStamp = 0;
        store.clockDisplay = "";
        this.oldTime = -1;
    }

    private void onTick(World world, Store store) {
        long timestamp = store.startTimeStamp;
        if (timestamp == 0) {
            return;
        }
        long worldTimeStamp = world.getTime();
        long time = worldTimeStamp - timestamp;
        if (time == this.oldTime) {
            return;
        }
        this.oldTime = time;
        store.clockDisplay = formatIGT(time);
        store.updateTick = worldTimeStamp;
    }

    public String formatIGT(long time) {
        long tenth = (time % 20) * 5;
        long seconds = (time / 20) % 60;
        long minutes = (time / 1200) % 60;
        long hours = (time / 72000);

        String hoursString = String.format("%2s", String.valueOf(hours)).replace(' ', '0');
        String minutesString = String.format("%2s", String.valueOf(minutes)).replace(' ', '0');
        String secondsString = String.format("%2s", String.valueOf(seconds)).replace(' ', '0');
        String tenthString = String.format("%2s", String.valueOf(tenth)).replace(' ', '0');
        String withoutHoursString = minutesString + ":" + secondsString + "." + tenthString;
        return hours == 0 ? withoutHoursString : hoursString + ":" + withoutHoursString;
    }

    private int executeGiveLurchysClock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players;
        try {
            players = (Collection) EntityArgumentType.getEntities(context, "players");
        } catch (Exception e) {
            players = new ArrayList<>();
            players.add(context.getSource().getPlayer());
        }
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(context.getSource().getWorld());
        for (ServerPlayerEntity player : players) {
            ItemStack stack = ServersideObjectRegistry.createItemStackOf(new Identifier("lurchium", "lurchys_clock"));
            stack.setCustomName(new LiteralText(store.initialClockName));
            player.giveItemStack(stack);
        }
        return 0;
    }
}
