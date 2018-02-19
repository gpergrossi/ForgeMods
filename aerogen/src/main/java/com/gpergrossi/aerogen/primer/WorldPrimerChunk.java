package com.gpergrossi.aerogen.primer;

import java.util.concurrent.locks.ReentrantLock;

import com.gpergrossi.aerogen.AeroGenMod;
import com.gpergrossi.aerogen.AeroGenerator;
import com.gpergrossi.util.data.Tuple2;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

public class WorldPrimerChunk {
	
	public static final int NOT_LOADED = 0;
	public static final int LOAD_STATUS_GENERATED = 1;
	public static final int LOAD_STATUS_POPULATED = 2;
	public static final int LOAD_STATUS_BIOMES = 4;
	
	/** This chunk's status upon being loaded, or NOT_LOADED (0) if it wasn't loaded */
	private int loadedStatus = NOT_LOADED;
	
	/** This chunk has been has been saved more recently than it has been modified. */
	private boolean isDirty = false;
	
	/** 
	 * This chunk is currently in the save queue. 
	 * It may still be there even if it has already been saved (isDirty == false). 
	 */
	boolean inSaveQueue = false;
	
	/** Last time this chunk was modified */
	long timestamp;
	
	/** The biomes for this chunk have been generated */
	private boolean hasBiomes;
	
	/** The terrain blocks for this chunk have been generated */
	private boolean isGenerated; 
	
	/** The region this chunk populates (+8, +8, +23, +23) has been populated */
	private boolean isPopulated;
	
	/** 
	 * This chunk and it's negative neighbors, (-1, -1), (-1, 0), (0, -1), and (0, 0) have 
	 * been populated and this chunk has been transferred to the Minecraft world 
	 */
	private boolean isCompleted;
	

	
	private ReentrantLock dataLock;		// Makes sure all modifications to the variables in this class are thread safe
	private ReentrantLock completeLock; // Prevents more than one call to getComplete from overlapping
	
	public final WorldPrimer world;
	public final int chunkX, chunkZ;

	private byte[] biomes;
	private ChunkPrimerExt blocks;
	
	private int[] heightmap;
	
	public WorldPrimerChunk(WorldPrimer world, int chunkX, int chunkZ) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		
		this.dataLock = new ReentrantLock(true);
		this.completeLock = new ReentrantLock(true);
		
