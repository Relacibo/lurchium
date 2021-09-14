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
    BlockStateExtensionFunctions getExtensionFunctions();
    void setExtensionFunction(BlockStateExtensionFunctions functions);
}
