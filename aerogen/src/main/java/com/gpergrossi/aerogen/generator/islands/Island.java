package com.gpergrossi.aerogen.generator.islands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.gpergrossi.aerogen.definitions.biomes.IslandBiome;
import com.gpergrossi.aerogen.generator.data.IslandCell;
import com.gpergrossi.aerogen.generator.decorate.terrain.ITerrainFeature;
import com.gpergrossi.aerogen.generator.decorate.terrain.TerrainFeatureBasin;
import com.gpergrossi.aerogen.generator.decorate.terrain.TerrainFeatureWaterfall;
import com.gpergrossi.aerogen.generator.islands.carve.IslandCaves;
import com.gpergrossi.aerogen.generator.islands.contour.IslandShape;
import com.gpergrossi.aerogen.generator.islands.extrude.IslandHeightmap;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.features.RiverWaterfall;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class Island {
	
	public static final int LAYER_UNASSIGNED = Integer.MIN_VALUE;

	Region region;
	public final int id;
	
	long seed;
	Random random;
	
	protected Map<String, Object> extensions;
	
	boolean constructed = false;
	boolean initialized = false;

	IslandBiome biome;
	IslandShape shape;
	IslandCaves caves;
	
	protected int altitude;
	IslandHeightmap heightmap;

	int altitudeLayer;
	List<IslandCell> cells;
	List<ITerrainFeature> terrainFeatures;
	
	public Island(Region region, int regionIsleID, long seed) {
		this.region = region;
		this.id = regionIsleID;
		this.seed = seed;
		this.random = new Random(seed);
		this.cells = new ArrayList<>();
		this.altitudeLayer = LAYER_UNASSIGNED;
		this.extensions = new HashMap<>();
	}

	public void setExtension(String key, Object value) {
		this.extensions.put(key, value);
	}
	
	public Object getExtension(String key) {
		return this.extensions.get(key);
	}
	
	public void grantCell(IslandCell cell) {
		if (cell.getIsland() != this) throw new IllegalArgumentException("Cell does not belong to island");
		if (cells.contains(cell)) return;
		cells.add(cell);
	}

	public void finishGrant() {
		this.cells = Collections.unmodifiableList(cells);
		this.shape = new IslandShape(this, cells);
		constructed = true;
	}

	public int getAltitudeLayer() {
		return altitudeLayer;
	}

	public void setAltitudeLayer(int altitudeLayer) {
		this.altitudeLayer = altitudeLayer;
	}
	
	public void initialize() {
		if (!constructed) throw new IllegalStateException("Island needs to finishGrant() before being initialized");
		if (this.initialized) return;
		this.initialized = true;
		
		this.biome = region.getIslandBiome(this, random);
		this.biome.prepare(this);
		this.biome.generateShape(shape, random);

		this.altitude = region.getAltitude(this, random);
		this.heightmap = biome.getHeightMap(this);
		this.heightmap.initialize(random);

		this.terrainFeatures = new ArrayList<>();
		for (RiverWaterfall waterfall : shape.getWaterfalls()) {
			if (waterfall.isSource(this)) terrainFeatures.add(new TerrainFeatureWaterfall(waterfall, random));
			if (waterfall.isDestination(this)) terrainFeatures.add(new TerrainFeatureBasin(waterfall, random));
		}
		
		this.caves = new IslandCaves(this, shape.range.size() / 128, 5);
		while (this.caves.generate(random));
	}
	
	public boolean hasWaterfall() {
		return !shape.getWaterfalls().isEmpty();
	}
	
	public void provideBiomes(Int2DRange range, int[] biomeIDs) {
		Int2DRange overlap = range.intersect(this.shape.range);
		if (overlap.isEmpty()) return;
		this.initialize();
		
		for (Int2D tile : overlap.getAllMutable()) {
			if (!shape.contains(tile)) continue;
			int index = range.indexFor(tile);
			biomeIDs[index] = Biome.getIdForBiome(this.biome);
		}
	}
	
	public boolean provideChunk(ChunkPrimer primer, Biome[] biomes, Int2DRange chunkBounds) {
		Int2DRange overlap = chunkBounds.intersect(this.shape.range);
		
		if (overlap.isEmpty() && !this.hasWaterfall()) return false;
		this.initialize();

		for (Int2D tile : overlap.getAllMutable()) {
			if (!shape.contains(tile)) continue;
			
			int startY = heightmap.getBottom(tile.x(), tile.y());
			int stopY = heightmap.getTop(tile.x(), tile.y());
			
			for (int y = startY; y <= stopY; y++) {
				IBlockState block = biome.getBlockByDepth(this, tile.x(), y, tile.y());
				
				if ((y < stopY-2 || stopY > altitude+1) && caves.carve(tile.x(), y, tile.y())) block = Blocks.AIR.getDefaultState();
				
				primer.setBlockState(tile.x()-chunkBounds.minX, y, tile.y()-chunkBounds.minY, block);
			}
			for (int y = stopY+1; y <= altitude; y++) {
				primer.setBlockState(tile.x()-chunkBounds.minX, y, tile.y()-chunkBounds.minY, biome.getWater());
			}
			
//			for (int y = heightmap.getEstimateDeepestY(); y < heightmap.getEstimateHighestY(); y++) {
//				if (y >= startY && y <= stopY) continue;
//				if ((y < stopY-2 || stopY >= altitude) && caves.carve(tile.x(), y, tile.y())) {
//					primer.setBlockState(tile.x()-chunkRange.minX, y, tile.y()-chunkRange.minY, Blocks.IRON_BLOCK.getDefaultState());
//				}
//			}
			
			biomes[chunkBounds.indexFor(tile)] = biome;
		}
		
		for (ITerrainFeature feature : terrainFeatures) {
			feature.provideChunk(primer, chunkBounds, random);
		}
		
		return true;
	}
	
	public void populateChunk(World world, Int2DRange chunkBounds, Random random) {
		Int2DRange overlap = chunkBounds.intersect(this.shape.range);
		if (overlap.isEmpty() && !this.hasWaterfall()) return;
		this.initialize();
	
		for (ITerrainFeature feature : terrainFeatures) {
			feature.populateChunk(world, chunkBounds, random);
		}
		
		biome.decorate(world, this, chunkBounds, overlap, random);
	}

	@Override
	public String toString() {
		return "Island ("+region.getCoord().x()+", "+region.getCoord().y()+"):"+id+" (seed: "+seed+")";
	}

	public long getSeed() {
		return seed;
	}

	public int getAltitude() {
		return altitude;
	}
	
	public Region getRegion() {
		return region;
	}
	
	public IslandShape getShape() {
		return shape;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public IslandBiome getBiome() {
		return biome;
	}

	public IslandHeightmap getHeightmap() {
		return heightmap;
	}

}
