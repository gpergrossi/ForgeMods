package com.gpergrossi.aerogen.generator;

import java.util.concurrent.locks.ReentrantLock;

import com.gpergrossi.util.data.Tuple2;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

public class WorldPrimerChunk {
	
	/** The biomes for this chunk have been generated */
	boolean hasBiomes;
	
	/** The terrain blocks for this chunk have been generated */
	boolean isGenerated; 
	
	/** The region this chunk populates (+8, +8, +23, +23) has been populated */
	boolean isPopulated;
	
	/** 
	 * This chunk and it's negative neighbors, (-1, -1), (-1, 0), (0, -1), and (0, 0) have 
	 * been populated and this chunk has been transferred to the Minecraft world 
	 */
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
		if (this.isCompleted) {
			Chunk mcChunk = world.generator.getWorld().getChunkFromChunkCoords(chunkX, chunkZ);
			return Biome.getBiomeForId(mcChunk.getBiomeArray()[j << 4 | i]);
		}
		return getBiomes()[j << 4 | i];
	}
	
	public Biome[] getBiomes() {
		generateBiomes();
		return biomes;
	}
	
	/**
	 * Generates this chunks biomes array if it has not already been generated. Thread safe.
	 */
	private void generateBiomes() {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		
		biomeLock.lock();
		try {
			if (hasBiomes) return;
			
			this.biomes = new Biome[256];
			world.generator.generateBiomes(biomes, chunkX, chunkZ);
			
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
		
		populateLock.lock();
		try {
			return blocks.getBlockState(x, y, z);
		} finally {
			populateLock.unlock();
		}
	}
	
	public void setBlockState(int x, int y, int z, IBlockState state) {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		if (!isGenerated) throw new RuntimeException("Not generated but setBlockState was called");
		
		if (x < 0 || x > 15 || z < 0 || z > 15) throw new IndexOutOfBoundsException("Invalid coordinates ("+x+","+y+","+z+")");
		if (y < 0 || y > world.getHeight()) return;
		
		populateLock.lock();
		try {			
			blocks.setBlockState(x, y, z, state);
	
			int index = z << 4 | x;
			if (y < heightMap[index]) return;
			
			// Update heightmap
			if (state.getMaterial() != Material.AIR) {
				heightMap[index] = y;
			} else if (y == heightMap[index]) {
				int j;
				for (j = y-1; j > 0; j--) {
					if (blocks.getBlockState(x, j, z).getMaterial() == Material.AIR) continue;
					break;
				}
				heightMap[index] = j;
			}
		} finally {
			populateLock.unlock();
		}
	}
	
	/**
	 * Generates this chunk's terrain blocks if they have not already been generated. Thread safe.
	 */
	private void generateBlocks() {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		generateLock.lock();
		try {
			if (isGenerated) return;
			
			// Generate terrain blocks
			this.blocks = new ChunkPrimer();
			world.generator.generateTerrain(blocks, chunkX, chunkZ);
			
			// Create the initial heightmap for the terrain tiles
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
			
			isGenerated = true;
		} finally {
			generateLock.unlock();
		}
	}

	/**
	 * Populates this chunks populate region (+8, +8, +24, +24) if it has not been populated already. Thread safe.
	 * Population of this chunks populate region requires generation of this chunk's terrain blocks and those of
	 * it positive 3-neighbors (+0, +1), (+1, +0), and (+1, +1).
	 */
	private void populate() {		
		populateLock.lock();
		try {
			if (isPopulated) return;
			
			// Make sure the positive neighbors are generated. This provides a buffer of readable/writeable blocks
			// around the chunk being populated, allowing features to spill beyond this chunk's bounds.
			this.generateBlocks();
			this.getNeighbor(0, 1).generateBlocks();
			this.getNeighbor(1, 0).generateBlocks();
			this.getNeighbor(1, 1).generateBlocks();
			
			// The populate region is offset by (+8, +8). This means features will be able to
			// place blocks overhanging their spawn location by up to (+7, +7) and down to (-8, -8)
			// This is an idea borrowed from Minecraft's populate functionality.
			world.generator.prePopulate(world, chunkX, chunkZ);
			
			isPopulated = true;
		} finally {
			populateLock.unlock();
		}
	}

	/**
	 * Completes this chunk and provides it to the MinecraftWorld. This method should only ever be called once.
	 * Since it is called only by the Minecraft thread dealing with world generation, it is not thread safe.<br /><br />
	 * 
	 * After the ChunkPrimer and Biome[] array have been returned, all internal data for this chunk is set to null
	 * and thus freed to the garbage collector. Because of this, an exception will be thrown if this method is 
	 * called more than once.
	 * 
	 * @return
	 */
	public Tuple2<ChunkPrimer, Biome[]> getCompleted() {		
		completeLock.lock();
		try {
			if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");

			this.generateBiomes();
			
			// Make sure the positive neighbors are generated. This provides a buffer of readable/writeable blocks
			// around the chunk being populated, allowing features to spill beyond this chunk's bounds.
			this.populate();
			this.getNeighbor(0, -1).populate();
			this.getNeighbor(-1, 0).populate();
			this.getNeighbor(-1, -1).populate();

			Tuple2<ChunkPrimer, Biome[]> result = new Tuple2<ChunkPrimer, Biome[]>(blocks, biomes);
			this.heightMap = null;
			this.biomes = null;
			this.blocks = null;
			
			this.isCompleted = true;
			
			return result;
		} finally {
			completeLock.unlock();
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
	
	public WorldPrimerChunk getNeighbor(int offsetX, int offsetZ) {
		return world.getOrCreatePrimerChunk(chunkX+offsetX, chunkZ+offsetZ);
	}
	
	public int getHeight() {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		if (!this.isGenerated) return world.getHeight();
		return highestY;
	}

	/**
	 * Supposed to return the air block above the first block with a light opacity > 0.
	 * However, the lazy implementation used for this "WorldPrimer" returns the air block
	 * above the first non-air block, because opacity checks in Minecraft require digging
	 * into block properties, which is unnecessary for pre-population.
	 */
	public int getHeight(int x, int z) {
		if (isCompleted) throw new RuntimeException("This chunks data has already been completed and dumped!");
		if (!this.isGenerated) return world.getHeight();
		return heightMap[z << 4 | x] + 1;
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
