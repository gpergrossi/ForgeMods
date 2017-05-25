package dev.mortus.aerogen.world.islands.biomes;

import net.minecraft.world.biome.Biome;

public class IslandBiomes {

	public static void registerBiomes(int baseBiomeIndex) {
		IslandBiomes.register(baseBiomeIndex+0, "void", VOID);
		IslandBiomes.register(baseBiomeIndex+1, "test", TEST);
	}
	
	public static void register(int index, String name, IslandBiome biome) {
		Biome.registerBiome(index, name, biome);
	}

	public static final IslandBiome VOID = new IslandBiomeVoid();
	public static final IslandBiome TEST = new IslandBiomeTest();
	
}
