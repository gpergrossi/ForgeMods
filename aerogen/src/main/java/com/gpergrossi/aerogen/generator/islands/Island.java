package com.gpergrossi.aerogen.generator.islands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.gpergrossi.aerogen.definitions.biomes.IslandBiome;
import com.gpergrossi.aerogen.generator.GenerationPhase;
import com.gpergrossi.aerogen.generator.decorate.terrain.ITerrainFeature;
import com.gpergrossi.aerogen.generator.decorate.terrain.TerrainFeatureBasin;
import com.gpergrossi.aerogen.generator.decorate.terrain.TerrainFeatureWaterfall;
import com.gpergrossi.aerogen.generator.islands.carve.IslandCaves;
import com.gpergrossi.aerogen.generator.islands.contour.IslandShape;
import com.gpergrossi.aerogen.generator.islands.extrude.IslandHeightmap;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.features.river.RiverWaterfall;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.util.geom.shapes.Rect;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.viewframe.IslandDebugRender;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class Island {
	
	public static final int LAYER_UNASSIGNED = Integer.MIN_VALUE;

	public IslandDebugRender debugRender;
	
	Region region;
	public final int regionIndex;
	long seed;
	Random random;
	List<IslandCell> cells;
	Map<String, Object> extensions;
	
	boolean grantFinished = false;	// finishGrant() has been called
	boolean initialized = false;	// Island shape creation
	boolean generated = false;		// Island heightmap and feature placement

	IslandBiome biome;
	IslandShape shape;
	IslandCaves caves;
	
	int altitude;
	IslandHeightmap heightmap;

	int altitudeLayer;
	List<ITerrainFeature> terrainFeatures;
	
	public Island(Region region, int regionIsleIndex, long seed) {
		this.region = region;
		this.regionIndex = regionIsleIndex;
		this.seed = seed;
		this.random = new Random(seed);
		this.cells = new ArrayList<>();
		this.altitudeLayer = LAYER_UNASSIGNED;
		this.extensions = new HashMap<>();
	}
	
	public void grantCell(IslandCell cell) {
		if (cell.getIsland() != this) throw new IllegalArgumentException("Cell does not belong to island");
		if (cells.contains(cell)) return;
		cells.add(cell);
	}

	public void finishGrant() {
		this.cells = Collections.unmodifiableList(cells);
		this.shape = new IslandShape(this, cells);
		grantFinished = true;
	}
	
	public synchronized void initialize() {
		if (!grantFinished) throw new IllegalStateException("Island needs to finishGrant() before being initialized");
		if (this.initialized) return;
		
		this.biome = region.getIslandBiome(this, random);
		this.biome.prepare(this);
		this.biome.generateShape(shape, random);

		this.altitude = region.getAltitude(this, random);
		
		this.initialized = true;
	}
	
	public synchronized void generate() {
		if (!initialized) throw new IllegalStateException("Island needs to initialize() before being generated");
		if (this.generated) return;
		
		this.heightmap = biome.getHeightMap(this);
		this.heightmap.initialize(random);

		this.terrainFeatures = new ArrayList<>();
		for (RiverWaterfall waterfall : shape.getWaterfalls()) {
			if (waterfall.isSource(this)) terrainFeatures.add(new TerrainFeatureWaterfall(waterfall, random));
			if (waterfall.isDestination(this)) terrainFeatures.add(new TerrainFeatureBasin(waterfall, random));
		}
		
		this.caves = new IslandCaves(this, shape.range.size() / 128, 5);
		while (this.caves.generate(random));
		
		this.generated = true;
	}
	
	public boolean hasWaterfall() {
		return !shape.getWaterfalls().isEmpty();
	}
	
	public void provideBiomes(Biome[] outputBiomes, Int2DRange chunkBounds) {
		Int2DRange overlap = chunkBounds.intersect(this.shape.range);
		
		if (overlap.isEmpty()) return;
		if (!initialized) this.initialize();
		
		for (Int2D tile : overlap.getAllMutable()) {
			if (!shape.contains(tile)) continue;
			int index = chunkBounds.indexFor(tile);
			outputBiomes[index] = this.biome.getMinecraftBiome();
		}
	}	
	
	public boolean provideChunk(ChunkPrimer primer, Int2DRange chunkBounds) {
		Int2DRange overlap = chunkBounds.intersect(this.shape.range);
		
		if (overlap.isEmpty() && !this.hasWaterfall()) return false;
		if (!initialized) this.initialize();
		if (!generated) this.generate();
		
		for (Int2D tile : overlap.getAllMutable()) {
			if (!shape.contains(tile)) continue;
			
			int startY = heightmap.getBottom(tile.x(), tile.y());
			int stopY = heightmap.getTop(tile.x(), tile.y());
			
			// Fill blocks from island bottom to island top
			for (int y = startY; y <= stopY; y++) {
				IBlockState block = biome.getBlockByDepth(this, tile.x(), y, tile.y());
				
				if ((y < stopY-2 || stopY > altitude+1) && caves.carve(tile.x(), y, tile.y())) block = Blocks.AIR.getDefaultState();
				
				primer.setBlockState(tile.x()-chunkBounds.minX, y, tile.y()-chunkBounds.minY, block);
			}
			
			// Place water from island surface to island sea level
			for (int y = stopY+1; y <= altitude; y++) {
				primer.setBlockState(tile.x()-chunkBounds.minX, y, tile.y()-chunkBounds.minY, biome.getWater());
			}
		}
		
		for (ITerrainFeature feature : terrainFeatures) {
			feature.provideChunk(primer, chunkBounds, new Random(this.seed));
		}
		
		return true;
	}
	
	public void populateChunk(World world, Int2DRange chunkBounds, Random random, GenerationPhase currentPhase) {
		Int2DRange overlap = chunkBounds.intersect(this.shape.range);
		
		if (overlap.isEmpty() && !this.hasWaterfall()) return;
		if (!initialized) this.initialize();
		if (!generated) this.generate();
	
		for (ITerrainFeature feature : terrainFeatures) {
			feature.populateChunk(world, chunkBounds, random, currentPhase);
		}
		
		biome.decorate(world, this, chunkBounds, overlap, random, currentPhase);
	}

	
	
	/* 
	 * Methods available after construction
	 */	
	
	@Override
	public String toString() {
		return "Island ("+region.getCoord().x()+", "+region.getCoord().y()+"):"+regionIndex+" (seed: "+seed+")";
	}
	
	public Region getRegion() {
		return region;
	}
	
	public int getRegionIndex() {
		return regionIndex;
	}
	
	public long getSeed() {
		return seed;
	}
	
	public int getAltitudeLayer() {
		return altitudeLayer;
	}

	public void setAltitudeLayer(int altitudeLayer) {
		this.altitudeLayer = altitudeLayer;
	}
	
	public void setExtension(String key, Object value) {
		this.extensions.put(key, value);
	}
	
	public Object getExtension(String key) {
		return this.extensions.get(key);
	}
	
	
	
	/* 
	 * Methods available after grant complete
	 */	
	
	public IslandShape getShape() {
		return shape;
	}
	
	public List<IslandCell> getCells() {
		return cells;
	}
	
	
	
	/* 
	 * Methods available after initialization
	 */	
	
	public boolean isInitialized() {
		return initialized;
	}

	public IslandBiome getBiome() {
		if (!this.initialized) throw new IllegalStateException("Cannot return island biome until it has been initialized");
		return biome;
	}
	
	public int getAltitude() {
		if (!this.initialized) throw new IllegalStateException("Cannot return island altitude until it has been initialized");
		return altitude;
	}
	
	
	
	/*
	 * Methods available after generation 
	 */
	
	public boolean isGenerated() {
		return generated;
	}
	
	public IslandHeightmap getHeightmap() {
		if (!this.generated) throw new IllegalStateException("Cannot get island height map until it has been generated");
		return heightmap;
	}

	public double getVolume(Int2DRange chunkRange) {
		if (!this.generated) throw new IllegalStateException("Cannot get island chunk volume until it has been generated");
		
		double volume = 0;
		for (Int2D.Mutable block : chunkRange.getAllMutable()) {
			volume += heightmap.getTop(block) - heightmap.getBottom(block) + 1;
		}
		
		return volume;
	}
	
	public BlockPos findGoodSpawnLocation() {
		if (!this.generated) throw new IllegalStateException("Cannot find island spawn until it has been generated");
		
		Rect bounds = shape.getBoundingBox();		
		Int2D.Mutable location = new Int2D.Mutable((int) bounds.centerX(), (int) bounds.centerY());
		
		Random random = new Random(this.seed);
		int numTries = 0;
		
		// Find a location on the island (if not already)
		while (!shape.contains(location) && numTries < 30) {
			Int2D tryLocation = shape.range.randomTile(random);
			location.x(tryLocation.x());
			location.y(tryLocation.y());
			numTries++;
		}
		
		if (numTries >= 30) return null;
		
		// Go as far inland as is easily find-able
		float edgeDistNow = shape.getEdgeDistance(location.x(), location.y());
		float edgeDistNeighbor;
		
		while (true) {
		
			// Is X+1 farther from the edge?
			edgeDistNeighbor = shape.getEdgeDistance(location.x()+1, location.y());
			if (edgeDistNeighbor > edgeDistNow) {
				edgeDistNow = edgeDistNeighbor;
				location.x(location.x()+1);
				continue;
			}
			
			// Is X-1 farther from the edge?
			edgeDistNeighbor = shape.getEdgeDistance(location.x()-1, location.y());
			if (edgeDistNeighbor > edgeDistNow) {
				edgeDistNow = edgeDistNeighbor;
				location.x(location.x()-1);
				continue;
			}

			// Is Z+1 farther from the edge?
			edgeDistNeighbor = shape.getEdgeDistance(location.x(), location.y()+1);
			if (edgeDistNeighbor > edgeDistNow) {
				edgeDistNow = edgeDistNeighbor;
				location.y(location.y()+1);
				continue;
			}

			// Is Z-1 farther from the edge?
			edgeDistNeighbor = shape.getEdgeDistance(location.x(), location.y()-1);
			if (edgeDistNeighbor > edgeDistNow) {
				edgeDistNow = edgeDistNeighbor;
				location.y(location.y()-1);
				continue;
			}
			
			// Can't find better?
			break;
		}
		
		return new BlockPos(location.x(), this.getHeightmap().getTop(location.x(), location.y()), location.y());
	}

}
