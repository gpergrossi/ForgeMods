package dev.mortus.chunks;

public abstract class ChunkLoader<T extends Chunk> {

	public abstract T getChunk(int chunkX, int chunkY);

	public long getMaxChunkAge() {
		return 0; // 120 = 2 seconds at 60 FPS
	}

	public double getChunkSize() {
		return 200;
	}
	
}
