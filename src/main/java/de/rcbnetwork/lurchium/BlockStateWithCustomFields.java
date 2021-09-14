package de.rcbnetwork.lurchium;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public interface BlockStateWithCustomFields {
    Function<Block, Function<BlockView, Function<BlockPos, Function<Direction, Integer>>>> getGetWeakRedstonePowerFunction();
    void overrideGetWeakRedstonePowerFunction(Function<Block, Function<BlockView, Function<BlockPos, Function<Direction, Integer>>>> getWeakRedstonePowerFunction);
    Function<Block, Function<BlockView, Function<BlockPos, Function<Direction, Integer>>>> getGetStrongRedstonePowerFunction();
    void overrideGetStrongRedstonePowerFunction(Function<Block, Function<BlockView, Function<BlockPos, Function<Direction, Integer>>>> getStrongRedstonePowerFunction);
    Function<Block, Boolean> getEmitsRedstonePowerFunction();
    void overrideEmitsRedstonePowerFunction(Function<Block, Boolean> emitsRedstonePowerFunction);
    Function<Block, Function<ServerWorld, Function<BlockPos, Consumer<Random>>>> getScheduleTickFunction();
    void setScheduleTickFunction(Function<Block, Function<ServerWorld, Function<BlockPos, Consumer<Random>>>> scheduleTickFunction);

}
