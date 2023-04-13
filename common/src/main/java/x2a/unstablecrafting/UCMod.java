package x2a.unstablecrafting;

import com.google.common.base.Suppliers;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.Game;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
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
        server.getPlayerList().broadcastAll(new ClientboundUpdateRecipesPacket(target.getRecipes()));
        /*for (var recipe : recipes) {
            var repl = outputs.remove(outputs.size() - 1).getResultItem();
            var replace = (RecipeMixin)recipe;
            RecipeMixin<?> replaceRecipe;
            if (recipe instanceof RecipeMixin<?> r) {
                replaceRecipe = r;
            } else {
                replaceRecipe = new RandomRecipe<>(recipe);
            }
            replaceRecipe.newOutput = repl;
            replacement.add(replaceRecipe);
        }*/
    }

    public static void init() {
        Log.info("Welcome to the world of recipes with half lives");
        LifecycleEvent.SERVER_STARTING.register(server -> {
            RAND.setSeed(server.getWorldData().worldGenSettings().seed());
            randomiseRecipes(server);
        });
    }
}
