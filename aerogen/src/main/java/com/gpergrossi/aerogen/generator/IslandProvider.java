package com.gpergrossi.aerogen.generator;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.gpergrossi.aerogen.AeroGeneratorSettings;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandCell;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.RegionManager;
import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.util.geom.shapes.IShape;
import com.gpergrossi.util.geom.vectors.Double2D;
import com.gpergrossi.util.geom.vectors.Int2D;

public abstract class IslandProvider {
	
	boolean seedInitialized = false;
	long seed, seedX, seedY;
	
	public long getChunkSeed(int chunkX, int chunkZ) {
		final AeroGeneratorSettings settings = this.getSettings();
		if (!this.seedInitialized) {
			this.seed = settings.seed;
			final Random random = new Random(seed);
			this.seedX = random.nextLong() | 1; // "| 1" makes sure the number
			this.seedY = random.nextLong() | 1; // is not Long.MIN_VALUE again
		}
		return (long) chunkX * seedX + (long) chunkZ * seedY ^ seed;
	}

	public abstract AeroGeneratorSettings getSettings();
	
	/**
	 * Get a list of all loaded islands
	 * @param regions
	 */
	public void getIslandsInRegion(List<Island> output, Region region) {
		if (region.getProvider() != this) return;
		for (Island island : region.getIslands()) {
			if (!hasIsland(island)) continue;
			output.add(island);
		}
	}
	
	/**
	 * Add all regions in the given block range to the output list
	 * @param output
	 * @param minecraftBlockRangeXZ
	 */
	protected abstract void getRegions(List<Region> output, Int2DRange minecraftBlockRangeXZ);
	
	/**
	 * Return false if the given island is not allowed by this provider
	 * @param island
	 * @return
	 */
	protected abstract boolean hasIsland(Island island);

	public void getIslands(List<Island> output, IShape bounds) {
		List<Island> islands = new ArrayList<>();
		Int2DRange range = Int2DRange.fromRect(bounds.getBounds());
		getIslands(islands, range);
		
		for (Island island : islands) {
			for (IslandCell cell : island.getCells()) {
				if (cell.getPolygon().intersects(bounds)) {
					output.add(island);
					break;
				}
			}
		}
	}
	
	public void getIslands(List<Island> output, Int2DRange minecraftBlockRangeXZ) {
        List<Region> regions = new ArrayList<>();
		getRegions(regions, minecraftBlockRangeXZ);
		
		for (Region region : regions) {
			getIslandsInRegion(output, region);
		}
	}

	public Optional<Island> getIsland(Int2D minecraftBlockXZ) {
        Int2DRange searchBounds = new Int2DRange(minecraftBlockXZ, minecraftBlockXZ);
        List<Region> regions = new ArrayList<>();
		getRegions(regions, searchBounds);
		
		Double2D checkPos = new Double2D(minecraftBlockXZ.x(), minecraftBlockXZ.y());
		for (Region region : regions) {
			for (Island island : region.getIslands()) {
				if (!hasIsland(island)) continue;
				for (IslandCell cell : island.getCells()) {
					if (cell.getPolygon().contains(checkPos)) return Optional.of(cell.getIsland());
				}
			}
		}

		return Optional.empty();
	}

	public abstract Island getSpawnIsland();
	
	/**
	 * Get a list of all loaded regions
	 * @param regions
	 */
	public abstract void debugGetLoadedRegions(List<Region> output);
	
	
	
	
	public static class Simple extends IslandProvider {
		
		private AeroGeneratorSettings settings;
		private RegionManager regionManager;
		
		public Simple(AeroGeneratorSettings settings) {
			this.settings = settings;
			this.regionManager = new RegionManager(this);
		}

		@Override
		public AeroGeneratorSettings getSettings() {
			return settings;
		}

		@Override
		protected void getRegions(List<Region> output, Int2DRange minecraftBlockRangeXZ) {
			regionManager.getAll(output, minecraftBlockRangeXZ);
		}
		
		@Override
		public void debugGetLoadedRegions(List<Region> output) {
			try {
				Iterator<Region> iter = regionManager.getLoadedRegions().iterator();
				while (iter.hasNext()) {
					Region region = iter.next();
					output.add(region);
				}
			} catch (ConcurrentModificationException e) {}
		}

		@Override
		protected boolean hasIsland(Island island) {
			return true;
		}

		@Override
		public Island getSpawnIsland() {
			Island spawnIsland = null;
			float idealSpawnIslandSize = 2.1f; // 2 better than 3 better than 1... 
			
			// Find island closest to ideal size
			int spawnIslandSize = -1;
			List<Island> islands = new ArrayList<>();
			getIslandsInRegion(islands, regionManager.getSpawnRegion());
			for (Island island : islands) {
				int islandSize = island.getShape().getCells().size();
				if (spawnIslandSize == -1 || Math.abs(idealSpawnIslandSize-islandSize) < Math.abs(idealSpawnIslandSize-spawnIslandSize)) {
					spawnIslandSize = islandSize;
					spawnIsland = island;
				}
			}
			
			return spawnIsland;
		}

	}
	
}
