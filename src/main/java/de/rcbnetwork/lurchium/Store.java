package de.rcbnetwork.lurchium;


import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

public class Store implements ComponentV3 {
    public long startTimeStamp;
    public String initialClockName;
    public boolean clockNameDirty;
    public String clockName;

    protected Store() { }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.startTimeStamp = tag.getLong("startTimeStamp");
        this.initialClockName = tag.getString("initialClockName");
        if (this.initialClockName == null) {
            this.initialClockName = "Lurchys magische Uhr";
        }
        this.clockNameDirty = tag.getBoolean("clockNameDirty");
        this.clockName = tag.getString("clockName");
        if (this.clockName == null) {
            this.clockName = this.initialClockName;
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putLong("startTimeStamp", this.startTimeStamp);
        tag.putString("initialClockName", this.initialClockName);
        tag.putBoolean("clockNameDirty", this.clockNameDirty);
        tag.putString("clockName", this.clockName);
    }
}