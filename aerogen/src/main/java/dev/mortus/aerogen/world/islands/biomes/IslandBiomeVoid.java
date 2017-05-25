package dev.mortus.aerogen.world.islands.biomes;

import net.minecraft.world.biome.Biome.BiomeProperties;

public class IslandBiomeVoid extends IslandBiome {
	
	private static BiomeProperties getBiomeProperties() {
		BiomeProperties properties = new BiomeProperties("Void");
		properties.setWaterColor(0x0077FF);
		return properties;
	}
	
	public IslandBiomeVoid() {
		super(getBiomeProperties());
	}
	
}