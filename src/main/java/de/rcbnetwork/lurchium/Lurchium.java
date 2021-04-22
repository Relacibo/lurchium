package de.rcbnetwork.lurchium;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.rcbnetwork.lurchium.Items.LurchysClock;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


import java.util.*;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
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
                        .then(literal("give")
                                .then(argument("id", string())
                                        .suggests((context, builder) -> {
                                            StringReader stringReader = new StringReader(builder.getInput());
                                            stringReader.setCursor(builder.getStart());
                                            return CommandSource.suggestMatching(ServersideObjectRegistry.ITEMS.keySet().stream().map(i -> i.getPath()), builder);
                                        })
                                        .then(argument("players", EntityArgumentType.players())
                                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                                .executes(this::executeGive))
                                        .executes(this::executeGive))
                        )
                        .then(literal("start_timer")
                                .executes(this::executeStartTimer))
                        .then(literal("reset_timer")
                                .executes(this::executeResetTimer))
                        .then(literal("set_chest")
                                .executes(this::executeSetChest))
                        .then(literal("set_display")
                                .executes(this::executeSetDisplay))
                ));
        ServerWorldEvents.LOAD.register((s, world) -> {
            Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
            BlockPos chestPosition = store.chestPosition;
            if (chestPosition != null) {
                BlockState chestState = world.getBlockState(chestPosition);
                if (chestState != null) {
                    addListenerToChestAt(world, store, chestPosition, chestState);
                }
            }
            printLeaderBoard(world, store);
            ServerTickEvents.END_WORLD_TICK.register(w -> this.onTick(w, store));
        });
    }

    private void printLeaderBoard(World world, Store store) {
        BlockPos signPosition = store.signPosition;
        BlockState state = world.getBlockState(signPosition);
        if (state == null) {
            return;
        }
        Block block = state.getBlock();
        if (!(block instanceof SignBlock)) {
            return;
        }
        store.leaderBoard.forEach((playerName, time) -> {
            SignBlockEntity signBlockEntity = (SignBlockEntity)world.getBlockEntity(signPosition);
            signBlockEntity.setTextOnRow(0, new LiteralText(""));
            signBlockEntity.setTextOnRow(1, playerName);
            signBlockEntity.setTextOnRow(2, new LiteralText(formatIGT(time)));
            signBlockEntity.setTextOnRow(3, new LiteralText(""));
        });
    }

    private int executeSetDisplay(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        System.out.println("Execute Display");
        ServerWorld world = context.getSource().getWorld();
        HitResult hit = context.getSource().getPlayer().raycast(20D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return 1;
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        return setDisplay(world, store, pos) ? 0 : 1;
    }

    private boolean setDisplay(ServerWorld world, Store store, BlockPos pos) {
        System.out.println("Chest");
        System.out.println(pos.toString());
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof SignBlock)) {
            return false;
        }
        store.signPosition = pos;
        System.out.println(store.signPosition.toString());
        return true;
    }

    private int executeSetChest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        System.out.println("Execute Chest");
        ServerWorld world = context.getSource().getWorld();
        HitResult hit = context.getSource().getPlayer().raycast(20D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return 1;
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        return setChest(world, store, pos) ? 0 : 1;
    }

    private boolean setChest(ServerWorld world, Store store, BlockPos pos) {
        System.out.println("Chest");
        System.out.println(pos.toString());
        BlockState state = world.getBlockState(pos);
        if (state == null || !(state.getBlock() instanceof ChestBlock)) {
            return false;
        }
        // Check if same chest
        BlockPos oldPos = store.chestPosition;
        boolean samePos = false;
        BlockState oldState = null;
        if (oldPos != null) {
            samePos = Util.doesBlockPosEqual(pos, oldPos);
            oldState = world.getBlockState(oldPos);
            if (samePos) {
                if (oldState == state) {
                    return true;
                }
            }
        }

        // Add listener to new chest
        if (!addListenerToChestAt(world, store, pos, state)) {
            return false;
        }
        store.chestPosition = pos;
        System.out.println(store.chestPosition.toString());
        if (samePos) {
            return true;
        }
        // Remove listener from old chest
        if (oldState != null) {
            Block oldChestBlock = oldState.getBlock();
            if (oldChestBlock instanceof ChestBlock) {
                ChestBlock oldChest = (ChestBlock) oldChestBlock;
                Inventory oldInventory = ChestBlock.getInventory(oldChest, oldState, world, oldPos, true);
                if (oldInventory instanceof SimpleInventory) {
                    ((SimpleInventory) oldInventory).removeListener((sender) -> this.onLurchyChestInventoryChanged(sender, oldInventory));
                }
            }
        }
        return true;
    }

    private boolean addListenerToChestAt(ServerWorld world, Store store, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof ChestBlock) {
            ChestBlock chest = (ChestBlock) block;
            Inventory inventory = ChestBlock.getInventory(chest, state, world, pos, true);
            if (inventory instanceof ChestBlockEntity) {
                ((ChestBlockEntityWithCustomEvents) inventory).getInventoryChangedEvent().addListener((w, p, player, newInventory) -> this.onLurchyChestInventoryChanged(w, p, player, newInventory));
            } else {
                return false;
            }
        }
        return true;
    }

    private ActionResult onLurchyChestInventoryChanged(World world, BlockPos position, Entity entity, DefaultedList<ItemStack> chestInventory) {
        System.out.println("*****");
        if (!(entity instanceof PlayerEntity)) {
            return ActionResult.PASS;
        }
        // Remove clock from inventory
        Item clock = (Item) ServersideObjectRegistry.ITEMS.get(new Identifier("lurchium", "lurchys_clock"));
        boolean removedClock = false;
        for (int i = 0; i < chestInventory.size(); ++i) {
            ItemStack itemStack = chestInventory.get(i);
            if (clock == itemStack.getItem() && itemStack.getCount() > 0) {
                chestInventory.remove(i);
                removedClock = true;
            }
        }
        if (!removedClock) {
            return ActionResult.PASS;
        }
        PlayerEntity player = (PlayerEntity)entity;
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        Text playerName = player.getName();
        if (store.leaderBoard.containsKey(playerName)) {
            return ActionResult.PASS;
        }
        long time = world.getTime() - store.startTimeStamp;
        store.leaderBoard.put(playerName, time);
        printLeaderBoard(world, store);
        return ActionResult.PASS;
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
        store.leaderBoard.clear();
        printLeaderBoard(world, store);
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

    private int executeGive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players;
        try {
            players = (Collection) EntityArgumentType.getEntities(context, "players");
        } catch (Exception e) {
            players = new ArrayList<>();
            players.add(context.getSource().getPlayer());
        }
        String itemId = context.getArgument("id", String.class);
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(context.getSource().getWorld());
        for (ServerPlayerEntity player : players) {
            ItemStack stack = ServersideObjectRegistry.createItemStackOf(new Identifier("lurchium", itemId));
            player.giveItemStack(stack);
        }
        return 0;
    }
}
