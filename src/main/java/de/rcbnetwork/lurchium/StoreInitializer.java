package de.rcbnetwork.lurchium;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.util.Identifier;

public class StoreInitializer implements WorldComponentInitializer {
    public final ComponentKey<Store> componentKey;

    private static StoreInitializer _instance;

    public static StoreInitializer instance() {
        if (_instance == null) {
            _instance = new StoreInitializer();
        }
        return _instance;
    }

    public static void initialize() {
        instance();
    }

    private StoreInitializer() {
        this.componentKey = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("lurchium", "store"), Store.class);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        // Add the component to every World instance
        registry.register(this.componentKey, world -> new Store());
    }
}
