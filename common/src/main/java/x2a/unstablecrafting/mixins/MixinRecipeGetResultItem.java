package x2a.unstablecrafting.mixins;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import x2a.unstablecrafting.UCMod;


@Mixin({ShapedRecipe.class, AbstractCookingRecipe.class, ShapelessRecipe.class, CustomRecipe.class})
public abstract class MixinRecipeGetResultItem {

    @Shadow
    public abstract ResourceLocation getId();

    @Inject(at = @At("RETURN"), method = "getResultItem", cancellable = true)
    public void unstablecrafting$afterGetResultItem(CallbackInfoReturnable<ItemStack> cir) {
        var replace = UCMod.replaceResult(this.getId());
        if (replace != null) {
            cir.setReturnValue(replace);
        }
    }

}
