package de.rcbnetwork.lurchium.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import de.rcbnetwork.lurchium.BlockStateExtensionFunctions;
import de.rcbnetwork.lurchium.BlockStateWithCustomFields;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(BlockState.class)
public class BlockStateMixin extends AbstractBlock.AbstractBlockState implements BlockStateWithCustomFields {
    protected BlockStateMixin(Block block, ImmutableMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> codec) {
        super(block, propertyMap, codec);
    }

    @Shadow
    public BlockState asBlockState() {
        return null;
    }

    @Unique
    public BlockStateExtensionFunctions extensionFunctions = null;

    @Unique
    public BlockStateExtensionFunctions getExtensionFunctions() {
        return extensionFunctions;
    }

    @Unique
    public void setExtensionFunction(BlockStateExtensionFunctions functions) {
        this.extensionFunctions = functions;
    }
}
