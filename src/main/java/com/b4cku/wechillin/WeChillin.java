package com.b4cku.wechillin;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WeChillin.MODID)
public class WeChillin
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "wechillin";
    public static final String NAME = "We Chillin";
    public static final String VERSION = "1.0";
    public static final String ACCEPTED_VERSIONS = "[1.19.2]";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public WeChillin()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::loadComplete);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        MinecraftForge.EVENT_BUS.register(new HypothermiaEvent());
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info(NAME + "(" + VERSION + ") has loaded successfully!");
        }
    }

    @Mod.EventBusSubscriber
    public static class HypothermiaEvent {
        @SubscribeEvent
        public void playerTickEvent(TickEvent.PlayerTickEvent e) {
            Player player = e.player;
            Level level = player.getCommandSenderWorld();
            if (level.isClientSide) {
                return;
            }

            //drip check
            if (!player.canFreeze()) {
                return;
            }


            //snowing check
            if (!level.isRaining()) {
                return;
            }
            BlockPos playerPosition = player.blockPosition();
            if (!level.canSeeSky(playerPosition) ) {
                return;
            }
            //idk what this even does i stole this snowing check from alex's mobs, ily alex, big thanks my bro
            if (level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, playerPosition).getY() > playerPosition.getY()) {
                return;
            }
            if (level.getBiome(playerPosition).value().getPrecipitation() != Biome.Precipitation.SNOW) {
                return;
            }

            //campfire/fire/something check
            if (level.getBrightness(LightLayer.BLOCK, playerPosition) >= 12) {
                return;
            }
            //huge thanks to the potato man himself for his huge brain

            //we chillin boys
            player.setTicksFrozen( Math.min( player.getTicksRequiredToFreeze(), player.getTicksFrozen() + 3 ) );
            //this Math.min is there because we don't want to accidentally make our players have to defrost for half an hour after spending too long outside
            //it also adds +3 frozen ticks because the game will automatically remove 2 ticks every tick unless player is covered in powder snow
            //and this mod has to be lightweight so i didnt bother with mixins
            //as a result this will freeze players that are naked outside and covered with powdered snow REALLY fast
        }
    }
}
