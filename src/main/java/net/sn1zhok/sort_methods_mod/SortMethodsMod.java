package net.sn1zhok.sort_methods_mod;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SortMethodsMod.MOD_ID)
public class SortMethodsMod
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "sort_methods_mod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();


    public SortMethodsMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code

    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("createarraycontainer")
                .requires(source -> source.hasPermission(2)) // Вимагає прав адміністратора
                .then(Commands.argument("length", IntegerArgumentType.integer(1)) // Аргумент довжини
                        .then(Commands.argument("height", IntegerArgumentType.integer(1)) // Аргумент висоти
                                .executes(context -> createContainer(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "length"),
                                        IntegerArgumentType.getInteger(context, "height")
                                )))));
    }

    private int createContainer(CommandSourceStack source, int length, int height) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException(); // Отримує гравця
        BlockPos startPos = player.blockPosition().relative(player.getDirection()).above(); // Початкова позиція перед гравцем
        Direction direction = player.getDirection(); // Напрямок, у який дивиться гравець
        Direction perpendicularRight = direction.getClockWise(); // Напрямок праворуч
        Direction perpendicularLeft = direction.getCounterClockWise(); // Напрямок ліворуч


        // Цикли для створення масиву блоків
        for (int y = 0; y < height + 2; y++) {
            for (int x = 0; x < length + 2; x++) {
                BlockPos centerPos = startPos.relative(direction, x).above(y);
                BlockPos rightPos = centerPos.relative(perpendicularRight);
                BlockPos leftPos = centerPos.relative(perpendicularLeft);

                if (y == 0) {
                    boolean isEdge = (x == 0 || x == length + 1);
                    boolean isEven = (x % 2 == 0);

                    // Встановлюємо блоки для нижнього рівня
                    player.level().setBlock(centerPos, isEdge
                                    ? Blocks.BLACK_CONCRETE.defaultBlockState()
                                    : Blocks.GRAY_CONCRETE.defaultBlockState(),
                            3);
                    player.level().setBlock(rightPos, isEven
                                    ? Blocks.YELLOW_CONCRETE.defaultBlockState()
                                    : Blocks.BLACK_CONCRETE.defaultBlockState(),
                            3);
                    player.level().setBlock(leftPos, isEven
                                    ? Blocks.YELLOW_CONCRETE.defaultBlockState()
                                    : Blocks.BLACK_CONCRETE.defaultBlockState(),
                            3);
                } else {
                    // Встановлюємо блоки для всіх інших рівнів
                    if (x == 0 || x == length + 1) {
                        player.level().setBlock(centerPos, Blocks.GLASS.defaultBlockState(), 3);
                    }
                    player.level().setBlock(rightPos, Blocks.GLASS.defaultBlockState(), 3);
                    player.level().setBlock(leftPos, Blocks.GLASS.defaultBlockState(), 3);
                }
            }
        }

//        BlockPos targetPos = startPos.relative(direction, x).above(y); // Позиція блоку
//        player.level().setBlock(targetPos, Blocks.GRAY_CONCRETE.defaultBlockState(), 3); // Ставить блок

        source.sendSuccess(() -> Component.nullToEmpty("Created array of blocks with length " + length + " and height " + height), true);
        return Command.SINGLE_SUCCESS;
    }
}
