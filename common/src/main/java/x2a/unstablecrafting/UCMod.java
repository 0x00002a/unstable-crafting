package x2a.unstablecrafting;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import x2a.unstablecrafting.network.RandomiseWarningPacket;

import java.util.*;

public class UCMod {
    public static final Class<?>[] SUPPORTED_RECIPE_TYPES = {ShapedRecipe.class, AbstractCookingRecipe.class,
            ShapelessRecipe.class, CustomRecipe.class,
            SingleItemRecipe.class};

    public static final String MOD_ID = "unstablecrafting";
    public static final Logger Log = LogManager.getLogger("Unstable Crafting");

    private static final Random RAND = new Random();
    public static Map<ResourceLocation, ItemStack> RECIPE_REDIRECTS = new HashMap<>();
    public static final NetworkChannel RAND_WARNING_CHAN = NetworkChannel.create(new ResourceLocation(MOD_ID,
            "randomise_time_update"));

    public static UCConfig CONFIG;

    static void randomiseRecipes(MinecraftServer server) {
        var redirects = new HashMap<ResourceLocation, ItemStack>();
        RECIPE_REDIRECTS.clear();
        var target = server.getRecipeManager();
        var targetRecipes =
                target.getRecipes().stream().filter(r -> Arrays.stream(SUPPORTED_RECIPE_TYPES).anyMatch(c -> c.isInstance(r))).toList();
        var outputs = new ArrayList<>(targetRecipes);
        outputs.sort(Comparator.comparing(Recipe::getId)); // this is so our rng is seed determined
        Collections.shuffle(outputs, RAND);

        for (var recipe : targetRecipes) {
            var repl = outputs.remove(outputs.size() - 1).getResultItem();
            redirects.put(recipe.getId(), repl);
        }
        RECIPE_REDIRECTS = redirects;
        var pkt = new ClientboundUpdateRecipesPacket(target.getRecipes());
        server.getPlayerList().getPlayers().forEach(p -> {
            p.connection.send(pkt);
            p.getRecipeBook().sendInitialRecipeBook(p);
        });
    }

    public static long msToTicks(long ms) {
        return ms / 50;
    }


    public static long secsToTicks(long secs) {
        return secs * 20;
    }

    public static long minsToTicks(long mins) {
        return msToTicks(mins * 60 * 1000);
    }

    public static long ticksToMins(long ticks) {
        return ticks / 20 / 60;
    }

    public static long ticksToSecs(long ticks) {
        return ticks / 20;
    }

    @Nullable
    public static ItemStack replaceResult(ResourceLocation orig) {
        var replace = UCMod.RECIPE_REDIRECTS.get(orig);
        if (replace != null) {
            return replace.copy();
        }
        return null;
    }


    public static void init(UCConfig config) {
        CONFIG = config;
        RAND_WARNING_CHAN.register(RandomiseWarningPacket.class, RandomiseWarningPacket::encode, RandomiseWarningPacket::new,
                RandomiseWarningPacket::apply);
        Log.info("Welcome to the world of recipes with half lives");
        LifecycleEvent.SERVER_STARTING.register(server -> {
            RAND.setSeed(server.getWorldData().worldGenSettings().seed());
            randomiseRecipes(server);
            Log.info("Recipes have started to decay");
        });
        TickEvent.SERVER_POST.register(inst -> {
            var tickCount = inst.getTickCount();
            var interval = CONFIG.server.ticksPerRandomise.get();
            var timeToRandom = interval - (tickCount % interval);
            if (tickCount % interval == 0) {
                randomiseRecipes(inst);
            }
            inst.getPlayerList().getPlayers().forEach(player -> {
                ChatFormatting colour = null;
                if (tickCount % interval == 0) {
                    RAND_WARNING_CHAN.sendToPlayer(player, new RandomiseWarningPacket(Component.translatable("message.unstablecrafting.random_time").withStyle(ChatFormatting.DARK_AQUA),
                            SoundEvents.BELL_RESONATE));
                } else if (timeToRandom <= secsToTicks(5)) {
                    if (timeToRandom % 20 == 0) {
                        RAND_WARNING_CHAN.sendToPlayer(player, new RandomiseWarningPacket(Component.translatable("message.unstablecrafting.randomise_warn_secs",
                                ticksToSecs(timeToRandom)).withStyle(ChatFormatting.DARK_RED),
                                SoundEvents.NOTE_BLOCK_CHIME));
                    }
                } else {
                    if (timeToRandom == minsToTicks(1)) {
                        colour = ChatFormatting.RED;
                    } else if (timeToRandom == minsToTicks(5)) {
                        colour = ChatFormatting.YELLOW;
                    } else if (timeToRandom == minsToTicks(10)) {
                        colour = ChatFormatting.GREEN;
                    }
                    if (colour != null) {
                        RAND_WARNING_CHAN.sendToPlayer(player, new RandomiseWarningPacket(Component.translatable("message" +
                                        ".unstablecrafting" +
                                        ".randomise_warn",
                                ticksToMins(timeToRandom)).withStyle(colour), null));
                    }
                }
            });
        });

    }
}
