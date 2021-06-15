package com.gpergrossi.aerogen.generator.decorate.placeables;

import java.util.Random;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlaceableBlockRandomFacing implements IPlaceable {
	
	final BlockHorizontal block;
	
    public PlaceableBlockRandomFacing(BlockHorizontal block) {
    	this.block = block;
    }
	
	@Override
	public boolean place(World world, BlockPos pos, Random rand) {
    	if (block.canPlaceBlockAt(world, pos)) {
    		IBlockState blockState = this.block.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.Plane.HORIZONTAL.random(rand));
            world.setBlockState(pos, blockState, 2);
            return true;
    	}
    	return false;
	}
	
}
