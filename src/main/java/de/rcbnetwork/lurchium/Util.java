package de.rcbnetwork.lurchium;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class Util {
    public static NbtCompound convertBlockPosToTag(BlockPos pos) {
        NbtCompound tag = new NbtCompound();
        tag.putInt("X", pos.getX());
        tag.putInt("Y", pos.getY());
        tag.putInt("Z", pos.getZ());
        return tag;
    }

    public static BlockPos convertTagToBlockPos(NbtCompound tag) {
        int x, y, z;
        x = tag.getInt("X");
        y = tag.getInt("Y");
        z = tag.getInt("Z");
        return new BlockPos(x, y, z);
    }

    public static boolean doesBlockPosEqual(BlockPos a, BlockPos b) {
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }

    public static String formatIGT(long time) {
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

    public static String formatIGTSeconds(long time) {
        long seconds = (time / 20) % 60;
        long minutes = (time / 1200) % 60;
        long hours = (time / 72000);

        String hoursString = String.format("%2s", String.valueOf(hours)).replace(' ', '0');
        String minutesString = String.format("%2s", String.valueOf(minutes)).replace(' ', '0');
        String secondsString = String.format("%2s", String.valueOf(seconds)).replace(' ', '0');
        String withoutHoursString = minutesString + ":" + secondsString;
        return hours == 0 ? withoutHoursString : hoursString + ":" + withoutHoursString;
    }
}
