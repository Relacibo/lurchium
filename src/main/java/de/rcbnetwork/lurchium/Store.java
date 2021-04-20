package de.rcbnetwork.lurchium;


import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.CompoundTag;

public class Store implements ComponentV3 {
    public long startTimeStamp;
    public String initialClockName;
    public String clockDisplay;
    public long updateTick;

    protected Store() {
        this.initialClockName = "Lurchys magische Uhr";
        this.clockDisplay = "";
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.startTimeStamp = tag.getLong("startTimeStamp");
        this.initialClockName = tag.getString("initialClockName");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putLong("startTimeStamp", this.startTimeStamp);
        tag.putString("initialClockName", this.initialClockName);
    }
}