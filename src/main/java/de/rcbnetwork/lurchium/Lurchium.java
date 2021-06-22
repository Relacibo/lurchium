package de.rcbnetwork.lurchium;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.rcbnetwork.lurchium.Items.LurchysClock;
import de.rcbnetwork.lurchium.events.ChestBlockEntityLoadedCallback;
import de.rcbnetwork.lurchium.events.ChestBreakEvent;
import de.rcbnetwork.lurchium.events.ChestInventoryChangedEvent;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;


import java.util.*;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.block.BellBlock.FACING;
import static net.minecraft.block.SignBlock.ROTATION;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Lurchium implements ModInitializer {
    public final String TIMER_ROOT_COMMAND = "lurchium";

    public final ChestInventoryChangedEvent.ChestInventoryChangedListener lurchyChestInventoryChangedHandle =
            this::onLurchyChestInventoryChanged;
    private ChestBreakEvent.ChestBreakListener lurchyChestBrokenHandle = (world) -> {
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        store.chestPosition = null;
        return ActionResult.PASS;
    };

    private long oldTime = 0;

    @Override
    public void onInitialize() {
        ServersideObjectRegistry.ITEMS.put(new Identifier("lurchium", "lurchys_clock"), new LurchysClock());
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) -> dispatcher.register(literal(TIMER_ROOT_COMMAND)
                        .then(literal("give")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .then(argument("id", string())
                                        .suggests((context, builder) -> {
                                            StringReader stringReader = new StringReader(builder.getInput());
                                            stringReader.setCursor(builder.getStart());
                                            return CommandSource.suggestMatching(ServersideObjectRegistry.ITEMS.keySet().stream().map(Identifier::getPath), builder);
                                        })
                                        .then(argument("players", EntityArgumentType.players())
                                                .executes(this::executeGive))
                                        .executes(this::executeGive))
                        )
                        .then(literal("start_timer")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .executes(this::executeStartTimer))
                        .then(literal("reset_timer")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .executes(this::executeResetTimer))
                        .then(literal("set_chest")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .executes(this::executeSetChest))
                        .then(literal("unset_chest")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .executes(this::executeUnsetChest))
                        .then(literal("finish")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .then(argument("players", EntityArgumentType.players())
                                        .executes(this::executeSetPlayerFinished)))
                        .then(literal("leaderboard")
                                .then(literal("set_display")
                                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                        .executes(this::executeSetDisplay))
                                .then(literal("unset_display")
                                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                        .executes(this::executeUnsetLeaderBoardDisplay))
                                .then(literal("broadcast")
                                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                        .executes(this::executeBroadcastLeaderBoard))
                                .then(literal("reset")
                                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                        .executes(this::executeResetLeaderBoard))
                                .then(literal("print")
                                        .executes(this::executeSendLeaderBoard)))
                        .then(literal("add_timer_display")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .executes(this::executeAddTimerDisplay))
                        .then(literal("reset_timer_displays")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .executes(this::executeResetTimerDisplays))
                        .then(literal("print_timer_display_positions")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                                .executes(this::executePrintTimerDisplayPositions))

                ));
        ServerWorldEvents.LOAD.register((s, world) -> {
            Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
            ServerTickEvents.END_WORLD_TICK.register(w -> this.onTick(w, store));
        });
        ChestBlockEntityLoadedCallback.EVENT.register(this::onChestLoaded);
    }

    private ActionResult onChestLoaded(World world, BlockPos pos, ChestBlockEntity entity) {
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        BlockPos chestPosition = store.chestPosition;
        if (!pos.equals(chestPosition)) {
            return ActionResult.PASS;
        }
        addListenerToChestAt((ServerWorld) world, store, pos, entity);
        return ActionResult.PASS;
    }

    private int executeBroadcastLeaderBoard(CommandContext<ServerCommandSource> context) {
        World world = context.getSource().getWorld();
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        LiteralText leaderboardText = leaderBoardToText(store.leaderBoard);
        if (leaderboardText.getSiblings().size() == 0) {
            try {
                context.getSource().getPlayer().sendMessage(new LiteralText("There are no players on the leaderboard yet!"), false);
            } catch (CommandSyntaxException e) {
            }
            return 0;
        }
        for (PlayerEntity player : world.getPlayers()) {
            player.sendMessage(leaderboardText, false);
        }
        return 0;
    }

    private int executeSendLeaderBoard(CommandContext<ServerCommandSource> context) {
        World world = context.getSource().getWorld();
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        LiteralText leaderboardText = leaderBoardToText(store.leaderBoard);
        Text message = leaderboardText.getSiblings().size() > 0 ? leaderboardText : new LiteralText("There are no players on the leaderboard yet!");
        try {
            context.getSource().getPlayer().sendMessage(message, false);
        } catch (CommandSyntaxException e) {
        }
        return 0;
    }

    private LiteralText leaderBoardToText(Map<Text, Long> leaderboard) {
        LiteralText text = new LiteralText("");
        int i = 0;
        int size = leaderboard.size();
        for (Text key : leaderboard.keySet()) {
            long value = leaderboard.get(key);
            text = (LiteralText) text.append(String.format("%d. ", i + 1)).append(key).append(" ").append(Util.formatIGT(value));
            if (i != size - 1) {
                text = (LiteralText) text.append("\n");
            }
            i++;
        }
        return text;
    }

    private void executePrintLeaderBoard(World world, Store store) {
        BlockPos signPosition = store.signPosition;
        if (signPosition == null) {
            return;
        }
        BlockState state = world.getBlockState(signPosition);
        if (state == null) {
            return;
        }

        Direction direction = getSignDirectionFromState(state);
        int i = 0;
        for (Text playerName : store.leaderBoard.keySet()) {
            SignBlockEntity signBlockEntity = (SignBlockEntity) world.getBlockEntity(signPosition);
            if (signBlockEntity == null) {
                break;
            }
            state = world.getBlockState(signPosition);
            long time = store.leaderBoard.get(playerName);
            signBlockEntity.setTextOnRow(0, new LiteralText(String.format("- %d -", i + 1)));
            signBlockEntity.setTextOnRow(1, playerName.shallowCopy().formatted(Formatting.BOLD));
            signBlockEntity.setTextOnRow(2, new LiteralText(""));
            signBlockEntity.setTextOnRow(3, new LiteralText(Util.formatIGT(time)));
            ((ServerWorld) world).updateListeners(signPosition, state, state, 3);
            signPosition = signPosition.offset(direction);
            i++;
        }
    }

    private Direction getSignDirectionFromState(BlockState state) {
        Direction direction;
        try {
            direction = state.get(FACING);
        } catch (IllegalArgumentException e) {
            int rotation = state.get(ROTATION); // 0 == north, 4 == west, 8 == south, 12 == east
            return rotation < 2 ? Direction.EAST :
                    rotation < 6 ? Direction.SOUTH :
                            rotation < 10 ? Direction.WEST :
                                    rotation < 14 ? Direction.NORTH : Direction.EAST;
        }
        return direction.rotateYCounterclockwise();

    }

    private SignBlockEntity findNextSignEditBlock(SignBlockEntity signBlockEntity, Direction direction) {
        return signBlockEntity;
    }

    private int executeUnsetLeaderBoardDisplay(CommandContext<ServerCommandSource> context) {
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(context.getSource().getWorld());
        store.signPosition = null;
        context.getSource().sendFeedback(Text.of("Unset the leaderboard Display!"), true);
        return 0;
    }

    private int executeUnsetChest(CommandContext<ServerCommandSource> context) {
        World world = context.getSource().getWorld();
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        if (store.chestPosition == null) {
            return 0;
        }
        unsetChest(world, store.chestPosition);
        store.chestPosition = null;
        context.getSource().sendFeedback(Text.of("Unset the lurchium chest!"), true);
        return 0;
    }

    private BlockPos getSignPosFromPlayer(World world, ServerPlayerEntity player) throws CommandSyntaxException {
        HitResult hit = player.raycast(20D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof AbstractSignBlock)) {
            return null;
        }
        return pos;
    }

    private int executeAddTimerDisplay(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        ServerPlayerEntity player = context.getSource().getPlayer();
        BlockPos pos = getSignPosFromPlayer(world, player);
        if (pos == null) {
            context.getSource().sendError(Text.of("No Sign found!"));
            return 1;
        }
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        store.timerSignPositions.add(pos);
        context.getSource().sendFeedback(Text.of(String.format("Added Timer Display! (%s)", pos.toShortString())), true);
        return 0;
    }

    private int executeResetTimerDisplays(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        store.timerSignPositions.clear();
        context.getSource().sendFeedback(Text.of("Resetted the timer displays!"), true);
        return 0;
    }

    private int executePrintTimerDisplayPositions(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        String timerDisplayPositions = store.timerSignPositions.stream().map(pos -> String.format("(%s)", pos.toShortString())).collect(Collectors.joining(", "));
        context.getSource().sendFeedback(Text.of(timerDisplayPositions), false);
        return 0;
    }

    private int executeSetDisplay(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        ServerPlayerEntity player = context.getSource().getPlayer();
        BlockPos pos = getSignPosFromPlayer(world, player);
        if (pos == null) {
            context.getSource().sendError(Text.of("No Sign found!"));
            return 1;
        }
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        store.signPosition = pos;
        executePrintLeaderBoard(world, store);
        context.getSource().sendFeedback(Text.of(String.format("Set leaderboard display! (%s)", pos.toShortString())), true);
        return 0;
    }

    private int executeSetChest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        ServerPlayerEntity player = context.getSource().getPlayer();
        HitResult hit = player.raycast(20D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            context.getSource().sendError(Text.of("No Block found!"));
            return 1;
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        BlockState state = world.getBlockState(pos);
        if (state == null) {
            context.getSource().sendError(Text.of("Block has no state!"));
            return 1;
        }
        Block block = state.getBlock();
        if (!(block instanceof ChestBlock)) {
            context.getSource().sendError(Text.of("Block is no chest!"));
            return 1;
        }
        // Check if same chest
        BlockPos oldPos = store.chestPosition;
        if (pos.equals(oldPos)) {
            context.getSource().sendError(Text.of("Chest is already lurchys chest!"));
            return 1;
        }
        ChestBlockEntity inventory = (ChestBlockEntity) ChestBlock.getInventory((ChestBlock) block, state, world, pos, true);
        // Add listener to new chest
        addListenerToChestAt(world, store, pos, inventory);

        store.chestPosition = pos;
        if (oldPos != null && unsetChest(world, oldPos)) {
            context.getSource().sendFeedback(Text.of("Unset the other chest!"), true);
        }
        context.getSource().sendFeedback(Text.of(String.format("Set lurchys chest! (%s)", pos.toShortString())), true);
        return 0;
    }

    private boolean unsetChest(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state == null) {
            return false;
        }
        Block chestBlock = state.getBlock();
        if (!(chestBlock instanceof ChestBlock)) {
            return false;
        }

        ChestBlock chest = (ChestBlock) chestBlock;
        ChestBlockEntity chestBlockEntity = (ChestBlockEntity) ChestBlock.getInventory(chest, state, world, pos, true);
        assert chestBlockEntity != null;
        ((ChestBlockEntityWithCustomEvents) chestBlockEntity).getInventoryChangedEvent().removeListener(lurchyChestInventoryChangedHandle);
        ((ChestBlockEntityWithCustomEvents) chestBlockEntity).getChestBreakEvent().removeListener(lurchyChestBrokenHandle);
        return true;
    }

    private void addListenerToChestAt(ServerWorld world, Store store, BlockPos pos, ChestBlockEntity chestBlockEntity) {
        ((ChestBlockEntityWithCustomEvents) chestBlockEntity).getInventoryChangedEvent().addListener(lurchyChestInventoryChangedHandle);
        ((ChestBlockEntityWithCustomEvents) chestBlockEntity).getChestBreakEvent().addListener(lurchyChestBrokenHandle);
    }

    private int executeSetPlayerFinished(CommandContext<ServerCommandSource> context) {
        Collection<ServerPlayerEntity> players;
        World world = context.getSource().getWorld();
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        Item lurchys_clock = (Item) ServersideObjectRegistry.ITEMS.get(new Identifier("lurchium", "lurchys_clock"));
        try {
            players = (Collection) EntityArgumentType.getEntities(context, "players");
        } catch (Exception e) {
            return 1;
        }
        for (PlayerEntity player : players) {
            int count = Inventories.remove(player.getInventory(), (stack) -> stack.getItem() == lurchys_clock, 1, false);
            if (count > 0) {
                addPlayerToLeaderBoard(world, store, player);
            }
        }
        return 0;
    }

    private ActionResult onLurchyChestInventoryChanged(
            GenericContainerScreenHandler screenHandler,
            World world,
            BlockPos position,
            Entity entity,
            ChestBlockEntity chestBlockEntity,
            Slot slot,
            ItemStack stack) {
        if (!(entity instanceof PlayerEntity)) {
            return ActionResult.PASS;
        }
        if (stack.getItem() != (Item) ServersideObjectRegistry.ITEMS.get(new Identifier("lurchium", "lurchys_clock"))) {
            return ActionResult.PASS;
        }
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        if (store.startTimeStamp == 0) {
            return ActionResult.PASS;
        }
        screenHandler.sendContentUpdates();
        slot.setStack(ItemStack.EMPTY);
        screenHandler.sendContentUpdates();
        addPlayerToLeaderBoard(world, store, (PlayerEntity) entity);
        return ActionResult.PASS;
    }

    private void addPlayerToLeaderBoard(World world, Store store, PlayerEntity player) {
        Text playerName = player.getName();
        if (store.leaderBoard.containsKey(playerName)) {
            return;
        }
        long time = world.getTime() - store.startTimeStamp;
        store.leaderBoard.put(playerName, time);
        executePrintLeaderBoard(world, store);
    }

    private int executeStartTimer(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        if (startTimer(world)) {
            context.getSource().sendFeedback(Text.of("Started timer!"), true);
        } else {
            context.getSource().sendError(Text.of("Timer already started!"));
        }
        return 0;
    }

    private int executeResetTimer(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        resetTimer(world);
        context.getSource().sendFeedback(Text.of("Resetted timer!"), true);
        return 0;
    }


    private boolean startTimer(World world) {
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        if (store.startTimeStamp != 0) {
            return false;
        }
        store.startTimeStamp = world.getTime();
        return true;

    }

    private void resetTimer(World world) {
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        store.startTimeStamp = 0;
        store.clockDisplay = "";
    }

    private int executeResetLeaderBoard(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        resetLeaderBoard(world);
        context.getSource().sendFeedback(Text.of("Resetted leaderboard!"), true);
        return 0;
    }

    private void resetLeaderBoard(World world) {
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        store.leaderBoard.clear();
    }

    private void onTick(World world, Store store) {
        long timestamp = store.startTimeStamp;
        if (timestamp == 0) {
            return;
        }
        long worldTimeStamp = world.getTime();
        long time = worldTimeStamp - timestamp;
        store.clockDisplay = Util.formatIGT(time);
        store.updateTick = worldTimeStamp;
        if (this.oldTime == 0 || this.oldTime / 20 != time / 20) {
            Iterator<BlockPos> iter = store.timerSignPositions.iterator();
            while (iter.hasNext()) {
                BlockPos pos = iter.next();
                SignBlockEntity signBlockEntity = (SignBlockEntity) world.getBlockEntity(pos);
                if (signBlockEntity == null) {
                    iter.remove();
                    break;
                }
                BlockState state = world.getBlockState(pos);
                String formattedTime = Util.formatIGTSeconds(time);
                signBlockEntity.setTextOnRow(1, new LiteralText(String.format("- %s -", formattedTime)));
                world.updateListeners(pos, state, state, 3);
            }
        }
        this.oldTime = time;
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
        context.getSource().sendFeedback(Text.of("OK!"), false);
        return 0;
    }
}
