package com.gpergrossi.aerogen.definitions.biomes;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class IslandBiomes {

	public static final IslandBiome FOREST = new IslandBiomeForest();
	public static final IslandBiome FOREST_BIRCH = new IslandBiomeForestBirch();
	public static final IslandBiome FOREST_CLEARING = new IslandBiomeForestClearing();
	public static final IslandBiome DESERT = new IslandBiomeDesert();
	public static final IslandBiome DESERT_DUNES = new IslandBiomeDesertDunes();
	public static final IslandBiome JUNGLE = new IslandBiomeJungle();
	public static final IslandBiome MESA = new IslandBiomeMesa();
	public static final IslandBiome SAVANNAH = new IslandBiomeSavannah();
	public static final IslandBiome COLD_TAIGA = new IslandBiomeColdTaiga();
	public static final IslandBiome ROOFED_FOREST = new IslandBiomeForestRoofed();
	
	public static boolean isVoid(int biomeID) {
		return Biome.getBiomeForId(biomeID) == Biomes.SKY;
	}
	
}
