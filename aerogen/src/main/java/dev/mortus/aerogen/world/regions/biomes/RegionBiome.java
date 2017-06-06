package dev.mortus.aerogen.world.regions.biomes;

import java.util.List;
import java.util.Random;

import dev.mortus.aerogen.world.islands.biomes.IslandBiome;

public abstract class RegionBiome {

	public abstract String getName();
	public abstract List<IslandBiome> getPossibleIslandBiomes();
	
	public IslandBiome getRandomIslandBiome(Random random) {
		List<IslandBiome> biomes = getPossibleIslandBiomes();
		if (biomes.size() == 0) return null;
		return biomes.get(random.nextInt(biomes.size()));
	}
	
	public int getIslandMinAltitude() {
		return 64;
	}
	
	public int getIslandMaxAltitude() {
		return 128;
	}
	
	public int getRandomIslandAltitude(Random random, int minHeight, int maxHeight) {
		int altitude = random.nextInt(maxHeight-minHeight+1)+minHeight;
		altitude += random.nextInt(maxHeight-minHeight+1)+minHeight;
		return altitude / 2;
	}
	
}
