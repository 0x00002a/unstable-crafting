package x2a.unstablecrafting;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import x2a.unstablecrafting.network.RandomiseWarningPacket;
import x2a.unstablecrafting.overrides.RecipeClassOverride;
import x2a.unstablecrafting.overrides.RecipeOverride;

import java.time.Duration;
import java.util.*;

public class UCMod {

    public static final String MOD_ID = "unstablecrafting";
    public static final Logger Log = LogManager.getLogger("Unstable Crafting");
    private static final UCOverrides RECIPE_OVERRIDES = new UCOverrides();

    private static final Random RAND = new Random();
    public static Map<ResourceLocation, ItemStack> RECIPE_REDIRECTS = new HashMap<>();
    public static final NetworkChannel RAND_WARNING_CHAN = NetworkChannel.create(new ResourceLocation(MOD_ID,
            "randomise_time_update"));

    public static UCConfig CONFIG;

    static void randomiseRecipes(MinecraftServer server) {
        var target = server.getRecipeManager();
        var targetRecipes =
                target.getRecipes()
                        .stream()
                        .filter(RECIPE_OVERRIDES::matches)
                        .toList();
        var outputs = new ArrayList<>(targetRecipes);
        outputs.sort(Comparator.comparing(Recipe::getId)); // this is so our rng is seed determined
        Collections.shuffle(outputs, RAND);

        for (var recipe : targetRecipes) {
            var repl = outputs.remove(outputs.size() - 1)
                    .getResultItem();
            RECIPE_OVERRIDES.apply(recipe, repl);
        }
        var pkt = new ClientboundUpdateRecipesPacket(target.getRecipes());
        var warnPkt = new RandomiseWarningPacket(Component.translatable("message.unstablecrafting.random_time")
                .withStyle(ChatFormatting.DARK_AQUA),
                SoundEvents.BELL_RESONATE);
        server.getPlayerList()
                .getPlayers()
                .forEach(p -> {
                    p.connection.send(pkt);
                    p.getRecipeBook()
                            .sendInitialRecipeBook(p);
                    RAND_WARNING_CHAN.sendToPlayer(p, warnPkt);
                });
    }

    public static long msToTicks(long ms) {
        return ms / 50;
    }


    public static long secsToTicks(long secs) {
        return secs * 20;
    }

    public static long minsToTicks(long mins) {
        return msToTicks(mins * 60 * 1000);
    }

    public static long ticksToMins(long ticks) {
        return ticks / 20 / 60;
    }

    public static long ticksToSecs(long ticks) {
        return ticks / 20;
    }

    @Nullable
    public static ItemStack replaceResult(ResourceLocation orig) {
        var replace = UCMod.RECIPE_REDIRECTS.get(orig);
        if (replace != null) {
            return replace.copy();
        }
        return null;
    }

    public static void init(UCConfig config) {
        CONFIG = config;
        RAND_WARNING_CHAN.register(RandomiseWarningPacket.class, RandomiseWarningPacket::encode, RandomiseWarningPacket::new,
                RandomiseWarningPacket::apply);
        Log.info("Welcome to the world of recipes with half lives");
        applyVanillaOverrides(RECIPE_OVERRIDES);

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("uc")
                    .then(Commands.literal("time")
                            .executes(ctx -> {
                                var tick = ctx.getSource()
                                        .getServer()
                                        .getTickCount();
                                var interval = CONFIG.server.ticksPerRandomise.get();
                                var timeToTick = interval - (tick % interval);
                                ctx.getSource()
                                        .getPlayer()
                                        .sendSystemMessage(Component.translatable("message" +
                                                                ".unstablecrafting.time",
                                                        ticksToMins(timeToTick) / 60, ticksToMins(timeToTick),
                                                        ticksToSecs(timeToTick))
                                                .withStyle(ChatFormatting.GREEN));
                                return 0;
                            }))
                    .then(Commands.literal("decay")
                            .executes(context -> {
                                randomiseRecipes(context.getSource()
                                        .getServer());
                                return 0;
                            })));
        });

        LifecycleEvent.SERVER_STARTING.register(server -> {
            RAND.setSeed(server.getWorldData()
                    .worldGenSettings()
                    .seed());
            randomiseRecipes(server);
            Log.info("Recipes have started to decay");
        });
        TickEvent.SERVER_POST.register(inst -> {
            var tickCount = inst.getTickCount();
            var interval = CONFIG.server.ticksPerRandomise.get();
            var timeToRandom = interval - (tickCount % interval);
            if (tickCount % interval == 0) {
                randomiseRecipes(inst);
            }
            inst.getPlayerList()
                    .getPlayers()
                    .forEach(player -> {
                        if (timeToRandom <= secsToTicks(5) && timeToRandom % 20 == 0) {
                            RAND_WARNING_CHAN.sendToPlayer(player, new RandomiseWarningPacket(Component.translatable("message.unstablecrafting.randomise_warn_secs",
                                            ticksToSecs(timeToRandom))
                                    .withStyle(ChatFormatting.DARK_RED),
                                    SoundEvents.NOTE_BLOCK_CHIME));
                        } else {
                            ChatFormatting colour = null;
                            if (timeToRandom == minsToTicks(1)) {
                                colour = ChatFormatting.RED;
                            } else if (timeToRandom == minsToTicks(5)) {
                                colour = ChatFormatting.YELLOW;
                            } else if (timeToRandom == minsToTicks(10)) {
                                colour = ChatFormatting.GREEN;
                            }
                            if (colour != null) {
                                RAND_WARNING_CHAN.sendToPlayer(player, new RandomiseWarningPacket(Component.translatable("message" +
                                                        ".unstablecrafting" +
                                                        ".randomise_warn",
                                                ticksToMins(timeToRandom))
                                        .withStyle(colour), null));
                            }
                        }
                    });
        });

    }
    private static void applyVanillaOverrides(UCOverrides reg) {
        RecipeOverride[] overrides = {
                new RecipeClassOverride<>(ShapelessRecipe.class, (recipe, replace) -> {
                    recipe.result = replace;
                }),
                new RecipeClassOverride<>(ShapedRecipe.class, (recipe, replace) -> {
                    recipe.result = replace;
                }),
                new RecipeClassOverride<>(SingleItemRecipe.class, (recipe, replace) -> {
                    recipe.result = replace;
                }),
                new RecipeClassOverride<>(AbstractCookingRecipe.class, (recipe, replace) -> {
                    recipe.result = replace;
                }),
        };
        for (var override : overrides) {
            reg.register(override);
        }
    }
}
