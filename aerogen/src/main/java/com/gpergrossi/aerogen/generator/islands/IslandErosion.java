package com.gpergrossi.aerogen.generator.islands;

import java.util.Random;

import com.gpergrossi.aerogen.generator.regions.features.river.RiverCell;
import com.gpergrossi.util.math.func2d.FractalNoise2D;
import com.gpergrossi.util.math.func2d.IFunction2D;

public class IslandErosion {
		
	public int   erodeOctaves = 6;
	public float erodeWavelength = 52f;
	public float erodePersistence = 0.45f;
	
	public float erodeGainBase = 16f;
	public float erodeGainPerRadius = 0.3f;
	
	public float cutoffBase = 0f;
	public float cutoffPerErodeGain = 0.7f;
	public float cutoffPerRadius = 0.1f;
	
	public IslandErosion() {}
	
	private float erodeGain;
	private float erodeCutoff;
	private IFunction2D erodeNoise;

	private Island island;
	double holeSize = 0;
	
	/**
	 * Record the maximum distance from the boundary of the island. This is used as a gauge for how big the island is.
	 * @param random - the island's random number generator. To be used for all random rolls. 
	 * @param maxRadius - an estimate of the greatest distance (in blocks) from some point in the center of the island to the edge
	 */
	public void begin(Island island, Random random, float maxRadius) {
		// Below are the settings used to compute erosion. 
		// It took a massive amount of tuning to get good results
		erodeNoise = FractalNoise2D.builder().withSeed(random.nextLong()).withPeriod(erodeWavelength).withOctaves(erodeOctaves, erodePersistence).build();
		
		// These two parameters scale with the island's size true size
		erodeGain = erodeGainBase + erodeGainPerRadius*maxRadius;
		erodeCutoff = cutoffBase + cutoffPerErodeGain*erodeGain + cutoffPerRadius*maxRadius;
		
		this.island = island;
		this.holeSize = random.nextInt(5)+3;
	}
	
	/**
	 * Return true if the block at coordinate (x, z) with given distanceToEdge of the island should be eroded from the island's shape.
	 * @param x - x position of the block (used for noise)
	 * @param z - z position of the block (used for noise)
	 * @param distanceToEdge - estimated number of blocks to the edge of the island
	 * @return
	 */
	public boolean erode(float x, float z, float distanceToEdge) {
		float noiseVal = (float) erodeNoise.getValue(x, z)*0.5f+0.5f; // FractalNoise2D returns (-1,1) we want (0,1)
		
		double riverDist = Double.POSITIVE_INFINITY;
		for (RiverCell irc : island.getShape().getRiverCells()) {
			riverDist = Math.min(riverDist, irc.minDistToRiver(x, z));
		}
		double riverErodeProtect = 0;
		if (riverDist < 64) riverErodeProtect = Math.pow((64-riverDist)/64, 8) * 2*erodeGain;
		
		return erodeGain*noiseVal + distanceToEdge + riverErodeProtect < erodeCutoff;
	}

	/**
	 * This is the maximum amount the erosion matrix can be downsized by. A value of 1 means every block on the island
	 * requires a tile in the matrix. A value of N means that the matrix can pretend the island is made of NxN tiles.
	 * This value should be as high as possible while still achieving good results. Low values can cause immense lag
	 * on large islands. The default algorithm can handle up to about N=6 downsize. Most small islands will not need
	 * to be downsized.
	 * @return
	 */
	public float maximumDownsize() {
		return 8;
	}

	/**
	 * The maximum number of cells in the erosion matrix. Smaller numbers mean lower quality for large islands.
	 * Bugger numbers means slower (sometimes MUCH slower) island generation. Through testing, I have seen that 
	 * 256x256 is a good grid size for quality and speed.
	 * @return preferred max matrix size (maximumDownsize could bypass this preferred maximum)
	 */
	public int preferredMaxMatrixSize() {
		return 256*256;
	}

}
