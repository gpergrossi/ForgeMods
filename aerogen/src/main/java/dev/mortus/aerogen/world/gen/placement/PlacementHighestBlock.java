package dev.mortus.aerogen.world.gen.placement;

import java.util.Random;

import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.util.math.vectors.Int2D;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlacementHighestBlock extends AbstractPlacement {
	
	public PlacementHighestBlock withDesiredCount(int num) {
		super.withDesiredCount(num);
		return this;
	}
	
	public PlacementHighestBlock withChanceForExtra(float chance) {
		super.withChanceForExtra(chance);
		return this;
	}
	
	@Override
	public int getMinY(World world, Island island, Int2D position) {
		return world.getHeight(position.x(), position.y());
	}

	@Override
	public int getMaxY(World world, Island island, Int2D position) {
		return world.getHeight(position.x(), position.y());
	}

	@Override
	public boolean canGenerate(World world, Island island, BlockPos position, Random random) {
		return (position.getY() > 0);
	}
	
}
