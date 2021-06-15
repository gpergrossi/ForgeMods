package com.gpergrossi.aerogen;

import org.apache.logging.log4j.Logger;

import com.gpergrossi.aerogen.commands.CommandAeroMap;
import com.gpergrossi.aerogen.commands.CommandTest;
import com.gpergrossi.aerogen.generator.AeroGenerator;

import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(AeroGenMod.MODID)
@Mod.EventBusSubscriber
public class AeroGenMod {
	
    public static final String MODID = "aerogen";
    public static final String VERSION = "1.16.5-007";
    
    public static DimensionType DIMENSION_TYPE_SKY;
	public static Logger log;
    
    public static boolean REGISTER_DIMENSION = false;
    public static int SKY_DIMENSION_ID;
	public static DimensionType SKY_DIMENSION_TYPE;
	
	public static Configuration config;
	public static int numThreads;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		log = event.getModLog();
		
		config = new Configuration(event.getSuggestedConfigurationFile());
		syncConfig();
		
		// DEBUG
		numThreads = 1;
		// DEBUG
	}
	
    private static void syncConfig() {
		try {
			config.load();
			
			Property numThreadsProperty = config.get(Configuration.CATEGORY_GENERAL, "multithreadingMaxThreads", "0",
					"Number of threads to use in generation. 0 means all generation tasks are done by the main thread.");

			numThreads = numThreadsProperty.getInt();
		} finally {
			if (config.hasChanged()) config.save();
		}
	}

	@EventHandler
    public void init(FMLInitializationEvent event) {
        log.info("Loading "+MODID+" version "+VERSION);
        
        DIMENSION_TYPE_SKY = new DimensionTypeSky();
        
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
    	if (world.getWorldType() == AeroGenMod.DIMENSION_TYPE_SKY && (world.provider instanceof WorldProviderSurface)) return true;
    	return false;
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandTest());
		event.registerServerCommand(new CommandAeroMap());
	}
	
	@EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		for (AeroGenerator gen : AeroGenerator.getGenerators()) {
			gen.shutdown();
		}
	}
	
    @SubscribeEvent
	public static void onEntitySpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().getWorldType() != DIMENSION_TYPE_SKY) return;
		
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
    
	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
    	AeroGenerator generator = AeroGenerator.getGeneratorForWorld(event.world);
    	if (generator == null) return;
    	generator.onWorldTick(event);
	}
    
}
