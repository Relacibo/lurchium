package de.rcbnetwork.lurchium;


import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class Store implements ComponentV3 {
    public long startTimeStamp;
    public String clockDisplay = "";
    public long updateTick;
    public BlockPos signPosition = null;
    public BlockPos chestPosition = null;
    public final Map<Text, Long> leaderBoard = new HashMap<>();

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.startTimeStamp = tag.getLong("startTimeStamp");
        CompoundTag signPositionTag = tag.getCompound("signPosition");
        if (tag.get("signPosition") != null && !signPositionTag.isEmpty()) {
            this.signPosition = Util.convertTagToBlockPos(signPositionTag);
        }
        CompoundTag chestPositionTag = tag.getCompound("chestPosition");
        if (chestPositionTag != null && !chestPositionTag.isEmpty()) {
            this.chestPosition = Util.convertTagToBlockPos(chestPositionTag);
        }
        CompoundTag leaderBoardTag = tag.getCompound("leaderBoard");
        if (leaderBoardTag != null && !leaderBoardTag.isEmpty()) {
            for (String key : leaderBoardTag.getKeys()) {
                long value = leaderBoardTag.getLong(key);
                this.leaderBoard.put(new LiteralText(key), value);
            }
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putLong("startTimeStamp", this.startTimeStamp);
        if (this.signPosition != null) {
            tag.put("signPosition", Util.convertBlockPosToTag(this.signPosition));
        }
        if (this.chestPosition != null) {
            tag.put("chestPosition", Util.convertBlockPosToTag(this.chestPosition));
        }

        CompoundTag leaderBoardTag = new CompoundTag();
        for (Text key : this.leaderBoard.keySet()) {
            long value = this.leaderBoard.get(key);
            leaderBoardTag.putLong(key.asString(), value);
        }
        tag.put("leaderBoard", leaderBoardTag);
    }
}