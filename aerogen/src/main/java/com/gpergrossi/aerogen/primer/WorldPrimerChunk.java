package com.gpergrossi.aerogen.primer;

import java.util.concurrent.locks.ReentrantLock;

import com.gpergrossi.aerogen.AeroGenMod;
import com.gpergrossi.aerogen.AeroGenerator;
import com.gpergrossi.util.data.Tuple2;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
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

	/**
	 * This chunk has been has been saved more recently than it has been modified.
	 */
	boolean isSaved = false;
	
	public static final int NOT_LOADED = 0;
	public static final int LOADED_GENERATED = 1;
	public static final int LOADED_POPULATED = 2;
	public static final int LOADED_WITH_BIOMES = 4;
	int loadedStatus = NOT_LOADED;
	
	private ReentrantLock dataLock;		// Makes sure all modifications to the variables in this class are thread safe
	private ReentrantLock completeLock; // Prevents more than one call to getComplete from overlapping
	
	public final WorldPrimer world;
	public final int chunkX, chunkZ;

	byte[] biomes;
	ChunkPrimerExt blocks;
	
	int[] heightMap;
	
	protected static WorldPrimerChunk proxy(WorldPrimer world, int chunkX, int chunkZ) {
		WorldPrimerChunk proxy = new WorldPrimerChunk(world, chunkX, chunkZ);
		proxy.isCompleted = true;
		proxy.isGenerated = true;
		proxy.isPopulated = true;
		proxy.hasBiomes = true;
		proxy.isSaved = true;
		return proxy;
	}
	
	public WorldPrimerChunk(WorldPrimer world, int chunkX, int chunkZ) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		
		this.dataLock = new ReentrantLock(true);
		this.completeLock = new ReentrantLock(true);
	}
	
	private void warnComplete(String methodName) {
		StringBuilder warning = new StringBuilder();
		warning.append("WARNING! ").append(methodName).append(" on primer chunk that is already complete!\n");
		warning.append("This is typically caused by a feature populating blocks outside the allowed bounds.\n");
		warning.append("Stack trace:\n");
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for (int i = 2; i < trace.length; i++) {
			warning.append("   ").append(trace[i]).append("\n");
			if (trace[i].getClassName().equals(AeroGenerator.class.getName())) {
				final int remaining = trace.length-1 - i;
				warning.append("   ... ").append(remaining).append(" more");
				break;
			}
		}
		AeroGenMod.log.warn(warning.toString());
	}

	
	public byte getBiome(int i, int j) {
		if (this.isCompleted) {
			warnComplete("getBiome");
			Chunk mcChunk = world.getMinecraftWorld().getChunkFromChunkCoords(chunkX, chunkZ);
			return mcChunk.getBiomeArray()[j << 4 | i];
		}
		final byte biome = getBiomes()[j << 4 | i];
		return biome;
	}
	
	public byte[] getBiomes() {
		if (this.isCompleted) {
			Chunk mcChunk = world.getMinecraftWorld().getChunkFromChunkCoords(chunkX, chunkZ);
			return mcChunk.getBiomeArray();
		}
		generateBiomes();
		return biomes;
	}

	public IBlockState getBlockState(int x, int y, int z) {
		if (isCompleted) {
			warnComplete("getBlockState");
			Chunk mcChunk = world.getMinecraftWorld().getChunkFromChunkCoords(chunkX, chunkZ);
			return mcChunk.getBlockState(x, y, z);
		}
		
		if (!isGenerated) throw new RuntimeException("Not generated but getBlockState was called");
		if (x < 0 || x > 15 || z < 0 || z > 15) throw new IndexOutOfBoundsException("Invalid coordinates ("+x+","+y+","+z+")");
		if (y < 0 || y > world.getHeight()) return Blocks.AIR.getDefaultState();

		if (dataLock.isLocked() && !dataLock.isHeldByCurrentThread()) AeroGenMod.log.info("!!!!!!!!!!!!!!");
		dataLock.lock();
		try {
			return blocks.getBlockState(x, y, z);
		} finally {
			dataLock.unlock();
		}
	}
	
	public void setBlockState(int x, int y, int z, IBlockState state) {		
		if (isCompleted) {
			warnComplete("setBlockState");
			Chunk mcChunk = world.getMinecraftWorld().getChunkFromChunkCoords(chunkX, chunkZ);
			mcChunk.setBlockState(new BlockPos(x, y, z), state);
			return;
		}
		
		if (x < 0 || x > 15 || z < 0 || z > 15) throw new IndexOutOfBoundsException("Invalid coordinates ("+x+","+y+","+z+")");
		if (y < 0 || y > world.getHeight()) return;
		
		if (!isGenerated) throw new RuntimeException("Not generated but setBlockState was called");

		if (dataLock.isLocked() && !dataLock.isHeldByCurrentThread()) AeroGenMod.log.info("!!!!!!!!!!!!!!");
		dataLock.lock();
		try {
			blocks.setBlockState(x, y, z, state);
	
			int index = z << 4 | x;
			if (y < heightMap[index]) return;
			
			// Update heightmap
			if (state.getMaterial() != Material.AIR) {
				heightMap[index] = y;
			} else if (y == heightMap[index]) {
				heightMap[index] = blocks.findFirstBlockBelow(x, y, z);
			}
			
			isSaved = false;
		} finally {
			dataLock.unlock();
		}
	}
	
	/**
	 * Generates this chunk's biomes array if it has not already been generated. Thread safe.
	 */
	private void generateBiomes() {
		if (dataLock.isLocked() && !dataLock.isHeldByCurrentThread()) AeroGenMod.log.info("!!!!!!!!!!!!!!");
		dataLock.lock();
		try {
			if (hasBiomes) return;
			
			this.biomes = new byte[256];
			world.getGenerator().generateBiomes(biomes, chunkX, chunkZ);
			
			hasBiomes = true;
			isSaved = false;
		} finally {
			dataLock.unlock();
		}
	}
	
	/**
	 * Generates this chunk's terrain blocks if they have not already been generated. Thread safe.
	 */
	private void generateBlocks() {		
		if (dataLock.isLocked() && !dataLock.isHeldByCurrentThread()) AeroGenMod.log.info("!!!!!!!!!!!!!!");
		dataLock.lock();
		try {
			if (isGenerated) return;
			
			// Generate terrain blocks
			this.blocks = new ChunkPrimerExt();
			world.getGenerator().generateTerrain(blocks, chunkX, chunkZ);
			
			// Create the initial heightmap for the terrain tiles
			heightMap = new int[256];
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					heightMap[z << 4 | x] = blocks.findGroundBlockIdx(x, z);
				}	
			}
			
			isGenerated = true;
			isSaved = false;
		} finally {
			dataLock.unlock();
		}
	}

	/**
	 * Populates this chunks populate region (+8, +8, +24, +24) if it has not been populated already. Thread safe.
	 * Population of this chunks populate region requires generation of this chunk's terrain blocks and those of
	 * it positive 3-neighbors (+0, +1), (+1, +0), and (+1, +1).
	 */
	private void populate() {		
		if (dataLock.isLocked() && !dataLock.isHeldByCurrentThread()) AeroGenMod.log.info("!!!!!!!!!!!!!!");
		dataLock.lock();
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
			world.getGenerator().prePopulate(world, chunkX, chunkZ);
			
			isPopulated = true;
			isSaved = false;
		} finally {
			dataLock.unlock();
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
	public Tuple2<ChunkPrimer, byte[]> getCompleted() {
		if (completeLock.isLocked() && !completeLock.isHeldByCurrentThread()) AeroGenMod.log.info("!!!!!!!!!!!!!!");
		completeLock.lock();
		try {
			if (isCompleted) throw new RuntimeException("This chunk's data has already been completed and dumped!");

			this.generateBiomes();
			
			// Populate all chunks that would affect blocks in this chunk's range.
			// Since primer chunks populate a region (+8,+8) to (+24,+24), the chunks
			// that need to be populated are this chunk and its negative neighbors.
			this.populate();
			this.getNeighbor(0, -1).populate();
			this.getNeighbor(-1, 0).populate();
			this.getNeighbor(-1, -1).populate();

			Tuple2<ChunkPrimer, byte[]> result = new Tuple2<>(blocks, biomes);
			
			this.isCompleted = true;
			this.heightMap = null;
			this.biomes = null;
			this.blocks = null;
			this.isSaved = false;
			
			this.save();
			
			return result;
		} finally {
			completeLock.unlock();
		}
	}
	
	private void save() {
		this.world.save(this);
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
		return world.peakPrimerChunk(chunkX+offsetX, chunkZ+offsetZ);
	}
	
	public WorldPrimerChunk getNeighbor(int offsetX, int offsetZ) {
		return world.getOrCreatePrimerChunk(chunkX+offsetX, chunkZ+offsetZ);
	}

	public int getHeight(int x, int z) {
		if (isCompleted) {
			warnComplete("getHeight");
			Chunk mcChunk = world.getMinecraftWorld().getChunkFromChunkCoords(chunkX, chunkZ);
			return mcChunk.getHeightValue(x, z);
		}
		
		if (!this.isGenerated) throw new RuntimeException("Cannot retrieve height data before chunk is generated!");
		return heightMap[z << 4 | x] + 1;
	}
	
	public ChunkPrimerExt getBlocks() {
		return blocks;
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
	
	public boolean isSaved() {
		return isSaved;
	}

	public boolean wasLoadedGenerated() {
		return (this.loadedStatus & LOADED_GENERATED) != 0;
	}
	
	public boolean wasLoadedPopulated() {
		return (this.loadedStatus & LOADED_POPULATED) != 0;
	}
	
	public boolean wasLoadedWithBiomes() {
		return (this.loadedStatus & LOADED_WITH_BIOMES) != 0;
	}
	
}
