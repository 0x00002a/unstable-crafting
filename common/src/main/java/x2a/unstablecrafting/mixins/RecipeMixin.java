package x2a.unstablecrafting.mixins;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import x2a.unstablecrafting.RandomRecipe;

@Mixin(Recipe.class)
public class RecipeMixin<C extends Container> implements Recipe<C> {

    Recipe<C> replaced;
    ItemStack newOutput;


    private ItemStack tryReplace(ItemStack input) {
        if (input.equals(replaced.getResultItem())) {
            return newOutput.copy();
        } else {
            return input;
        }
    }

    @Override
    public boolean matches(C container, Level level) {
        return replaced.matches(container, level);
    }

    @Override
    public ItemStack assemble(C container) {
        return tryReplace(this.replaced.assemble(container));
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return this.replaced.canCraftInDimensions(i, j);
    }

    @Override
    public ItemStack getResultItem() {
        return newOutput.copy();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(C container) {
        return this.replaced.getRemainingItems(container);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.replaced.getIngredients();
    }

    @Override
    public boolean isSpecial() {
        return this.replaced.isSpecial();
    }

    @Override
    public String getGroup() {
        return this.replaced.getGroup();
    }

    @Override
    public ItemStack getToastSymbol() {
        return this.replaced.getToastSymbol();
    }

    @Override
    public ResourceLocation getId() {
        return this.replaced.getId();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.replaced.getSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return this.replaced.getType();
    }

    @Override
    public boolean isIncomplete() {
        return this.replaced.isIncomplete();
    }
}