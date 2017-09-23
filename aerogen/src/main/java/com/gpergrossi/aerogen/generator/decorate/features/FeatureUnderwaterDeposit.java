package com.gpergrossi.aerogen.generator.decorate.features;

import java.util.Random;

import com.gpergrossi.aerogen.generator.islands.Island;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FeatureUnderwaterDeposit extends AbstractFeature {

	Block block;
	int radius;
	
	public FeatureUnderwaterDeposit(Block block, int radius) {
		this.block = block;
		this.radius = radius;
	}
	
	@Override
	public boolean generate(World world, Island island, BlockPos position, Random rand) {    	
		// Find the ground
        for (; position.getY() > 0; position = position.down()) {
        	IBlockState iblockstate = world.getBlockState(position);
        	boolean isAir = (iblockstate.getMaterial() == Material.AIR);
        	boolean isWater = island.getBiome().isWater(iblockstate);
        	if (!isAir && !isWater) break;
        }
        position = position.up();

    	IBlockState iblockstate = world.getBlockState(position);
        if (!island.getBiome().isWater(iblockstate)) {
        	return false;
        }

        int spread = rand.nextInt(this.radius - 2) + 2;
    	
        for (int x = -spread; x <= spread; x++) {
            for (int z = -spread; z <= spread; z++) {
            	
                if (x*x + z*z > spread*spread) continue;
            
                for (int y = -1; y <= 0; y++) {
                    BlockPos blockpos = position.add(x, y, z);
                    Block block = world.getBlockState(blockpos).getBlock();
                    
                    if (blockpos.getY() == 0) continue;
                    IBlockState blockBelow = world.getBlockState(blockpos.down());
                    if (blockBelow.getMaterial() == Material.AIR) continue;
                    
                    if (block == Blocks.SAND || block == Blocks.GRAVEL || block == Blocks.DIRT || block == Blocks.GRASS) {
                        world.setBlockState(blockpos, this.block.getDefaultState(), 2);
                    }
                }
            }
        }

        return true;
	}
	
}
