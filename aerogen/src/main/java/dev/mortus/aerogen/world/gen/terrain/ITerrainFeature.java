package dev.mortus.aerogen.world.gen.terrain;

import java.util.Random;

import dev.mortus.util.math.ranges.Int2DRange;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public interface ITerrainFeature {

	public Int2DRange getRangeXZ();
	
	public int getMinY();
	
	public int getMaxY();
	
	public boolean provideChunk(ChunkPrimer primer, Int2DRange chunkRange, Random random);
	public boolean populateChunk(World world, Int2DRange chunkRange, Random random);
	
}
