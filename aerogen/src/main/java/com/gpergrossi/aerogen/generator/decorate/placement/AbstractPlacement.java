package com.gpergrossi.aerogen.generator.decorate.placement;

import java.util.Random;

import com.gpergrossi.aerogen.generator.GenerationPhase;
import com.gpergrossi.aerogen.generator.islands.Island;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractPlacement implements IPlacement {
	
	GenerationPhase phase = GenerationPhase.POST_POPULATE;
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

	public AbstractPlacement withPhase(GenerationPhase phase) {
		this.phase = phase;
		return this;
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
	
	@Override
	public GenerationPhase getPhase() {
		return phase;
	}
	
}