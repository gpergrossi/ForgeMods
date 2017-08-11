package dev.mortus.aerogen.world.islands.biomes;

import dev.mortus.aerogen.world.regions.Region;
import jline.internal.Log;
import net.minecraft.world.biome.Biome;

public class IslandBiomes {

	public static void registerBiomes(int baseBiomeIndex) {
		register(baseBiomeIndex+0, "sky_void", VOID);
		register(baseBiomeIndex+1, "sky_forest", FOREST);
		register(baseBiomeIndex+2, "sky_forest_clearing", FOREST_CLEARING);
		register(baseBiomeIndex+3, "sky_desert", DESERT);
		register(baseBiomeIndex+4, "sky_desert_dunes", DESERT_DUNES);
		register(baseBiomeIndex+5, "sky_jungle", JUNGLE);
	}
	
	public static void register(int index, String name, IslandBiome biome) {
		if (Region.DEBUG_VIEW) return;
		biome.biomeID = index;
		Biome.registerBiome(index, name, biome);
	}

	public static boolean isVoid(int id) {
		Biome biome = Biome.getBiomeForId(id);
		if (!(biome instanceof IslandBiome)) {
			Log.warn("Invalid island biome!");
			return true;
		}
		
		IslandBiome islandBiome = (IslandBiome) biome;
		return islandBiome.isVoid();
	}
	
	public static final IslandBiome VOID = new IslandBiomeVoid();
	public static final IslandBiome FOREST = new IslandBiomeForest();
	public static final IslandBiome FOREST_CLEARING = new IslandBiomeForestClearing();
	public static final IslandBiome DESERT = new IslandBiomeDesert();
	public static final IslandBiome DESERT_DUNES = new IslandBiomeDesertDunes();
	public static final IslandBiome JUNGLE = new IslandBiomeJungle();
	
	
}
