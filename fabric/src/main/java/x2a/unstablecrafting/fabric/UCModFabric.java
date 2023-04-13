package x2a.unstablecrafting.fabric;

import x2a.unstablecrafting.fabriclike.UCModFabricLike;
import net.fabricmc.api.ModInitializer;

public class UCModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        UCModFabricLike.init();
    }
}
