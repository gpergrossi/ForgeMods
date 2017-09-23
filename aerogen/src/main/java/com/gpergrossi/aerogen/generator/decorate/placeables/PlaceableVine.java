package com.gpergrossi.aerogen.generator.decorate.placeables;

import java.util.Random;

import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlaceableVine implements IPlaceable {
	
	public static enum Type {
		/** 
		 * The vines will generate on all available sides (north, south, east, and west) of the solid block at the given BlockPos
		 */
		AROUND_BLOCK, 
		
		/** 
		 * The vines will generate with one random available facing (north, south, east, or west) in the air block at the given BlockPos
		 */
		FROM_AIR;
	}
	
	protected Type type = Type.FROM_AIR;
	
	protected int minLength = 1;
	protected int maxLength = 1;
	
	public PlaceableVine() {}
	
	public PlaceableVine(Type type, int minLength, int maxLength) {
		this.type = type;
		this.minLength = minLength;
		this.maxLength = maxLength;
	}
	
	protected int growVine(World world, BlockPos airBlockPos, EnumFacing facing, Random rand) {                
		IBlockState iblockstate = Blocks.VINE.getDefaultState()
			.withProperty(BlockVine.NORTH, facing == EnumFacing.NORTH)
			.withProperty(BlockVine.SOUTH, facing == EnumFacing.SOUTH)
			.withProperty(BlockVine.WEST, facing == EnumFacing.WEST)
			.withProperty(BlockVine.EAST, facing == EnumFacing.EAST);
		
		int targetLength = rand.nextInt(maxLength - minLength + 1) + minLength;
		int numPlaced;
		
    	for (numPlaced = 0; numPlaced < targetLength; numPlaced++) {
    		if (!world.isAirBlock(airBlockPos)) break;
    		
    		boolean success = world.setBlockState(airBlockPos, iblockstate, 2);
    		if (!success) break;
    		
    		airBlockPos = airBlockPos.down();
    	}
		
		return numPlaced;
	}
	
	@Override
	public boolean place(World world, BlockPos pos, Random rand) {
		switch (type) {
			case AROUND_BLOCK:
				boolean success = false;
				success |= (growVine(world, pos.east(), EnumFacing.WEST, rand) > 0);
				success |= (growVine(world, pos.west(), EnumFacing.EAST, rand) > 0);
				success |= (growVine(world, pos.south(), EnumFacing.NORTH, rand) > 0);
				success |= (growVine(world, pos.north(), EnumFacing.SOUTH, rand) > 0);
				return success;
				
			case FROM_AIR:
				EnumFacing facing = getRandomValidFacing(world, pos, rand);
				if (facing == null) return false;				
				return growVine(world, pos, facing, rand) > 0;
		}
		return false;
	}
	
	/**
	 * Returns one of the valid facings (randomly selected with equal probability) for a vine to be spawned at the given BlockPos.
	 * The block at BlockPos is required to be air. If no valid vine facings exist, null is returned.
	 */
	public EnumFacing getRandomValidFacing(World world, BlockPos pos, Random rand) {
		if (!world.isAirBlock(pos)) return null;
		
		int numPossibleDirections = 0;
		boolean[] canPlace = new boolean[4];
		
		int i = 0;
		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL.facings()) {
            if (!Blocks.VINE.canPlaceBlockOnSide(world, pos, facing)) continue;
            canPlace[i++] = true;
            numPossibleDirections++;
        }
		if (numPossibleDirections == 0) return null;
		
		int select = getRandomTrueIndex(canPlace, numPossibleDirections, rand);				
		return EnumFacing.Plane.HORIZONTAL.facings()[select];
	}
	
	protected int getRandomTrueIndex(boolean[] arr, int numTrues, Random rand) {
		int select = (numTrues > 1) ? rand.nextInt(numTrues) : 0;
		return selectNthTrue(arr, select);
	}

	protected int selectNthTrue(boolean[] arr, int select) {
		int index = -1;
		for (; select >= 0; select--) { 
			index = selectNextTrue(arr, index);
		}
		return index;
	}

	protected int selectNextTrue(boolean[] arr, int currentIndex) {
		while (!arr[currentIndex++] && currentIndex < arr.length);
		return currentIndex;
	}

}
