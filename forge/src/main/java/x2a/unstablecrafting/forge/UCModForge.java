package x2a.unstablecrafting.forge;

import dev.architectury.platform.forge.EventBuses;
import x2a.unstablecrafting.UCMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(UCMod.MOD_ID)
public class UCModForge {
    public UCModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(UCMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        UCMod.init();
    }
}
