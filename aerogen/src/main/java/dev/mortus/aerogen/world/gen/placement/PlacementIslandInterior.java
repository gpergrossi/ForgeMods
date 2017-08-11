package dev.mortus.aerogen.world.gen.placement;

import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.util.math.vectors.Int2D;
import net.minecraft.world.World;

public class PlacementIslandInterior extends AbstractPlacement {
	
	int minDepth = 0;
	int maxDepth = 256;
	
	public PlacementIslandInterior withDesiredCount(int num) {
		super.withDesiredCount(num);
		return this;
	}
	
	public PlacementIslandInterior withChanceForExtra(float chance) {
		super.withChanceForExtra(chance);
		return this;
	}
	
	public PlacementIslandInterior withMinDepth(int depth) {
		this.minDepth = depth;
		return this;
	}
	
	public PlacementIslandInterior withMaxDepth(int depth) {
		this.maxDepth = depth;
		return this;
	}

	@Override
	public int getMinY(World world, Island island, Int2D position) {
		int top = island.getHeightmap().getTop(position);
		int bottom = island.getHeightmap().getBottom(position);
		return Math.max(bottom, top - maxDepth);
	}

	@Override
	public int getMaxY(World world, Island island, Int2D position) {
		int top = island.getHeightmap().getTop(position);
		return top - minDepth;
	}
	
}
