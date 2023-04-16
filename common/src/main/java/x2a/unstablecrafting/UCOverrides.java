package x2a.unstablecrafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import x2a.unstablecrafting.overrides.RecipeOverride;

import java.util.ArrayList;
import java.util.List;

public class UCOverrides {
    private final List<RecipeOverride> OVERRIDES = new ArrayList<>();

    public void register(RecipeOverride override) {
        OVERRIDES.add(override);
    }
    public boolean matches(Recipe<?> target) {
        return OVERRIDES.stream().anyMatch(o -> o.matches(target));
    }

    public void apply(Recipe<?> target, ItemStack replace) {
        for (var override : OVERRIDES) {
            if (override.matches(target)) {
                override.apply(target, replace);
                return;
            }
        }
    }
}
