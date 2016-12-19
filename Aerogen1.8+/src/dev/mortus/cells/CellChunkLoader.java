package dev.mortus.cells;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import dev.mortus.chunks.ChunkLoader;

public class CellChunkLoader extends ChunkLoader<CellChunk> {

	double chunkSize;
	
	Map<Point, CellChunk> map;
	
	public CellChunkLoader(double chunkSize) {
		this.chunkSize = chunkSize;
		this.map = new HashMap<>();
	}
	
	@Override
	public CellChunk getChunk(int chunkX, int chunkY) {
		Point pt = new Point(chunkX, chunkY);
		
		CellChunk chunk = map.get(pt);
		if (chunk == null) {
			chunk = new CellChunk(chunkX, chunkY);
			//System.out.println("Created "+chunk);
			map.put(pt, chunk);
		}
		
		return chunk;
	}

}
