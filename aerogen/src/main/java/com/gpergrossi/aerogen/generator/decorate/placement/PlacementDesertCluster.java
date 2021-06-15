package com.gpergrossi.aerogen.generator.decorate.placement;

import java.util.Random;

import com.gpergrossi.aerogen.generator.islands.Island;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Like highest block, except it requires a flat area around it.
 * Checks that the height is identical on the given block as well as +/- 3 in the X and Z directions.
 */
public class PlacementDesertCluster extends PlacementHighestBlock {
		
	@Override
	public boolean canGenerate(World world, Island island, BlockPos position, Random random) {
		if (!super.canGenerate(world, island, position, random)) return false;
		
		int height = island.getHeightmap().getTop(position.getX(), position.getZ());
		if (island.getHeightmap().getTop(position.getX()+3, position.getZ()) != height) return false;
		if (island.getHeightmap().getTop(position.getX()-3, position.getZ()) != height) return false;
		if (island.getHeightmap().getTop(position.getX(), position.getZ()+3) != height) return false;
		if (island.getHeightmap().getTop(position.getX(), position.getZ()-3) != height) return false;
		
		return true;			
	}
	
}