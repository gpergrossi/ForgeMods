package dev.mortus.aerogen.world.gen.placeables;

import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlaceableBush implements IPlaceable {
	
	final BlockBush block;
	final IBlockState blockState;
	
    public PlaceableBush(BlockBush bush) {
    	this.block = bush;
    	this.blockState = bush.getDefaultState();
    }
    
    public PlaceableBush(IBlockState bush) {
    	BlockBush block = (BlockBush) bush.getBlock();
    	this.block = block;
    	this.blockState = bush;
    }
    
	@Override
	public boolean place(World world, BlockPos pos, Random rand) {
        if (block.canBlockStay(world, pos, blockState)) {
            world.setBlockState(pos, blockState, 2);
            return true;
        }
        return false;
	}
	
}