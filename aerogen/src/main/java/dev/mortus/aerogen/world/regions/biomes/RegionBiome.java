package dev.mortus.aerogen.world.regions.biomes;

import java.util.List;
import java.util.Random;

import dev.mortus.aerogen.world.islands.biomes.IslandBiome;
import dev.mortus.aerogen.world.islands.biomes.IslandBiomes;

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
	
	public int getRandomNumberOfRivers(Random random) {
		return 1;
	}
	
	/**
	 * The percentage of remaining cells the island gatherer is allowed to claim per island.
	 * Larger numbers make bigger islands. Smaller numbers make more numerous tiny islands.
	 * 0.5 is default. 1.0 means the entire region is one island.
	 * @return
	 */
	public double getIslandCellGatherPercentage() {
		return 0.5;
	}
	
	public double getCellSizeMultiplier() {
		return 1;
	}
	
	public IslandBiome getVoidBiome() {
		return IslandBiomes.VOID;
	}
	
}
