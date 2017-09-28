package com.gpergrossi.aerogen.generator.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import com.gpergrossi.aerogen.definitions.biomes.IslandBiomes;
import com.gpergrossi.aerogen.generator.GenerationPhase;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.util.data.Tuple2;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class WorldPrimerChunk {
	
	boolean hasBiomes;
	boolean isGenerated;
	boolean isPopulated;
	boolean isCompleted;
	
	ReentrantLock biomeLock, generateLock, populateLock, completeLock;
	
	WorldPrimer world;
	public final int chunkX, chunkZ;

	Biome[] biomes;
	ChunkPrimer blocks;
	
	int[] heightMap;
	int highestY;
	
	// When this number reaches 8, the chunk can be dumped
	public int numNeighborsCompleted = 0;
	
	public WorldPrimerChunk(WorldPrimer world, int chunkX, int chunkZ) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		
		this.biomeLock = new ReentrantLock(true);
		this.generateLock = new ReentrantLock(true);
		this.populateLock = new ReentrantLock(true);
		this.completeLock = new ReentrantLock(true);
	}
	
	public Biome getBiome(int i, int j) {
		return getBiomes()[j << 4 | i];
	}
	
	public Biome[] getBiomes() {
		generateBiomes();
		return biomes;
	}
	
	private void generateBiomes() {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		
		biomeLock.lock();
		try {
			if (hasBiomes) return;
			
	        int chunkMinX = (chunkX << 4);
	        int chunkMinZ = (chunkZ << 4);
	        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
	        
			this.biomes = new Biome[256];

			List<Region> regions = new ArrayList<>();
			world.generator.getRegionManager().getAll(regions, chunkBounds);

			// Fill void biomes
			for (Int2D.Mutable block : chunkBounds.getAllMutable()) {
				int index = chunkBounds.indexFor(block);
				biomes[index] = IslandBiomes.VOID;
						
				for (Region region : regions) {
					if (!region.getRegionPolygon().contains(block.x(), block.y())) continue;
					biomes[index] = region.getBiome().getVoidBiome();
				}
			}
			
			// Fill island biomes
			for (Region region : regions) {
				for (Island island : region.getIslands()) {
					island.provideBiomes(biomes, chunkBounds);
				}
			}
			
			hasBiomes = true;			
		} finally {
			biomeLock.unlock();
		}
	}

	public IBlockState getBlockState(int x, int y, int z) {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		if (!isGenerated) throw new RuntimeException("Not generated but getBlockState was called");
		if (x < 0 || x > 15 || z < 0 || z > 15) throw new IndexOutOfBoundsException("Invalid coordinates ("+x+","+y+","+z+")");
		if (y < 0 || y > getHeight()) return Blocks.AIR.getDefaultState();
		
		return blocks.getBlockState(x, y, z);
	}
	
	public void setBlockState(int x, int y, int z, IBlockState state) {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		if (!isGenerated) throw new RuntimeException("Not generated but setBlockState was called");
		if (x < 0 || x > 15 || z < 0 || z > 15) throw new IndexOutOfBoundsException("Invalid coordinates ("+x+","+y+","+z+")");
		if (y < 0 || y > world.getHeight()) return;
		
		blocks.setBlockState(x, y, z, state);

		int index = z << 4 | x;
		if (y < heightMap[index]) return;
		
		// Update heightmap
		if (state.getMaterial() != Material.AIR) {
			heightMap[index] = y;
		} else {
			int j;
			for (j = y-1; j > 0; j--) {
				if (blocks.getBlockState(x, j, z).getMaterial() == Material.AIR) continue;
				break;
			}
			heightMap[index] = j;
		}
	}
	
	private void generateBlocks() {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		
		generateLock.lock();
		try {
			if (isGenerated) return;
			
	        int chunkMinX = (chunkX << 4);
	        int chunkMinZ = (chunkZ << 4);
	        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
	        
			this.blocks = new ChunkPrimer();

			List<Region> regions = new ArrayList<>();
			world.generator.getRegionManager().getAll(regions, chunkBounds);
			
			// Fill the chunk with island blocks
			for (Region region : regions) {
				for (Island island : region.getIslands()) {
					island.provideChunk(this.blocks, chunkBounds);
				}
			}
			
			createHeightMap();
			
			isGenerated = true;
		} finally {
			generateLock.unlock();
		}
	}

	private void createHeightMap() {
		heightMap = new int[256];
		highestY = 0;
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = world.getHeight()-1; y >= 0; y--) {
					if (blocks.getBlockState(x, y, z).getMaterial() == Material.AIR) continue;
					if (y > highestY) highestY = y;
					heightMap[z << 4 | x] = y;
					break;
				}
			}	
		}
	}

	private void populate() {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		
		this.generateBlocks();
		
		populateLock.lock();
		try {
			if (isPopulated) return;
			
			// Make sure neighbors are generated. This provides a buffer of readable/writeable blocks
			// around the chunk being populated, allowing features to spill beyond this chunk's bounds.
			for (WorldPrimerChunk neighbor : getNeighbors()) {
				neighbor.generateBlocks();
			}
			
			// The regular populate offsets (+8, +8) are gone.
			// Instead, the 8 neighboring chunks are generated before populating a chunk.
			// This provides 16 blocks of available populate overhang.
			
			/**
			 * The Phase.PRE_POPULATE features are done inside WorldPrimerChunk.populate()
			 */
	        int chunkMinX = (chunkX << 4);
	        int chunkMinZ = (chunkZ << 4);
	        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);

			List<Region> regions = new ArrayList<>();
			world.generator.getRegionManager().getAll(regions, chunkBounds);
			
			long seed = world.generator.getChunkSeed(chunkX, chunkZ);
			Random random = new Random();
			
			for (Region region : regions) {
				for (Island island : region.getIslands()) {
					random.setSeed(seed);
					island.populateChunk(world, chunkBounds, random, GenerationPhase.PRE_POPULATE);
				}
			}
			
			/**
			 * The Phase.POST_POPULATE features are done inside ChunkGeneratorSky.
			 */			
			
			isPopulated = true;
		} finally {
			populateLock.unlock();
		}
	}
	
	public boolean isNeighborSameStatus(int offsetX, int offsetZ) {
		WorldPrimerChunk neighbor = peakNeighbor(offsetX, offsetZ);
		if (neighbor == null) return false;
		if (this.hasBiomes != neighbor.hasBiomes) return false;
		if (this.isGenerated != neighbor.isGenerated) return false;
		if (this.isPopulated != neighbor.isPopulated) return false;
		if (this.isCompleted != neighbor.isCompleted) return false;
		return true;
	}
	
	public WorldPrimerChunk peakNeighbor(int offsetX, int offsetZ) {
		return world.getPrimerChunk(chunkX+offsetX, chunkZ+offsetZ);
	}
	
	private Iterable<WorldPrimerChunk> getNeighbors() {
		return new Iterable<WorldPrimerChunk>() {
			public Iterator<WorldPrimerChunk> iterator() {
				return new Iterator<WorldPrimerChunk>() {
					int i = -1;
					int j = -1;
					
					@Override
					public WorldPrimerChunk next() {
						WorldPrimerChunk neighbor = world.getOrCreatePrimerChunk(chunkX+i, chunkZ+j);
						i++; if (i >= 2) { i = -1; j++; }
						if (i == 0 && j == 0) i = 1;
						return neighbor;
					}
					
					@Override
					public boolean hasNext() {
						return i <= 1 && j <= 1;
					}
				};
			}
		};
	}

	public Tuple2<ChunkPrimer, Biome[]> getCompleted() {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		
		this.generateBiomes();
		this.generateBlocks();
		this.populate();
		
		Tuple2<ChunkPrimer, Biome[]> result = new Tuple2<ChunkPrimer, Biome[]>(blocks, biomes);
		
		// Notify neighbors this tile is completed
		for (WorldPrimerChunk neighbor : getNeighbors()) {
			neighbor.completeLock.lock();
			try {
				neighbor.numNeighborsCompleted++;
				if (neighbor.numNeighborsCompleted >= 9) neighbor.dump();
			} finally {
				neighbor.completeLock.unlock();
			}
		}
		
		this.completeLock.lock();
		try {
			this.numNeighborsCompleted++;
			if (this.numNeighborsCompleted >= 9) this.dump();
		} finally {
			this.completeLock.unlock();
		}
		
		return result;
	}
	
	private void dump() {
		this.isCompleted = true;
		this.heightMap = null;
		this.biomes = null;
		this.blocks = null;
	}

	public int getHeight() {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		if (!this.isGenerated) return world.getHeight();
		return highestY;
	}

	public int getHeight(int i, int k) {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		if (!this.isGenerated) return world.getHeight();
		return heightMap[k << 4 | i];
	}

	public boolean hasBiomes() {
		return hasBiomes;
	}

	public boolean isGenerated() {
		return isGenerated;
	}

	public boolean isPopulated() {
		return isPopulated;
	}	

	public boolean isCompleted() {
		return isCompleted;
	}	
	
}
