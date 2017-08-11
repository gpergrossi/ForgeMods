package dev.mortus.aerogen.world.gen.placement;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.util.math.vectors.Int2D;

public interface IPlacement {
	
	public int getNumAttemptsPerChunk(Random random);
	public int getMaxPlacementsPerChunk(Random random);
	
	public int getMinY(World world, Island island, Int2D position);
	public int getMaxY(World world, Island island, Int2D position);
		
	public boolean canGenerate(World world, Island island, BlockPos position, Random random);
	
}
