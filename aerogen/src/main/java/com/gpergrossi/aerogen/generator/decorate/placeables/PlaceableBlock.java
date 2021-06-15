package com.gpergrossi.aerogen.generator.decorate.placeables;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlaceableBlock implements IPlaceable {
	final Block block;
	final IBlockState blockState;
	
    public PlaceableBlock(Block block) {
    	this.block = block;
    	this.blockState = block.getDefaultState();
    }
    
    public PlaceableBlock(IBlockState blockState) {
    	this.block = blockState.getBlock();
    	this.blockState = blockState;
    }
	
	@Override
	public boolean place(World world, BlockPos pos, Random rand) {
    	if (block.canPlaceBlockAt(world, pos)) {            		
            world.setBlockState(pos, this.blockState, 2);
            return true;
        }
    	return false;
	}
}