package de.rcbnetwork.lurchium.mixin;

import de.rcbnetwork.lurchium.ServersideObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleRegistry.class)
public class SimpleRegistryMixin<T> {
    @Nullable
    @Inject(method="getId", cancellable = true, at=@At("HEAD"))
    public void handleGetId(T entry, CallbackInfoReturnable info) {
        if (!(entry instanceof ServersideObject)) {
            return;
        }
        Identifier id = ((ServersideObject)entry).getParentId();
        info.setReturnValue(id);
    }

    @Inject(method="getRawId", cancellable = true, at=@At("HEAD"))
    public void handleGetRawId(T entry, CallbackInfoReturnable info) {
        if (entry == null || !(entry instanceof ServersideObject)) {
            return;
        }
        int rawId = ((ServersideObject)entry).getParentRawId();
        info.setReturnValue(rawId);
    }
}
