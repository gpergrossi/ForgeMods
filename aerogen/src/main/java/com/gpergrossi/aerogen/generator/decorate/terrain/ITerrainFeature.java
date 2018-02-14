package com.gpergrossi.aerogen.generator.decorate.terrain;

import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.PopulatePhase;
import com.gpergrossi.util.geom.ranges.Int2DRange;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public interface ITerrainFeature {

	public Int2DRange getRangeXZ();
	
	public int getMinY();
	public int getMaxY();
	
	/**
	 * Place all blocks appropriate for this terrain feature that do not have to be in the populate phase.
	 * @param primer
	 * @param chunkRange
	 * @param random
	 */
	public void provideChunk(ChunkPrimer primer, Int2DRange chunkRange, Random random);
	
	/**
	 * Place all blocks appropriate for this terrain feature for the provided populate phase.
	 * @param world
	 * @param chunkRange
	 * @param random
	 * @param currentPhase
	 */
	public void populateChunk(World world, Int2DRange chunkRange, Random random, PopulatePhase currentPhase);
	
}
