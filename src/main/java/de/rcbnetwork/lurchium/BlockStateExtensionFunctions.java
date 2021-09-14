package de.rcbnetwork.lurchium;

import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Unique;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public interface BlockStateExtensionFunctions {
    int getWeakRedstonePower(Block block, BlockView blockView, BlockPos pos, Direction dir);
    int getStrongRedstonePower(Block block, BlockView blockView, BlockPos pos, Direction dir);
    boolean emitsRedstonePower(Block block);
    void scheduleTick(Block block, ServerWorld world, BlockPos pos, Random random);
}
