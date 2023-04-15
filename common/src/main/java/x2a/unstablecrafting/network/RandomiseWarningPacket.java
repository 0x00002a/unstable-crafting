package x2a.unstablecrafting.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;
import x2a.unstablecrafting.UCMod;

import java.util.function.Supplier;

public class RandomiseWarningPacket {
    public final Component msg;
    @Nullable
    public final SoundEvent sound;

    public RandomiseWarningPacket(FriendlyByteBuf buf) {
        msg = buf.readComponent();
        SoundEvent ev;
        try {
            ev = buf.readById(Registry.SOUND_EVENT);
        } catch (IndexOutOfBoundsException e) {
            ev = null;
        }
        sound = ev;
    }

    public RandomiseWarningPacket(Component msg, @Nullable SoundEvent event) {
        this.msg = msg;
        this.sound = event;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeComponent(msg);
        if (sound != null) {
            buf.writeId(Registry.SOUND_EVENT, sound);
        }
    }

    public void apply(Supplier<NetworkManager.PacketContext> ctx) {
        try {
            if (ctx.get().getPlayer() instanceof LocalPlayer && UCMod.CONFIG.client.displayRandomiseWarnings.get()) {
                var player = ctx.get().getPlayer();
                player.sendSystemMessage(msg);
                if (sound != null) {
                    player.playSound(sound);
                }
            }
        } catch (Exception e) {
            UCMod.Log.error(e);
        }
    }
}
