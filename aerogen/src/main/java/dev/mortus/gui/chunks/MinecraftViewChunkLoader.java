package dev.mortus.gui.chunks;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import dev.mortus.aerogen.regions.Region;
import dev.mortus.aerogen.regions.RegionManager;

public class MinecraftViewChunkLoader extends ChunkLoader<MinecraftViewChunk> {
	
	public RegionManager regionManager;
	
	Map<Point, MinecraftViewChunk> map;
	
	public MinecraftViewChunkLoader() {
		this(new Random().nextLong());
	}
	
	public MinecraftViewChunkLoader(long seed) {
		this.map = new HashMap<>();
		Region.DEBUG_VIEW = true;
		this.regionManager = new RegionManager();
	}
	
	@Override
	public double getChunkSize() {
		return regionManager.getRegionSize();
	}
	
	@Override
	public MinecraftViewChunk getChunk(int chunkX, int chunkY) {
		Point pt = new Point(chunkX, chunkY);
		
		MinecraftViewChunk chunk = map.get(pt);
		if (chunk == null) {
			chunk = new MinecraftViewChunk(getManager(), chunkX, chunkY);
			map.put(pt, chunk);
		}
		
		return chunk;
	}
	
}
