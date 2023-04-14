package x2a.unstablecrafting;

import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class ItemStackKey {
    final ItemStack stack;


    public ItemStackKey(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ItemStackKey other) {
            return other.stack.is(other.stack.getItem()) && other.stack.getCount() == stack.getCount();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int c = stack.getCount();
        var hash = stack.getItem().hashCode();
        return (hash << 6 | c);
    }
}
