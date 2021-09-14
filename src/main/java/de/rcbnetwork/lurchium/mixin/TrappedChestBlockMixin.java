package de.rcbnetwork.lurchium.mixin;

import de.rcbnetwork.lurchium.BlockStateWithCustomFields;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.function.Supplier;

@Mixin(ChestBlock.class)
class ChestBlockMixin extends AbstractChestBlock<ChestBlockEntity> {
    public ChestBlockMixin(AbstractBlock.Settings settings, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier) {
        super(settings, supplier);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        BlockStateWithCustomFields stateWCF = (BlockStateWithCustomFields)state;
        if (stateWCF.getGetWeakRedstonePowerFunction() == null) {
            return 0;
        }
        return stateWCF.getGetWeakRedstonePowerFunction().apply(this).apply(world).apply(pos).apply(direction);
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        BlockStateWithCustomFields stateWCF = (BlockStateWithCustomFields)state;
        if (stateWCF.getGetStrongRedstonePowerFunction() == null) {
            return 0;
        }
        return stateWCF.getGetStrongRedstonePowerFunction().apply(this).apply(world).apply(pos).apply(direction);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        BlockStateWithCustomFields stateWCF = (BlockStateWithCustomFields)state;
        if (stateWCF.getEmitsRedstonePowerFunction() == null) {
            return false;
        }
        return stateWCF.getEmitsRedstonePowerFunction().apply(this);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockStateWithCustomFields stateWCF = (BlockStateWithCustomFields)state;
        if (stateWCF.getScheduleTickFunction() == null) {
            return;
        }
        stateWCF.getScheduleTickFunction().apply(this).apply(world).apply(pos).accept(random);
    }

    @Shadow
    public DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> getBlockEntitySource(BlockState state, World world, BlockPos pos, boolean ignoreBlocked) {
        return null;
    }

    @Shadow
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
