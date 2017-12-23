package com.gpergrossi.aerogen.definitions.regions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gpergrossi.aerogen.definitions.biomes.IslandBiome;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.features.IRegionFeature;
import com.gpergrossi.aerogen.generator.regions.features.river.RiverFeature;

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
	
	public List<IRegionFeature> getFeatures(Region region, Random random) {
		List<IRegionFeature> features = new ArrayList<>();
		
		int numRivers = getRandomNumberOfRivers(random);
		if (numRivers > 0) {
			features.add(new RiverFeature().setRiverCount(getRandomNumberOfRivers(random)));
		}
		
		return features;
	}
	
	public int getRandomNumberOfRivers(Random random) {
		return 0;
	}
	
}
