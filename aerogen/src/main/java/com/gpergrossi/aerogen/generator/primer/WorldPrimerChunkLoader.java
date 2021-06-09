package com.gpergrossi.aerogen.generator.primer;

import java.io.File;
import java.io.IOException;

import com.gpergrossi.aerogen.AeroGenMod;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.util.io.ndmf.NamedDataMapFile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;

public class WorldPrimerChunkLoader {
	
	private final WorldPrimer world;
	private final NamedDataMapFile<Int2D, NBTTagCompound> saveFile;
	
	public WorldPrimerChunkLoader(WorldPrimer world) {
		this.world = world;
		
		File worldDir = world.getMinecraftWorld().getSaveHandler().getWorldDirectory();
		File primerSave = new File(worldDir, "AerogenPrimerChunks");
		
		this.saveFile = new NamedDataMapFile<>(StreamHandlerInt2D.INSTANCE, StreamHandlerNBT.INSTANCE, 4096);
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
	
	public void writeChunkData(Int2D chunkPos, NBTTagCompound nbtTagCompound) {
		if (!saveFile.isOpen()) throw new RuntimeException("Could not save chunk because AerogenPrimerChunks file is not open!");

		try {
			world.getMinecraftWorld().checkSessionLock();
		} catch (MinecraftException e) {
			throw new RuntimeException(e);
		}
		
		if (nbtTagCompound.hasNoTags()) {
			saveFile.set(chunkPos, null);
			world.chunks.remove(chunkPos);
		} else {
			saveFile.set(chunkPos, nbtTagCompound);
		}
	}
	
	public NBTTagCompound readChunkData(Int2D chunkPos) {
		if (!saveFile.isOpen()) throw new RuntimeException("Could not load chunk because AerogenPrimerChunks file is not open!");
		
		try {
			world.getMinecraftWorld().checkSessionLock();
		} catch (MinecraftException e) {
			throw new RuntimeException(e);
		}
		
		final NBTTagCompound nbt = saveFile.get(chunkPos);
		return nbt;
	}

	public void close() {
		try {
			saveFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
