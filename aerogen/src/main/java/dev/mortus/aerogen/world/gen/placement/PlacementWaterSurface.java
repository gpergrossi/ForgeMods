package dev.mortus.aerogen.world.gen.placement;

import java.util.Random;

import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.util.math.vectors.Int2D;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlacementWaterSurface extends AbstractPlacement {
	
	public PlacementWaterSurface withDesiredCount(int num) {
		super.withDesiredCount(num);
		return this;
	}
	
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
