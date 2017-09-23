package com.gpergrossi.aerogen;

import com.gpergrossi.aerogen.commands.CommandAeroMap;
import com.gpergrossi.aerogen.commands.CommandTest;
import com.gpergrossi.aerogen.world.WorldProviderSky;

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
    public static final String VERSION = "1.12.1-007";
    
    public static boolean REGISTER_DIMENSION = true;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Loading "+MODID+" version "+VERSION);
        
        if (REGISTER_DIMENSION) {
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
			if (Math.random() > 0.9) event.setCanceled(true);
		}
	}
    
}
