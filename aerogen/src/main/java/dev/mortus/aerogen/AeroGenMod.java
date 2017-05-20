package dev.mortus.aerogen;

import dev.mortus.aerogen.commands.CommandTest;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = AeroGenMod.MODID, version = AeroGenMod.VERSION)
public class AeroGenMod {
	
    public static final String MODID = "aerogen";
    public static final String VERSION = "0.0.7";
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Loading "+MODID+" version "+VERSION);

        WorldProviderSky worldProvider = new WorldProviderSky();
        int dimensionID = worldProvider.getDimensionID();
        
        DimensionManager.registerDimension(dimensionID, worldProvider.getDimensionType());
        
    }

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandTest());
	}
    
}
