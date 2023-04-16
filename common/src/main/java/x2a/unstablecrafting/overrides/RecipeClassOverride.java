package x2a.unstablecrafting.overrides;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public class RecipeClassOverride<T> implements RecipeOverride {
    @FunctionalInterface
    public interface Applier<T> {
        void apply(T inst, ItemStack replace);
    }
    private final Class<T> clazz;
    private final Applier<T> apply;

    public RecipeClassOverride(Class<T> clazz, Applier<T> apply) {
        this.clazz = clazz;
        this.apply = apply;
    }

    @Override
    public boolean matches(Recipe<?> inst) {
        return clazz.isInstance(inst);
    }
    @Override
    public void apply(Recipe<?> inst, ItemStack replace) {
        var cast = clazz.cast(inst);
        this.apply.apply(cast, replace);
    }
}
