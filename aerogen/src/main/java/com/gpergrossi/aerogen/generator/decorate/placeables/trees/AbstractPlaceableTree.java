package com.gpergrossi.aerogen.generator.decorate.placeables.trees;

import com.gpergrossi.aerogen.generator.decorate.placeables.IPlaceable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class AbstractPlaceableTree implements IPlaceable {

 	public static final IBlockState BIRCH_LOG 		= Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH);
 	public static final IBlockState JUNGLE_LOG 		= Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
 	public static final IBlockState OAK_LOG 		= Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
 	public static final IBlockState SPRUCE_LOG 		= Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
 	public static final IBlockState ACACIA_LOG 		= Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA);
 	public static final IBlockState DARK_OAK_LOG 	= Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK);

 	private static final IBlockState NO_DECAY_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false);
 	private static final IBlockState NO_DECAY_LEAF2 = Blocks.LEAVES2.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false);
 	
    public static final IBlockState BIRCH_LEAF 		= NO_DECAY_LEAF.withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH);
    public static final IBlockState JUNGLE_LEAF 	= NO_DECAY_LEAF.withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE);
    public static final IBlockState OAK_LEAF 		= NO_DECAY_LEAF.withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK);
    public static final IBlockState SPRUCE_LEAF 	= NO_DECAY_LEAF.withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE);
    public static final IBlockState ACACIA_LEAF 	= NO_DECAY_LEAF2.withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA);
    public static final IBlockState DARK_OAK_LEAF 	= NO_DECAY_LEAF2.withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK);

    public static final BlockSapling SAPLING 	= (BlockSapling) Blocks.SAPLING;
    
	/**
	 * Returns the absolute value of the x, y, or z axis with the largest absolute value.
	 */
	protected static int getLongestAxis(BlockPos pos) {
		int x = MathHelper.abs(pos.getX());
		int y = MathHelper.abs(pos.getY());
		int z = MathHelper.abs(pos.getZ());

		if (z > x && z > y) {
			return z;
		} else {
			return y > x ? y : x;
		}
	}
	
	protected static BlockLog.EnumAxis getLogAxis(BlockPos from, BlockPos to) {
		int dx = Math.abs(to.getX() - from.getX());
		int dz = Math.abs(to.getZ() - from.getZ());
		
		if (Math.max(dx, dz) == 0) return BlockLog.EnumAxis.Y;
		if (dx > dz) return BlockLog.EnumAxis.X;
		if (dz > dx) return BlockLog.EnumAxis.Z;
		return BlockLog.EnumAxis.X;	// Original tree code prefers the x axis
	}
    
    /**
     * Returns whether or not a tree can grow into a block
     * For example, a tree will not grow into stone
     */
    protected static boolean canGrowInto(Block block) {
        Material material = block.getDefaultState().getMaterial();
        return material == Material.AIR || material == Material.LEAVES || block == Blocks.VINE
        		|| block == Blocks.GRASS || block == Blocks.DIRT
        		|| block == Blocks.LOG || block == Blocks.LOG2
        		|| block == Blocks.SAPLING;
    }
    
    protected static boolean isAirLeavesOrVine(World world, BlockPos pos) {
    	IBlockState state = world.getBlockState(pos);
    	return state.getBlock().isAir(state, world, pos) || state.getBlock().isLeaves(state, world, pos) || state.getMaterial() == Material.VINE;
    }
    
    protected static boolean isReplaceable(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return canGrowInto(state.getBlock());
    }
    
}
