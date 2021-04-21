package de.rcbnetwork.lurchium;


import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

public class Store implements ComponentV3 {
    public long startTimeStamp;
    public String clockDisplay;
    public long updateTick;
    public BlockPos signPosition;
    public BlockPos displayPosition;

    protected Store() {
        this.clockDisplay = "";
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.startTimeStamp = tag.getLong("startTimeStamp");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putLong("startTimeStamp", this.startTimeStamp);
    }
}