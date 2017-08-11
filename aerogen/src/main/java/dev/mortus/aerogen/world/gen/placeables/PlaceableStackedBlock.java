package dev.mortus.aerogen.world.gen.placeables;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlaceableStackedBlock implements IPlaceable {
	final Block block;
	final IBlockState blockState;
	
	boolean isCactus = false;
	boolean isReed = false;
	
	final int minStack = 1;
	final int maxStack = 1;
	
    public PlaceableStackedBlock(Block block, int minStack, int maxStack) {
    	this.block = block;
    	this.blockState = block.getDefaultState();
    	if (block == Blocks.CACTUS) isCactus = true;
    	if (block == Blocks.REEDS) isReed = true;
    }
    
    public PlaceableStackedBlock(IBlockState blockState, int minStack, int maxStack) {
    	this.block = blockState.getBlock();
    	this.blockState = blockState;
    	if (block == Blocks.CACTUS) isCactus = true;
    	if (block == Blocks.REEDS) isReed = true;
    }
	
	@Override
	public boolean place(World world, BlockPos pos, Random rand) {
		boolean anyPlaced = false;
        int height = minStack + rand.nextInt(rand.nextInt(maxStack - minStack + 1) + 1);
        for (int h = 0; h < height; h++) {
            if (isCactus && !Blocks.CACTUS.canBlockStay(world, pos)) break;
            if (isReed && !Blocks.REEDS.canBlockStay(world, pos)) break;
            if (!isCactus && !isReed && !block.canPlaceBlockAt(world, pos)) break;
            world.setBlockState(pos.up(h), blockState, 2);
            anyPlaced = true;
        }
        return anyPlaced;
	}
}
