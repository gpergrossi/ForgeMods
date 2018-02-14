package com.gpergrossi.aerogen.primer;

import java.util.HashMap;
import java.util.Map;

import com.gpergrossi.aerogen.primer.ChunkPrimerExt.DataResult;

import jline.internal.Log;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.storage.ThreadedFileIOBase;

public class WorldPrimerChunkLoader {

	private static final Map<World, WorldPrimerChunkLoader> instances = new HashMap<>();
	
	public static WorldPrimerChunkLoader forWorld(World world) {
		WorldPrimerChunkLoader result = instances.get(world);
		
		if (result == null) {
			result = new WorldPrimerChunkLoader(world);
			instances.put(world, result);
		}
		return result;
	}
	
	private final World world;
	private WorldPrimer primer;
	
	private WorldPrimerChunkLoader(World world) {
		this.world = world;
	}

	public void setPrimer(WorldPrimer worldPrimer) {
		if (this.primer != null) throw new RuntimeException("This WorldPrimerChunkStore is already in use!");
		this.primer = worldPrimer;
	}
	
	public void saveChunk(World world, WorldPrimerChunk chunk) {
		if (chunk.isPurged()) return;
		try {
			world.checkSessionLock();
			
			NBTTagCompound chunkCompoundTag = writeChunkToNBT(chunk);
			this.addChunkToPending(chunk.chunkX, chunk.chunkZ, chunkCompoundTag);
		} catch (Exception exception) {
			Log.error("Failed to save primer chunk", (Throwable) exception);
		}
	}
	
	private void addChunkToPending(int chunkX, int chunkZ, NBTTagCompound chunkCompoundTag) {
		if (!this.currentSave.contains(pos)) {
			this.chunksToRemove.put(pos, compound);
		}
		ThreadedFileIOBase.getThreadedIOInstance().queueIO(this);
	}

	private static NBTTagCompound writeChunkToNBT(WorldPrimerChunk chunk) {
		NBTTagCompound chunkCompoundTag = new NBTTagCompound();
		chunkCompoundTag.setInteger("DataVersion", 1);
		
		NBTTagCompound levelCompoundTag = new NBTTagCompound();
		chunkCompoundTag.setTag("Level", levelCompoundTag);
		
		levelCompoundTag.setInteger("xPos", chunk.chunkX);
		levelCompoundTag.setInteger("zPos", chunk.chunkZ);
		levelCompoundTag.setBoolean("TerrainPopulated", chunk.isPopulated());
		if (chunk.hasBiomes()) levelCompoundTag.setByteArray("Biomes", chunk.getBiomes());
		
		if (chunk.isGenerated()) {
			levelCompoundTag.setIntArray("HeightMap", chunk.getHeightMap());
			chunkCompoundTag.setTag("Sections", chunk.getBlocks().getSectionsNBT());
		}
		
		return chunkCompoundTag;
	}
	
}
