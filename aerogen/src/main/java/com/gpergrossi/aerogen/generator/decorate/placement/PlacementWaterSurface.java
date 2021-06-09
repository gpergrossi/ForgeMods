package com.gpergrossi.aerogen.generator.decorate.placement;

import java.util.Random;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlacementWaterSurface extends AbstractPlacement {
	
	@Override
	public PlacementWaterSurface withDesiredCount(int num) {
		super.withDesiredCount(num);
		return this;
	}
	
	@Override
	public PlacementWaterSurface withChanceForExtra(float chance) {
		super.withChanceForExtra(chance);
		return this;
	}
	
	@Override
	public int getMinY(World world, Island island, Int2D position) {
		return world.getTopSolidOrLiquidBlock(new BlockPos(position.x(), 0, position.y())).getY();
	}

	@Override
	public int getMaxY(World world, Island island, Int2D position) {
		return world.getTopSolidOrLiquidBlock(new BlockPos(position.x(), 0, position.y())).getY();
	}
	
	@Override
	public boolean canGenerate(World world, Island island, BlockPos position, Random random) {
		return (position.getY() > 0);
	}
	
}
