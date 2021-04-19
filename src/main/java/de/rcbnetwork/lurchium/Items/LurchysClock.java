package de.rcbnetwork.lurchium.Items;

import de.rcbnetwork.lurchium.ServersideObject;
import de.rcbnetwork.lurchium.Store;
import de.rcbnetwork.lurchium.StoreInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class LurchysClock extends Item implements ServersideObject {
    public LurchysClock(Settings settings) {
        super(settings);
    }

    public LurchysClock() {
        this(new Settings().maxCount(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        Store store = (Store) StoreInitializer.instance().componentKey.get(world);
        if (!store.clockNameDirty) {
            return;
        }
        stack.setCustomName(new LiteralText(store.clockName));
    }

    @Override
    public Identifier getParentId() {
        return new Identifier("clock");
    }

    @Override
    public int getParentRawId() {
        return 685;
    }
}
