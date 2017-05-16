package dev.mortus.cells;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import dev.mortus.chunks.ChunkLoader;

public class CellChunkLoader extends ChunkLoader<CellChunk> {
	
	Map<Point, CellChunk> map;
	
	public CellChunkLoader() {
		this.map = new HashMap<>();
	}
	
	@Override
	public double getChunkSize() {
		return CellChunk.CHUNK_SIZE;
	}
	
	@Override
	public CellChunk getChunk(int chunkX, int chunkY) {
		Point pt = new Point(chunkX, chunkY);
		
		CellChunk chunk = map.get(pt);
		if (chunk == null) {
			chunk = new CellChunk(getManager(), chunkX, chunkY);
			map.put(pt, chunk);
		}
		
		return chunk;
	}

}
