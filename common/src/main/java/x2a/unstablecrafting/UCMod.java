package x2a.unstablecrafting;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UCMod {
    public static final String MOD_ID = "unstablecrafting";
    public static final Logger Log = LogManager.getLogger("Unstable Crafting");

    private static final Random RAND = new Random();
    public static Map<ResourceLocation, ItemStack> RECIPE_REDIRECTS = new HashMap<>();

    public static UCConfig CONFIG;

    static void randomiseRecipes(MinecraftServer server) {
        var redirects = new HashMap<ResourceLocation, ItemStack>();
        RECIPE_REDIRECTS.clear();
        var target = server.getRecipeManager();
        var outputs = new ArrayList<>(target.getRecipes());
        outputs.sort(Comparator.comparing(Recipe::getId)); // this is so our rng is seed determined
        Collections.shuffle(outputs, RAND);
        var recipes = target.getRecipes();

        for (var recipe : recipes) {
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
        Log.info("Welcome to the world of recipes with half lives");
        LifecycleEvent.SERVER_STARTING.register(server -> {
            RAND.setSeed(server.getWorldData().worldGenSettings().seed());
            randomiseRecipes(server);
            Log.info("Recipes have started to decay");
        });
        TickEvent.SERVER_POST.register(inst -> {
            var timeToRandom = inst.getTickCount() % CONFIG.server.ticksPerRandomise.get();
            if (timeToRandom == 0) {
                randomiseRecipes(inst);
            }
        });
        TickEvent.PLAYER_POST.register(player -> {
            if (player.isLocalPlayer() || !CONFIG.client.displayRandomiseWarnings.get()) {
                return;
            }
            var tickCount = player.level.getServer().getTickCount();
            var interval = CONFIG.server.ticksPerRandomise.get();
            var timeToRandom = interval - (tickCount % interval);
            ChatFormatting colour = null;
            if (tickCount % interval == 0) {
                player.sendSystemMessage(Component.translatable("message.unstablecrafting.random_time").withStyle(ChatFormatting.DARK_AQUA));
                player.playSound(SoundEvents.BELL_RESONATE);
            } else if (timeToRandom <= secsToTicks(5)) {
                if (timeToRandom % 20 == 0) {
                    player.sendSystemMessage(Component.translatable("message.unstablecrafting.randomise_warn_secs",
                            ticksToSecs(timeToRandom)).withStyle(ChatFormatting.DARK_RED));
                    player.playSound(SoundEvents.NOTE_BLOCK_CHIME);
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
                    player.sendSystemMessage(Component.translatable("message.unstablecrafting.randomise_warn",
                            ticksToMins(timeToRandom)).withStyle(ChatFormatting.RED));
                }
            }
        });
    }
}
