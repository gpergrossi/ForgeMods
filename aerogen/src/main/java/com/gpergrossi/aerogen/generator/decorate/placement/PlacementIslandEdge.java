package com.gpergrossi.aerogen.generator.decorate.placement;

import java.util.Random;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlacementIslandEdge extends AbstractPlacement {

	@Override
	public PlacementIslandEdge withDesiredCount(int num) {
		super.withDesiredCount(num);
		return this;
	}

	@Override
	public PlacementIslandEdge withChanceForExtra(float chance) {
		super.withChanceForExtra(chance);
		return this;
	}
	
	@Override
	public int getMinY(World world, Island island, Int2D position) {
		return island.getHeightmap().getTop(position);
	}

	@Override
	public int getMaxY(World world, Island island, Int2D position) {
		return island.getHeightmap().getTop(position);
	}
	
	@Override
	public boolean canGenerate(World world, Island island, BlockPos position, Random random) {
		int x = position.getX();
		int z = position.getZ();
		
		float edgeDist = island.getShape().getEdgeDistance(x, z);
		if (edgeDist <= 0.0f || edgeDist > 1.0f) return false;
		
		if (position.getY() != island.getHeightmap().getTop(x, z)) return false;
		
		return true;
	}

	
	
}
