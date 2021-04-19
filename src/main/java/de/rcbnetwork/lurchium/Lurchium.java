package de.rcbnetwork.lurchium;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.rcbnetwork.lurchium.Items.LurchysClock;
import de.rcbnetwork.lurchium.Items.LurchyButtonBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;


import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Lurchium implements ModInitializer {
    public final String TIMER_ROOT_COMMAND = "lurchium";
    public final Predicate<ServerCommandSource> PLAYER_AUTHORIZED_PREDICATE = (source) ->
    {
        try {
            return source.getPlayer().hasPermissionLevel(2);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return false;
        }
    };

    private long oldTime = -1;

    @Override
    public void onInitialize() {
        StoreInitializer.initialize();
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) -> dispatcher.register(literal(TIMER_ROOT_COMMAND)
                        .requires(PLAYER_AUTHORIZED_PREDICATE)
                        .then(
                                literal("give_button")
                                        .then(argument("players", EntityArgumentType.players())
                                                .executes(this::executeGiveLurchysButton))
                                        .executes(this::executeGiveLurchysButton)
                        )
                        .then(literal("give_clock")
                                .then(argument("players", EntityArgumentType.players())
                                        .executes(this::executeGiveLurchysClock))
                                .executes(this::executeGiveLurchysClock))));
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            TriggerRegistry.instance().onTimerStart = (World world, PlayerEntity player) -> {
                Store store = (Store) StoreInitializer.instance().componentKey.get(world);
                store.startTimeStamp = world.getTime();
                System.out.println("start");
            };
            TriggerRegistry.instance().onTimerReset = (World world, PlayerEntity player) -> {
                Store store = (Store) StoreInitializer.instance().componentKey.get(world);
                store.startTimeStamp = 0;
                this.oldTime = -1;
                System.out.println("stop");
            };
        });
        ServerWorldEvents.LOAD.register((s, world) -> {
            ServerTickEvents.END_WORLD_TICK.register(this::onTick);
        });
    }

    private void onTick(World world) {
        Store store = (Store) StoreInitializer.instance().componentKey.get(world);
        store.clockNameDirty = false;
        long timestamp = store.startTimeStamp;
        if (timestamp == 0) {
            return;
        }
        long time = world.getTime() - timestamp;
        if (time == this.oldTime) {
            return;
        }
        this.oldTime = time;
        store.clockName = formatIGT(time);
        store.clockNameDirty = true;
    }

    public String formatIGT(long time) {
        long tenth = time % 10;
        long seconds = (time / 10) % 60;
        long minutes = (time / 600) % 60;
        long hours = (time / 36000);

        String hoursString = String.format("%2s", String.valueOf(hours)).replace(' ', '0');
        String minutesString = String.format("%2s", String.valueOf(minutes)).replace(' ', '0');
        String secondsString = String.format("%2s", String.valueOf(seconds)).replace(' ', '0');
        String withoutHoursString = minutesString + ":" + secondsString + "." + tenth;
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
        Store store = (Store) StoreInitializer.instance().componentKey.get(context.getSource().getWorld());
        for (ServerPlayerEntity player : players) {
            Item lurchysClock = new LurchysClock();
            ItemStack stack = new ItemStack(lurchysClock, 1);
            stack.setCustomName(new LiteralText(store.initialClockName));
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("lurchium", "lurchys_clock");
            player.giveItemStack(stack);
        }
        return 0;
    }

    private int executeGiveLurchysButton(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players;
        try {
            players = (Collection) EntityArgumentType.getEntities(context, "players");
        } catch (Exception e) {
            players = new ArrayList<>();
            players.add(context.getSource().getPlayer());
        }
        for (ServerPlayerEntity player : players) {
            Item lurchyButton = new LurchyButtonBlockItem();
            ItemStack stack = new ItemStack(lurchyButton, 1);
            stack.setCustomName(new LiteralText("Lurchys Zauberknopf"));
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("lurchium", "lurchy_button");
            player.giveItemStack(stack);
        }
        return 0;
    }
}
