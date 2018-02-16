package com.gpergrossi.aerogen.primer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.gpergrossi.aerogen.AeroGenMod;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.util.io.ndmf.NamedDataMapFile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;

public class WorldPrimerChunkLoader implements IThreadedFileIO {
		
    private final Map<Int2D, NBTTagCompound> chunksToSave = Maps.newConcurrentMap();
    private final Set<Int2D> currentlySaving = Collections.newSetFromMap(Maps.newConcurrentMap());

	private final WorldPrimer world;
	private final NamedDataMapFile<Int2D, NBTTagCompound> saveFile;
	
	public WorldPrimerChunkLoader(WorldPrimer world) {
		this.world = world;
		
		File worldDir = world.getMinecraftWorld().getSaveHandler().getWorldDirectory();
		File primerSave = new File(worldDir, "AerogenPrimerChunks");
		
		this.saveFile = new NamedDataMapFile<>(Int2DStreamHandler.INSTANCE, NBTStreamHandler.INSTANCE, 4096);
		try {
			this.saveFile.open(primerSave);
			System.out.println("AerogenPrimerChunks contains "+this.saveFile.debugGetStoredNames().size()+" saved chunks");
		} catch (IOException e) {
			AeroGenMod.log.error("Could not open AerogenPrimerChunks file for world \""+world.getMinecraftWorld().getWorldInfo().getWorldName()+"\"");
		}
	}
	
	public boolean hasChunk(int chunkX, int chunkZ) {
		if (!saveFile.isOpen()) throw new RuntimeException("Could not request chunk because AerogenPrimerChunks file is not open!");
		return saveFile.has(new Int2D(chunkX, chunkZ));
	}
	
	public WorldPrimerChunk loadChunk(int chunkX, int chunkZ) {
		try {
			world.getMinecraftWorld().checkSessionLock();
			
			Int2D chunkPos = new Int2D(chunkX, chunkZ);
			NBTTagCompound chunkCompoundTag = readChunkData(chunkPos);
			return readChunkFromNBT(world, chunkCompoundTag);
		} catch (MinecraftException exception) {
			AeroGenMod.log.error("Failed to load primer chunk", exception);
		}
		return null;
	}

	public void saveChunk(WorldPrimerChunk chunk) {
		try {
			world.getMinecraftWorld().checkSessionLock();

			Int2D pos = new Int2D(chunk.chunkX, chunk.chunkZ);
			
			NBTTagCompound chunkCompoundTag = writeChunkToNBT(chunk);
			this.addChunkToPending(pos, chunkCompoundTag);
			
		} catch (Exception exception) {
			AeroGenMod.log.error("Failed to save primer chunk", exception);
		}
	}
	
	private void addChunkToPending(Int2D chunkPos, NBTTagCompound chunkCompoundTag) {
		if (!this.currentlySaving.contains(chunkPos)) {
			this.chunksToSave.put(chunkPos, chunkCompoundTag);
		}
		ThreadedFileIOBase.getThreadedIOInstance().queueIO(this);
	}

	@Override
	/**
	 * Save one chunk in the saving queue.
	 * Return true if there are more chunks to save.
	 */
	public boolean writeNextIO() {
        if (this.chunksToSave.isEmpty()) return false;
        
        final Int2D chunkPos = this.chunksToSave.keySet().iterator().next();

		try {
			this.currentlySaving.add(chunkPos);
			final NBTTagCompound nbtTagCompound = this.chunksToSave.remove(chunkPos);
			this.writeChunkData(chunkPos, nbtTagCompound);
		} finally {
			this.currentlySaving.remove(chunkPos);
		}

        return true;
	}

    public void flush() {
        while (this.writeNextIO());
    }
	
	private void writeChunkData(Int2D chunkPos, NBTTagCompound nbtTagCompound) {
		if (!saveFile.isOpen()) throw new RuntimeException("Could not save chunk because AerogenPrimerChunks file is not open!");
		if (nbtTagCompound.hasNoTags()) {
			saveFile.set(chunkPos, null);
		} else {
			saveFile.set(chunkPos, nbtTagCompound);
		}
	}
	
	private NBTTagCompound readChunkData(Int2D chunkPos) {
		if (!saveFile.isOpen()) throw new RuntimeException("Could not load chunk because AerogenPrimerChunks file is not open!");
		final NBTTagCompound nbt = saveFile.get(chunkPos);
		return nbt;
	}
	
	private static final int NBT_TAG_BYTE_ARRAY = 7;
	private static final int NBT_TAG_LIST = 9;
	private static final int NBT_TAG_COMPOUND = 10;
	
	private static NBTTagCompound writeChunkToNBT(WorldPrimerChunk chunk) {
		if (chunk.isCompleted()) {
			chunk.isSaved = true;
			return new NBTTagCompound();
		}
		
		final NBTTagCompound chunkCompoundTag = new NBTTagCompound();
		chunkCompoundTag.setInteger("DataVersion", 1);
		
		final NBTTagCompound levelCompoundTag = new NBTTagCompound();
		chunkCompoundTag.setTag("Level", levelCompoundTag);
		
		levelCompoundTag.setInteger("xPos", chunk.chunkX);
		levelCompoundTag.setInteger("zPos", chunk.chunkZ);
		levelCompoundTag.setBoolean("TerrainPopulated", chunk.isPopulated());
		if (chunk.hasBiomes()) levelCompoundTag.setByteArray("Biomes", chunk.getBiomes());
		
		if (chunk.isGenerated()) {
			levelCompoundTag.setIntArray("HeightMap", chunk.heightMap);
			levelCompoundTag.setTag("Sections", chunk.getBlocks().getSectionsNBT());
		}
		
		chunk.isSaved = true;
		return chunkCompoundTag;
	}
	
	private static WorldPrimerChunk readChunkFromNBT(WorldPrimer world, NBTTagCompound nbt) {
		if (nbt == null) return null;
		
		final NBTTagCompound levelNBT = nbt.getCompoundTag("Level");
		final int chunkX = levelNBT.getInteger("xPos");
		final int chunkZ = levelNBT.getInteger("zPos");
		
		final WorldPrimerChunk chunk = new WorldPrimerChunk(world, chunkX, chunkZ);
		chunk.isPopulated = levelNBT.getBoolean("TerrainPopulated");
		if (chunk.isPopulated) chunk.loadedStatus |= WorldPrimerChunk.LOADED_POPULATED;
		
		if (levelNBT.hasKey("Biomes", NBT_TAG_BYTE_ARRAY)) {
			chunk.hasBiomes = true;
			chunk.biomes = levelNBT.getByteArray("Biomes");
			chunk.loadedStatus |= WorldPrimerChunk.LOADED_WITH_BIOMES;
		}
		
		if (levelNBT.hasKey("Sections", NBT_TAG_LIST)) {
			chunk.isGenerated = true;
			chunk.blocks = ChunkPrimerExt.fromNBT(levelNBT.getTagList("Sections", NBT_TAG_COMPOUND));
			chunk.heightMap = levelNBT.getIntArray("HeightMap");
			chunk.loadedStatus |= WorldPrimerChunk.LOADED_GENERATED;
		} else {
			System.out.println("Chunk "+chunkX+", "+chunkZ+" had no Sections");
		}
		
		chunk.isSaved = true;
		return chunk;
	}

	public void close() {
		try {
			saveFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
