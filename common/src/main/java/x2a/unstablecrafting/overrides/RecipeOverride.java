package x2a.unstablecrafting.overrides;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public interface RecipeOverride {
    public boolean matches(Recipe<?> inst);
    public void apply(Recipe<?> inst, ItemStack replace);
}
