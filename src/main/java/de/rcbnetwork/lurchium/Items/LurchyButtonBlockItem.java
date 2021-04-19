package de.rcbnetwork.lurchium.Items;

import de.rcbnetwork.lurchium.Blocks.LurchyButton;
import de.rcbnetwork.lurchium.ServersideObject;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.*;

public class LurchyButtonBlockItem extends BlockItem implements ServersideObject {
    public LurchyButtonBlockItem(Settings settings) {
        super(new LurchyButton(), settings);
    }

    public LurchyButtonBlockItem() {
        this(new Settings().maxCount(1));
    }

    @Override
    public Identifier getParentId() {
        return new Identifier("polished_blackstone_button");
    }

    @Override
    public int getParentRawId() {
        return 313;
    }

    public ActionResult place(ItemPlacementContext context) {
        if (!context.canPlace()) {
            return ActionResult.FAIL;
        } else {
            ItemPlacementContext itemPlacementContext = this.getPlacementContext(context);
            if (itemPlacementContext == null) {
                return ActionResult.FAIL;
            } else {
                BlockState blockState = this.getPlacementState(itemPlacementContext);
                if (blockState == null) {
                    return ActionResult.FAIL;
                } else if (!this.place(itemPlacementContext, blockState)) {
                    return ActionResult.FAIL;
                } else {
                    BlockPos blockPos = itemPlacementContext.getBlockPos();
                    World world = itemPlacementContext.getWorld();
                    PlayerEntity playerEntity = itemPlacementContext.getPlayer();
                    ItemStack itemStack = itemPlacementContext.getStack();
                    BlockState blockState2 = world.getBlockState(blockPos);
                    Block block = blockState2.getBlock();
                    if (block == blockState.getBlock()) {
                        blockState2 = this.placeFromTag(blockPos, world, itemStack, blockState2);
                        this.postPlacement(blockPos, world, playerEntity, itemStack, blockState2);
                        block.onPlaced(world, blockPos, blockState2, playerEntity, itemStack);
                        System.out.println(Registry.BLOCK.getId(block));
                        System.out.println(Registry.BLOCK.getRawId(block));
                        if (playerEntity instanceof ServerPlayerEntity) {
                            Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)playerEntity, blockPos, itemStack);
                        }
                    }

                    BlockSoundGroup blockSoundGroup = blockState2.getSoundGroup();
                    world.playSound(playerEntity, blockPos, this.getPlaceSound(blockState2), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
                    if (playerEntity == null || !playerEntity.abilities.creativeMode) {
                        itemStack.decrement(1);
                    }

                    return ActionResult.success(world.isClient);
                }
            }
        }
    }

    private BlockState placeFromTag(BlockPos pos, World world, ItemStack stack, BlockState state) {
        BlockState blockState = state;
        CompoundTag compoundTag = stack.getTag();
        if (compoundTag != null) {
            CompoundTag compoundTag2 = compoundTag.getCompound("BlockStateTag");
            StateManager<Block, BlockState> stateManager = state.getBlock().getStateManager();
            Iterator var9 = compoundTag2.getKeys().iterator();

            while(var9.hasNext()) {
                String string = (String)var9.next();
                Property<?> property = stateManager.getProperty(string);
                if (property != null) {
                    String string2 = compoundTag2.get(string).asString();
                    blockState = with(blockState, property, string2);
                }
            }
        }

        if (blockState != state) {
            world.setBlockState(pos, blockState, 2);
        }

        return blockState;
    }
    private static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, String name) {
        return (BlockState)property.parse(name).map((value) -> {
            return (BlockState)state.with(property, value);
        }).orElse(state);
    }
}
