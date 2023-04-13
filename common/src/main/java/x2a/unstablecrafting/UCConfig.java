package x2a.unstablecrafting;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class UCConfig {
    private static <T> void define(ForgeConfigSpec.Builder builder, String name, String comment, T defaultValue) {
        builder.comment(comment).define(name, defaultValue);
    }

    private static long msToTicks(long ms) {
        return ms / 50;
    }

    private static long minsToTicks(long mins) {
        return msToTicks(mins * 60 * 1000);
    }

    public final ServerConfig server;
    public final ClientConfig client;

    public static class ServerConfig {
        public final ConfigValue<Long> ticksPerRandomise;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            ticksPerRandomise = builder.comment("Ticks per randomisation (20 ticks/second)").defineInRange(
                    "option.unstablecrafting.randomise_ticks",
                    minsToTicks(10), 0, Long.MAX_VALUE);
        }
    }

    public static class ClientConfig {
        public final ConfigValue<Boolean> displayRandomiseWarnings;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            displayRandomiseWarnings = builder.comment("Show chat warnings when randomisation is incoming").define(
                    "option.unstablecrafting.display_randomise_warnings", true);
        }
    }

    public UCConfig(ServerConfig server, ClientConfig client) {
        this.server = server;
        this.client = client;
    }
}
