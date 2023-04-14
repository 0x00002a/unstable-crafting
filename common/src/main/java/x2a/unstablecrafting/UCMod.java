package x2a.unstablecrafting;

import com.google.common.base.Suppliers;
import dev.architectury.event.events.client.ClientRecipeUpdateEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Game;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import x2a.unstablecrafting.mixins.RecipeMixin;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UCMod {
    public static final String MOD_ID = "unstablecrafting";
    public static final Logger Log = LogManager.getLogger("Unstable Crafting");

    private static final Random RAND = new Random();
    public static final Map<ItemStack, ItemStack> RECIPE_REDIRECTS = new HashMap<>();

    public static UCConfig CONFIG;

    static void randomiseRecipes(MinecraftServer server) {
        var target = server.getRecipeManager();
        var outputs = new ArrayList<>(target.getRecipes());
        outputs.sort(Comparator.comparing(Recipe::getId)); // this is so our rng is seed determined
        Collections.shuffle(outputs, RAND);
        var recipes = target.getRecipes();
        var replacement = new ArrayList<Recipe<?>>();
        replacement.ensureCapacity(recipes.size());

        RECIPE_REDIRECTS.clear();
        for (var recipe : recipes) {
            var repl = outputs.remove(outputs.size() - 1).getResultItem();
            RECIPE_REDIRECTS.put(recipe.getResultItem(), repl);
        }
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

    public static void init(UCConfig config) {
        CONFIG = config;
        Log.info("Welcome to the world of recipes with half lives");
        LifecycleEvent.SERVER_STARTING.register(server -> {
            RAND.setSeed(server.getWorldData().worldGenSettings().seed());
            randomiseRecipes(server);
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
            var timeToRandom =
                    CONFIG.server.ticksPerRandomise.get() - (player.tickCount % CONFIG.server.ticksPerRandomise.get());
            ChatFormatting colour = null;
            if (player.tickCount % CONFIG.server.ticksPerRandomise.get() == 0) {
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
