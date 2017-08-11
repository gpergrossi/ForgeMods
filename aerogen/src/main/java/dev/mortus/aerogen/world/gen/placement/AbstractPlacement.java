package dev.mortus.aerogen.world.gen.placement;

import java.util.Random;

import dev.mortus.aerogen.world.islands.Island;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractPlacement implements IPlacement {
	
	int desiredPlacementsPerChunk = 1;
	float chanceForExtraPlacement = 0;
	
	public AbstractPlacement() {}
	
	public AbstractPlacement withDesiredCount(int num) {
		this.desiredPlacementsPerChunk = num;
		return this;
	}
	
	public AbstractPlacement withChanceForExtra(float chance) {
		this.chanceForExtraPlacement = chance;
		return this;
	}
	
	@Override
	public int getNumAttemptsPerChunk(Random random) {
		return getMaxPlacementsPerChunk(random)*2;
	}

	@Override
	public int getMaxPlacementsPerChunk(Random random) {
		if (chanceForExtraPlacement > 0 && random.nextFloat() < chanceForExtraPlacement) {
			return (desiredPlacementsPerChunk+1);
		}
		return desiredPlacementsPerChunk;
	}
	
	@Override
	public boolean canGenerate(World world, Island island, BlockPos position, Random random) {
		return true;
	}
	
}