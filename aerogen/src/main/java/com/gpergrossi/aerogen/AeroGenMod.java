package com.gpergrossi.aerogen;

import com.gpergrossi.aerogen.commands.CommandAeroMap;
import com.gpergrossi.aerogen.commands.CommandTest;
import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.aerogen.world.WorldProviderSky;
import com.gpergrossi.aerogen.world.WorldTypeSky;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent.CreateSpawnPosition;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = AeroGenMod.MODID, version = AeroGenMod.VERSION, acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber
public class AeroGenMod {
	
    public static final String MODID = "aerogen";
    public static final String VERSION = "1.12.1-007";
    
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

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandTest());
		event.registerServerCommand(new CommandAeroMap());
	}
	
	@EventHandler
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if (!event.getWorld().getWorldType().getName().equals("aerogen_sky")) return;
		
		if (event.getEntity().isCreatureType(EnumCreatureType.MONSTER, true)) {
			if (Math.random() < 0.5) event.setCanceled(true);
		}
	}
	
    @SubscribeEvent
    public static void createSpawnPosition(CreateSpawnPosition event) {
    	World world = event.getWorld();
    	if (world.getWorldType() != WORLD_TYPE_SKY) return;
    	AeroGenerator generator = AeroGenerator.getGeneratorForWorld(world);
    	
    	boolean success = generator.createWorldSpawn();
    	if (!success) return;
    	
		event.setCanceled(true);
    }

}
