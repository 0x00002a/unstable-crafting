package x2a.unstablecrafting.quilt;

import x2a.unstablecrafting.fabriclike.UCModFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class UCModQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        UCModFabricLike.init();
    }
}
