package x2a.unstablecrafting.fabriclike;

import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import x2a.unstablecrafting.UCConfig;
import x2a.unstablecrafting.UCMod;

public class UCModFabricLike {
    public static void init() {
        var builder_cli = new ForgeConfigSpec.Builder();
        var cli = new UCConfig.ClientConfig(builder_cli);
        var builder_serve = new ForgeConfigSpec.Builder();
        var serve = new UCConfig.ServerConfig(builder_serve);
        ModLoadingContext.registerConfig(UCMod.MOD_ID, ModConfig.Type.CLIENT, builder_cli.build());
        ModLoadingContext.registerConfig(UCMod.MOD_ID, ModConfig.Type.SERVER, builder_serve.build());
        UCMod.init(new UCConfig(serve, cli));
    }
}
