package de.rcbnetwork.lurchium.Items;

import de.rcbnetwork.lurchium.Lurchium;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class LurchysClock extends Item implements LurchiumItem {
    public LurchysClock(Settings settings) {
        super(settings);
    }

    public LurchysClock() {
        this(new Settings().maxCount(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!selected) {
            return;
        }
        Store store = (Store) ComponentRegistryV3.INSTANCE.get(new Identifier("lurchium", "store")).get(world);
        if (!(entity instanceof ServerPlayerEntity) || store.updateTick == 0) {
            return;
        }
        Text message = new LiteralText(store.clockDisplay);
        ((ServerPlayerEntity)entity).sendMessage(message, true);
    }

    @Override
    public Identifier getParentId() {
        return new Identifier("clock");
    }

    @Override
    public int getParentRawId() {
        return 685;
    }

    @Override
    public String getCustomName() {
        return "Lurchys magische Uhr";
    }
}
