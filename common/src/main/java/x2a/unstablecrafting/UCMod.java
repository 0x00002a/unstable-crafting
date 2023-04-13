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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UCMod {
    public static final String MOD_ID = "unstablecrafting";
    public static final Logger Log = LogManager.getLogger("Unstable Crafting");

    private static final Random RAND = new Random();

    static void randomiseRecipes(RecipeManager target) {
        var outputs = new ArrayList<>(target.getRecipes());
        outputs.sort(Comparator.comparing(Recipe::getId)); // this is so our rng is seed determined
        Collections.shuffle(outputs, RAND);
        var recipes = target.getRecipes();
        var replacement = new ArrayList<Recipe<?>>();
        replacement.ensureCapacity(recipes.size());
        for (var recipe : recipes) {
            var repl = outputs.remove(outputs.size() - 1).getResultItem();
            RandomRecipe<?> replaceRecipe;
            if (recipe instanceof RandomRecipe<?> r) {
                replaceRecipe = r;
            } else {
                replaceRecipe = new RandomRecipe<>(recipe);
            }
            replaceRecipe.newOutput = repl;
            replacement.add(replaceRecipe);
        }
        target.replaceRecipes(replacement);
    }

    public static void init() {
        Log.info("Welcome to the world of recipes with half lives");
        LifecycleEvent.SERVER_STARTING.register(server -> {
            var manager = server.getRecipeManager();
            RAND.setSeed(server.getWorldData().worldGenSettings().seed());
            randomiseRecipes(manager);
        });
    }
}
