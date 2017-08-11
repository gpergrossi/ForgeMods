package dev.mortus.aerogen.world.islands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import dev.mortus.aerogen.world.gen.caves.IslandCaves;
import dev.mortus.aerogen.world.gen.terrain.ITerrainFeature;
import dev.mortus.aerogen.world.gen.terrain.TerrainFeatureBasin;
import dev.mortus.aerogen.world.gen.terrain.TerrainFeatureWaterfall;
import dev.mortus.aerogen.world.islands.biomes.IslandBiome;
import dev.mortus.aerogen.world.regions.Region;
import dev.mortus.util.math.ranges.Int2DRange;
import dev.mortus.util.math.vectors.Int2D;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class Island {
	
	public static final int LAYER_UNASSIGNED = Integer.MIN_VALUE;

	Region region;
	int id;
	long seed;
	Random random;
	
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
	}

	public void grantCell(IslandCell cell) {
		if (cell.island != this) throw new IllegalArgumentException("Cell does not belong to island");
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

		if (Region.DEBUG_VIEW) {
			this.shape.erode(new IslandErosion(), random);
			return;
		}
		
		this.biome = region.getIslandBiome(this, random);
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
	
	public boolean provideChunk(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes) {
		Int2DRange chunkRange = new Int2DRange(chunkX*16, chunkZ*16, chunkX*16+15, chunkZ*16+15);
		Int2DRange overlap = chunkRange.intersect(this.shape.range);
		
		if (overlap.isEmpty() && !this.hasWaterfall()) return false;
		this.initialize();

		for (Int2D tile : overlap.getAllMutable()) {
			if (!shape.contains(tile)) continue;
			
			int startY = heightmap.getBottom(tile.x(), tile.y());
			int stopY = heightmap.getTop(tile.x(), tile.y());
			
			for (int y = startY; y <= stopY; y++) {
				IBlockState block = biome.getBlockByDepth(this, tile.x(), y, tile.y());
				
				if ((y < stopY-2 || stopY > altitude+1) && caves.carve(tile.x(), y, tile.y())) block = Blocks.AIR.getDefaultState();
				
				primer.setBlockState(tile.x()-chunkRange.minX, y, tile.y()-chunkRange.minY, block);
			}
			for (int y = stopY+1; y <= altitude; y++) {
				primer.setBlockState(tile.x()-chunkRange.minX, y, tile.y()-chunkRange.minY, biome.getWater());
			}
			
//			for (int y = heightmap.getEstimateDeepestY(); y < heightmap.getEstimateHighestY(); y++) {
//				if (y >= startY && y <= stopY) continue;
//				if ((y < stopY-2 || stopY >= altitude) && caves.carve(tile.x(), y, tile.y())) {
//					primer.setBlockState(tile.x()-chunkRange.minX, y, tile.y()-chunkRange.minY, Blocks.IRON_BLOCK.getDefaultState());
//				}
//			}
			
			biomes[chunkRange.indexFor(tile)] = biome;
		}
		
		for (ITerrainFeature feature : terrainFeatures) {
			feature.provideChunk(primer, chunkRange, random);
		}
		
		return true;
	}
	
	public void populateChunk(World world, int chunkX, int chunkZ, Random random) {
		Int2DRange chunkRange = new Int2DRange(chunkX*16+8, chunkZ*16+8, chunkX*16+23, chunkZ*16+23);
		Int2DRange overlap = chunkRange.intersect(this.shape.range);
		if (overlap.isEmpty()) return;
		this.initialize();
	
		for (ITerrainFeature feature : terrainFeatures) {
			feature.populateChunk(world, chunkRange, random);
		}
		
		biome.decorate(world, this, chunkRange, overlap, random);
	}

	@Override
	public String toString() {
		return "Island ("+region.getCoord().x+", "+region.getCoord().y+"):"+id+" (seed: "+seed+")";
	}

	public long getSeed() {
		return seed;
	}

	public int getAltitude() {
		return altitude;
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
