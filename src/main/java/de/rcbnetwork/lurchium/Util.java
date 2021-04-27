package de.rcbnetwork.lurchium;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

public class Util {
    public static CompoundTag convertBlockPosToTag(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("X", pos.getX());
        tag.putInt("Y", pos.getY());
        tag.putInt("Z", pos.getZ());
        return tag;
    }

    public static BlockPos convertTagToBlockPos(CompoundTag tag) {
        int x, y, z;
        x = tag.getInt("X");
        y = tag.getInt("Y");
        z = tag.getInt("Z");
        return new BlockPos(x, y, z);
    }

    public static boolean doesBlockPosEqual(BlockPos a, BlockPos b) {
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }
}
