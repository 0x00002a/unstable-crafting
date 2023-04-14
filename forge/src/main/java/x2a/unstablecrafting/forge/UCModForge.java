package x2a.unstablecrafting.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import x2a.unstablecrafting.UCConfig;
import x2a.unstablecrafting.UCMod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(UCMod.MOD_ID)
public class UCModForge {
    public UCModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(UCMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        var builder_cli = new ForgeConfigSpec.Builder();
        var cli = new UCConfig.ClientConfig(builder_cli);
        var builder_serve = new ForgeConfigSpec.Builder();
        var serve = new UCConfig.ServerConfig(builder_serve);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, builder_cli.build());
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, builder_serve.build());
        UCMod.init(new UCConfig(serve, cli));
    }
}
