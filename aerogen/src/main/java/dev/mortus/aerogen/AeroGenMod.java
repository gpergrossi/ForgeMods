package dev.mortus.aerogen;

import dev.mortus.aerogen.commands.CommandTest;
import dev.mortus.aerogen.world.WorldProviderSky;
import dev.mortus.aerogen.world.islands.biomes.IslandBiomes;
import net.minecraft.entity.EnumCreatureType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = AeroGenMod.MODID, version = AeroGenMod.VERSION)
public class AeroGenMod {
	
    public static final String MODID = "aerogen";
    public static final String VERSION = "0.1.1";
    
    public static boolean REGISTER_DIMENSION = true;
    public static int BASE_BIOME_INDEX = 168;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Loading "+MODID+" version "+VERSION);
        
        if (REGISTER_DIMENSION) {
            WorldProviderSky worldProvider = new WorldProviderSky();
            int dimensionID = worldProvider.getDimensionID();
        	DimensionManager.registerDimension(dimensionID, worldProvider.getDimensionType());
        }
        
        IslandBiomes.registerBiomes(BASE_BIOME_INDEX);
        
    }

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandTest());
	}
	
	@EventHandler
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if (!event.getWorld().getWorldType().getName().equals("aerogen_sky")) return;
		
		if (event.getEntity().isCreatureType(EnumCreatureType.MONSTER, true)) {
			if (Math.random() > 0.9) event.setCanceled(true);
		}
	}
    
}
