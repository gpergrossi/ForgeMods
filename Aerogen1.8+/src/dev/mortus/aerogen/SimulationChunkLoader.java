package dev.mortus.aerogen;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import dev.mortus.chunks.ChunkLoader;

public class SimulationChunkLoader extends ChunkLoader<SimulationChunk> {

	double chunkSize;
	
	Map<Point, SimulationChunk> map;
	
	public SimulationChunkLoader(double chunkSize) {
		this.chunkSize = chunkSize;
		this.map = new HashMap<Point, SimulationChunk>();
	}
	
	@Override
	public SimulationChunk getChunk(int chunkX, int chunkY) {
		Point pt = new Point(chunkX, chunkY);
		
		SimulationChunk chunk = map.get(pt);
		if (chunk == null) {
			chunk = new SimulationChunk(chunkX, chunkY, chunkSize);
			//System.out.println("Created "+chunk);
			map.put(pt, chunk);
		}
		
		return chunk;
	}

}