		this.debugPrintChunkLog("constructed");
	}

	public static WorldPrimerChunk createProxy(WorldPrimer world, int chunkX, int chunkZ) {
		WorldPrimerChunk proxy = new WorldPrimerChunk(world, chunkX, chunkZ);
		proxy.hasBiomes = true;
		proxy.isGenerated = true;
		proxy.isPopulated = true;
		proxy.isCompleted = true;
		return proxy;
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

	public int getHeight(int x, int z) {
		if (isCompleted) {
			warnComplete("getHeight");
			Chunk mcChunk = world.getMinecraftWorld().getChunkFromChunkCoords(chunkX, chunkZ);
			return mcChunk.getHeightValue(x, z);
		}
		
		if (!this.isGenerated) throw new RuntimeException("Cannot retrieve height data before chunk is generated!");
		return heightmap[z << 4 | x] + 1;
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

			// Update heightmap
			if (y >= heightmap[index]) {
				if (state.getMaterial() != Material.AIR) {
					heightmap[index] = y;
				} else if (y == heightmap[index]) {
					heightmap[index] = blocks.findFirstBlockBelow(x, y, z);
				}
			}

			markDirty(true);
		} finally {
			dataLock.unlock();
		}
	}

	/**
	 * Generates this chunk's biomes array if it has not already been generated. Thread safe.
	 */
	private void generateBiomes() {
		debugPrintChunkLog("generateBiomes");
		
		if (dataLock.isLocked() && !dataLock.isHeldByCurrentThread()) AeroGenMod.log.info("!!!!!!!!!!!!!!");
		dataLock.lock();
		try {
			if (hasBiomes) return;
			
			this.biomes = new byte[256];
			world.getGenerator().generateBiomes(biomes, chunkX, chunkZ);
			
			hasBiomes = true;
			markDirty(true);
		} finally {
			dataLock.unlock();
		}
	}
	
	/**
	 * Generates this chunk's terrain blocks if they have not already been generated. Thread safe.
	 */
	private void generateBlocks() {
		debugPrintChunkLog("generateBlocks");
		
		if (dataLock.isLocked() && !dataLock.isHeldByCurrentThread()) AeroGenMod.log.info("!!!!!!!!!!!!!!");
		dataLock.lock();
		try {
			if (isGenerated) return;
			
			// Generate terrain blocks
			this.blocks = new ChunkPrimerExt();
			world.getGenerator().generateTerrain(blocks, chunkX, chunkZ);
			
			// Create the initial heightmap for the terrain tiles
			heightmap = new int[256];
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					heightmap[z << 4 | x] = blocks.findFirstBlockBelow(x, 255, z);
				}	
			}
			
			isGenerated = true;
			markDirty(true);
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
		debugPrintChunkLog("populate");
		
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
			markDirty(true);
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
		debugPrintChunkLog("getCompleted");
		
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
			this.biomes = null;
			this.blocks = null;

			world.save(this);
			
			return result;
		} finally {
			completeLock.unlock();
		}
	}
	
	
	
	public void markDirty(boolean dirty) {
		if (!dirty) {
			this.isDirty = false;
		} else {
			if (!isDirty) {
				isDirty = true;
				timestamp = System.currentTimeMillis();
				if (!inSaveQueue) {
					world.saveQueue.offer(new Tuple2<>(timestamp, this));
					inSaveQueue = true;
				}
			}
		}
	}
	
	public boolean needsSave() {
		return isDirty;
	}

	void debugPrintChunkLog(String string) {
		if (!WorldPrimer.DEBUG_PRINT_CHUNK_LOG) return;
		
		String id = this.toString();
		id = id.substring(id.indexOf("@"));
		
		String status = (this.hasBiomes ? "B" : "-") + (this.isGenerated ? "G" : "-") + (this.isPopulated ? "P" : "-") + (this.isCompleted ? "C" : "-");
		
		AeroGenMod.log.info("Chunk [x="+chunkX+", z="+chunkZ+"] ("+id+") "+status+":"+string);
	}
	
	void warnComplete(String methodName) {
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

	public boolean wasGeneratedOnLoad() {
		return (this.loadedStatus & LOAD_STATUS_GENERATED) != 0;
	}
	
	public boolean wasPopulatedOnLoad() {
		return (this.loadedStatus & LOAD_STATUS_POPULATED) != 0;
	}
	
	public boolean hadBiomesOnLoad() {
		return (this.loadedStatus & LOAD_STATUS_BIOMES) != 0;
	}

	public int getLoadedStatus() {
		return this.loadedStatus;
	}
	
	
	
	private static final int NBT_TAG_BYTE_ARRAY = 7;
	private static final int NBT_TAG_LIST = 9;
	private static final int NBT_TAG_COMPOUND = 10;
	
	public NBTTagCompound writeToNBT() {		
		if (this.isCompleted()) {
			return new NBTTagCompound();
		}
		
		final NBTTagCompound chunkCompoundTag = new NBTTagCompound();
		chunkCompoundTag.setInteger("DataVersion", 1);
		
		final NBTTagCompound levelCompoundTag = new NBTTagCompound();
		chunkCompoundTag.setTag("Level", levelCompoundTag);
		
		levelCompoundTag.setInteger("xPos", this.chunkX);
		levelCompoundTag.setInteger("zPos", this.chunkZ);
		levelCompoundTag.setBoolean("TerrainPopulated", this.isPopulated);
		if (this.hasBiomes) levelCompoundTag.setByteArray("Biomes", this.biomes);
		
		if (this.isGenerated) {
			levelCompoundTag.setIntArray("HeightMap", this.heightmap);
			levelCompoundTag.setTag("Sections", this.blocks.getSectionsNBT());
		}
		
		return chunkCompoundTag;
	}
	
	public static WorldPrimerChunk readFromNBT(WorldPrimer world, NBTTagCompound nbt) {
		if (nbt == null) return null;
				
		final NBTTagCompound levelNBT = nbt.getCompoundTag("Level");
		final int chunkX = levelNBT.getInteger("xPos");
		final int chunkZ = levelNBT.getInteger("zPos");
		
		WorldPrimerChunk chunk = new WorldPrimerChunk(world, chunkX, chunkZ);
				
		if (levelNBT.hasKey("Biomes", NBT_TAG_BYTE_ARRAY)) {
			chunk.biomes = levelNBT.getByteArray("Biomes");
			chunk.hasBiomes = true;
			chunk.loadedStatus |= LOAD_STATUS_BIOMES;
		}
		
		if (levelNBT.hasKey("Sections", NBT_TAG_LIST)) {
			chunk.blocks = ChunkPrimerExt.fromNBT(levelNBT.getTagList("Sections", NBT_TAG_COMPOUND));
			chunk.heightmap = levelNBT.getIntArray("HeightMap");
			chunk.isGenerated = true;
			chunk.loadedStatus |= LOAD_STATUS_GENERATED;
		} else {
			AeroGenMod.log.warn("Chunk "+chunkX+", "+chunkZ+" had no Sections NBT");
		}

		if (levelNBT.getBoolean("TerrainPopulated")) {
			chunk.isPopulated = true;
			chunk.loadedStatus |= LOAD_STATUS_POPULATED;
		}
				
		chunk.debugPrintChunkLog("loaded: "+chunk.loadedStatus);
		return chunk;
	}
	
}
