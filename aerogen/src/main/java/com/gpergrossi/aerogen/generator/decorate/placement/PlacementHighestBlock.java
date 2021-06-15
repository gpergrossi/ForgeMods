package com.gpergrossi.aerogen.generator.decorate.placement;

import java.util.Random;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlacementHighestBlock extends AbstractPlacement {
	
	@Override
	public PlacementHighestBlock withDesiredCount(int num) {
		super.withDesiredCount(num);
		return this;
	}
	
	@Override
	public PlacementHighestBlock withChanceForExtra(float chance) {
		super.withChanceForExtra(chance);
		return this;
	}
	
	@Override
	public int getMinY(World world, Island island, Int2D position) {
		if (!island.getShape().contains(position)) return 0;
		return world.getHeight(position.x(), position.y());
	}

	@Override
	public int getMaxY(World world, Island island, Int2D position) {
		if (!island.getShape().contains(position)) return 0;
		return world.getHeight(position.x(), position.y());
	}

	@Override
	public boolean canGenerate(World world, Island island, BlockPos position, Random random) {
		return (position.getY() > 0);
	}
	
}
