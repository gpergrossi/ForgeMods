package dev.mortus.aerogen.regions;

import java.util.List;
import java.util.Random;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public abstract class RegionBiome {

	public abstract String getName();
	public abstract List<Biome> getPossibleIslandBiomes();
	
	public Biome getRandomIslandBiome(Random random) {
		List<Biome> biomes = getPossibleIslandBiomes();
		if (biomes.size() == 0) return Biomes.SKY;
		return biomes.get(random.nextInt(biomes.size()));
	}
	
}
