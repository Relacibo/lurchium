package de.rcbnetwork.lurchium;


import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class Store implements ComponentV3 {
    public long startTimeStamp;
    public String clockDisplay = "";
    public long updateTick;
    public BlockPos signPosition = null;
    public Set<BlockPos> timerSignPositions = Set.of();
    public BlockPos chestPosition = null;
    public final Map<Text, Long> leaderBoard = new HashMap<>();

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.startTimeStamp = tag.getLong("startTimeStamp");
        NbtCompound signPositionTag = tag.getCompound("signPosition");
        if (tag.get("signPosition") != null && !signPositionTag.isEmpty()) {
            this.signPosition = Util.convertTagToBlockPos(signPositionTag);
        }
        NbtCompound chestPositionTag = tag.getCompound("chestPosition");
        if (chestPositionTag != null && !chestPositionTag.isEmpty()) {
            this.chestPosition = Util.convertTagToBlockPos(chestPositionTag);
        }
        NbtCompound leaderBoardTag = tag.getCompound("leaderBoard");
        if (leaderBoardTag != null && !leaderBoardTag.isEmpty()) {
            for (String key : leaderBoardTag.getKeys()) {
                long value = leaderBoardTag.getLong(key);
                this.leaderBoard.put(new LiteralText(key), value);
            }
        }
        NbtList timerSignPositionsList = tag.getList("timerSignPositions", NbtElement.COMPOUND_TYPE);
        if (timerSignPositionsList != null && !timerSignPositionsList.isEmpty()) {
            for (int i = 0; i < timerSignPositionsList.size(); i++) {
                NbtCompound compound = timerSignPositionsList.getCompound(i);
                BlockPos pos =  Util.convertTagToBlockPos(compound);
                this.timerSignPositions.add(pos);
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLong("startTimeStamp", this.startTimeStamp);
        if (this.signPosition != null) {
            tag.put("signPosition", Util.convertBlockPosToTag(this.signPosition));
        }
        if (this.chestPosition != null) {
            tag.put("chestPosition", Util.convertBlockPosToTag(this.chestPosition));
        }

        NbtCompound leaderBoardTag = new NbtCompound();
        for (Text key : this.leaderBoard.keySet()) {
            long value = this.leaderBoard.get(key);
            leaderBoardTag.putLong(key.asString(), value);
        }
        tag.put("leaderBoard", leaderBoardTag);

        NbtList timerSignPositionsList = new NbtList();
        for (BlockPos pos : this.timerSignPositions) {
            timerSignPositionsList.add(Util.convertBlockPosToTag(pos));
        }
    }
}