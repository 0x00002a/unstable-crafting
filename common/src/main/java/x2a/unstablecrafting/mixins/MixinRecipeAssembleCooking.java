package x2a.unstablecrafting.mixins;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import x2a.unstablecrafting.UCMod;


@Mixin(value = {AbstractCookingRecipe.class})
public class MixinRecipeAssembleCooking {
    @Inject(at = @At("RETURN"), method = "assemble",
            cancellable = true)
    public void unstablecrafting$afterAssemble(CallbackInfoReturnable<ItemStack> cir) {
        var replaced = UCMod.replaceResult(cir.getReturnValue());
        if (replaced != null) {
            cir.setReturnValue(replaced);
        }
    }
}
