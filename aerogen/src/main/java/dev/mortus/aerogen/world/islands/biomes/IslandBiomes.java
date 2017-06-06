package dev.mortus.aerogen.world.islands.biomes;

import dev.mortus.aerogen.world.regions.Region;
import net.minecraft.world.biome.Biome;

public class IslandBiomes {

	public static void registerBiomes(int baseBiomeIndex) {
		register(baseBiomeIndex+0, "sky_void", VOID);
		register(baseBiomeIndex+1, "sky_forest", FOREST);
	}
	
	public static void register(int index, String name, IslandBiome biome) {
		if (Region.DEBUG_VIEW) return;
		biome.biomeID = index;
		Biome.registerBiome(index, name, biome);
	}

	public static final IslandBiome VOID = new IslandBiomeVoid();
	public static final IslandBiome FOREST = new IslandBiomeForest();
	
}
