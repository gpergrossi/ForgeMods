package com.gpergrossi.aerogen;

import com.gpergrossi.aerogen.commands.CommandAeroMap;
import com.gpergrossi.aerogen.commands.CommandTest;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = AeroGenMod.MODID, version = AeroGenMod.VERSION, acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber
public class AeroGenMod {
	
    public static final String MODID = "aerogen";
    public static final String VERSION = "1.12.2-007";
    
    public static WorldType WORLD_TYPE_SKY;
	
    public static boolean REGISTER_DIMENSION = false;
    public static int SKY_DIMENSION_ID;
	public static DimensionType SKY_DIMENSION_TYPE;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Loading "+MODID+" version "+VERSION);
        
        WORLD_TYPE_SKY = new WorldTypeSky();
        
		if (REGISTER_DIMENSION) {
			SKY_DIMENSION_ID = DimensionManager.getNextFreeDimId();
			SKY_DIMENSION_TYPE = DimensionType.register("The Sky", "_sky", SKY_DIMENSION_ID, WorldProviderSky.class, false);
			
			WorldProviderSky worldProvider = new WorldProviderSky();
      		int dimensionID = worldProvider.getDimensionID();
      		DimensionManager.registerDimension(dimensionID, worldProvider.getDimensionType());
		}
    }

	public static boolean isWorldAerogen(World world) {
		if (world.isRemote) return false;
    	if (world.provider instanceof WorldProviderSky) return true;
    	if (world.getWorldType() == AeroGenMod.WORLD_TYPE_SKY && (world.provider instanceof WorldProviderSurface)) return true;
    	return false;
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandTest());
		event.registerServerCommand(new CommandAeroMap());
	}

    @SubscribeEvent
	public static void onEntitySpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().getWorldType() != WORLD_TYPE_SKY) return;
		
		if (event.getEntity().isCreatureType(EnumCreatureType.MONSTER, true)) {
			if (Math.random() < 0.5) event.setCanceled(true);
		}
	}
	
    @SubscribeEvent
    public static void onCreateWorldSpawn(WorldEvent.CreateSpawnPosition event) {
    	AeroGenerator generator = AeroGenerator.getGeneratorForWorld(event.getWorld());
    	if (generator == null) return;
    	generator.onCreateWorldSpawn(event);
    }
    
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
    	event.getWorld().getWorldType();
    	AeroGenerator generator = AeroGenerator.getGeneratorForWorld(event.getWorld());
    	if (generator == null) return;
    	generator.onWorldLoad(event);
    }
    
    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save event) {
    	AeroGenerator generator = AeroGenerator.getGeneratorForWorld(event.getWorld());
    	if (generator == null) return;
    	generator.onWorldSave(event);
    }
    
    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
    	AeroGenerator generator = AeroGenerator.getGeneratorForWorld(event.getWorld());
    	if (generator == null) return;
    	generator.onWorldUnload(event);
    }
    
}
