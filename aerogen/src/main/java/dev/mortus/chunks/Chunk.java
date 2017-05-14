package dev.mortus.chunks;

import java.awt.Graphics2D;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Chunk {
	
	long lastSeen;
	boolean loaded, loading, unloading;
	protected final int chunkX, chunkY;
	protected final Lock lock = new ReentrantLock(true);
	
	public Chunk(int chunkX, int chunkY) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		loaded = false;
	}
	
	public abstract void load();
	public abstract void unload();
	public abstract void draw(Graphics2D g);
	
	boolean canLoad() {
		return !loaded && !loading;
	}
	
	boolean canUnload() {
		return loaded && !unloading;
	}
	
	public boolean isLoaded() {
		return loaded;
	}

	protected synchronized void internalLoad() {
		if (this.loaded) {
			System.err.println(this+" already loaded!");
			return;
		}
		load();
		loaded = true;
		loading = false;
	}
	
	protected synchronized void internalUnload() {
		if (!this.loaded) {
			System.err.println(this+" already unloaded!");
			return;
		}
		unload();
		loaded = false;
		unloading = false;
	}
	
	public String toString() {
		return "Chunk["+chunkX+","+chunkY+"]";
	}
	
	public boolean equals(Chunk other) {
		return this.chunkX == other.chunkX && this.chunkY == other.chunkY;
	}
	
}
