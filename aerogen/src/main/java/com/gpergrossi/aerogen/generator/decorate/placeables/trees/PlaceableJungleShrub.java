package com.gpergrossi.aerogen.generator.decorate.placeables.trees;

import java.util.Random;

import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlaceableJungleShrub extends AbstractPlaceableTree {

	protected IBlockState metaLeaves;
	protected IBlockState metaWood;

	public PlaceableJungleShrub(IBlockState leaves, IBlockState trunk) {
		this.metaLeaves = leaves;
		this.metaWood = trunk;
	}

	@Override
	public boolean place(World world, BlockPos pos, Random rand) {
		while (pos.getY() > 0) {
			if (isAirLeavesOrVine(world, pos)) break;
			else pos = pos.down();
		}

		IBlockState state = world.getBlockState(pos);
		boolean isSoil = state.getBlock().canSustainPlant(state, world, pos, net.minecraft.util.EnumFacing.UP, (BlockSapling) Blocks.SAPLING);
		if (!isSoil) return false;
		
		pos = pos.up();
		world.setBlockState(pos, this.metaWood, 2);

		for (int y = 0; y <= 2; y++) {
			int radius = 2 - y;
			
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					
					if (Math.abs(x) == radius && Math.abs(z) == radius && rand.nextInt(2) == 0) continue;
					
					BlockPos blockpos = pos.add(x, y, z);
					state = world.getBlockState(blockpos);
					if (state.getBlock().canBeReplacedByLeaves(state, world, blockpos)) {
						world.setBlockState(blockpos, this.metaLeaves, 2);
					}
					
				}
			}
		}

		return true;
	}

}
