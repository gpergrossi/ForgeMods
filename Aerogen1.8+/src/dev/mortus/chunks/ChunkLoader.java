package dev.mortus.chunks;

import java.util.Comparator;

public abstract class ChunkLoader<T extends Chunk> {

	public abstract T getChunk(int chunkX, int chunkY);

	public Comparator<T> getLoadPriorityComparator() {
		return new Comparator<T>() {
			public int compare(T o1, T o2) {
				return 0;
			}
		};
	}

	public Comparator<T> getUnloadPriorityComparator() {
		return new Comparator<T>() {
			public int compare(T o1, T o2) {
				return 0;
			}
		};
	}

	public long getMaxChunkAge() {
		return 120; // 2 seconds at 60 FPS
	}
	
}
