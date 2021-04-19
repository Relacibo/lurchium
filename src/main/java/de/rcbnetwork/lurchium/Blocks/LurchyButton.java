package de.rcbnetwork.lurchium.Blocks;

import de.rcbnetwork.lurchium.ServersideObject;
import de.rcbnetwork.lurchium.Store;
import de.rcbnetwork.lurchium.StoreInitializer;
import de.rcbnetwork.lurchium.TriggerRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.StoneButtonBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LurchyButton extends StoneButtonBlock implements ServersideObject {

    protected LurchyButton(Settings settings) {
        super(settings);
    }

    public LurchyButton() {
        this(AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().strength(0.5F));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if ((Boolean)state.get(POWERED)) {
            return ActionResult.CONSUME;
        } else {
            this.powerOn(state, world, pos, player);
            this.playClickSound(player, world, pos, true);
            return ActionResult.success(world.isClient);
        }
    }

    public void powerOn(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        world.setBlockState(pos, (BlockState)state.with(POWERED, true), 3);
        Store store = (Store) StoreInitializer.instance().componentKey.get(world);
        if (store.startTimeStamp == 0) {
            TriggerRegistry.instance().onTimerStart.accept(world, player);
        } else {
            TriggerRegistry.instance().onTimerReset.accept(world, player);
        }

    }

    @Override
    public Identifier getParentId() {
        return new Identifier("polished_blackstone_button");
    }

    @Override
    public int getParentRawId() {
        return 758;
    }
}
