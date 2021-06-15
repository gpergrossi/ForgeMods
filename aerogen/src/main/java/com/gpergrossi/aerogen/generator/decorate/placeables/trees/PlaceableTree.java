package com.gpergrossi.aerogen.generator.decorate.placeables.trees;

import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.placeables.IPlaceable;

import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockVine;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlaceableTree extends AbstractPlaceableTree {
	
	public static final IPlaceable JUNGLE_TREE = new PlaceableTree(4, JUNGLE_LEAF, JUNGLE_LOG, true, true);
	
	protected int minTreeHeight;
	protected IBlockState metaLeaves;
	protected IBlockState metaWood;
	protected boolean vinesGrow;
	protected boolean placeCocoa;
	
	public PlaceableTree(int minHeight, IBlockState leaves, IBlockState trunk, boolean vines, boolean cocoa) {
		this.minTreeHeight = minHeight;
		this.metaLeaves = leaves;
		this.metaWood = trunk;
		this.vinesGrow = vines;
		this.placeCocoa = cocoa;
	}
	
	@Override
	public boolean place(World world, BlockPos pos, Random rand) {
		int height = rand.nextInt(3) + this.minTreeHeight + 1;
		if (!hasSpace(world, pos, height)) return false;

		IBlockState state = world.getBlockState(pos.down());
		if (!state.getBlock().canSustainPlant(state, world, pos.down(), EnumFacing.UP, SAPLING)) return false;
		state.getBlock().onPlantGrow(state, world, pos.down(), pos);

		BlockPos.MutableBlockPos workPos = new BlockPos.MutableBlockPos();
		
		// Add leaves
		for (int y = 0; y < 4; y++) {
			int radius = 1 + y / 2;
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					if (Math.abs(x) == radius && Math.abs(z) == radius && (rand.nextInt(2) == 0 || y == 0)) continue;
					workPos.setPos(pos.getX()+x, pos.getY()+height-1-y, pos.getZ()+z);
					if (isAirLeavesOrVine(world, workPos)) {
						 world.setBlockState(workPos, metaLeaves, 2);
					}
				}
			}
		}
		
		// Add trunk
		for (int y = 0; y < height-1; y++) {
			BlockPos upN = pos.up(y);
			state = world.getBlockState(upN);
			if (!isAirLeavesOrVine(world, upN)) continue;
			
			world.setBlockState(upN, metaWood, 2);
			
			if (this.vinesGrow && y > 0) {
				if (rand.nextInt(3) > 0) addVine(world, upN.west(), BlockVine.EAST);
				if (rand.nextInt(3) > 0) addVine(world, upN.east(), BlockVine.WEST);
				if (rand.nextInt(3) > 0) addVine(world, upN.north(), BlockVine.SOUTH);
				if (rand.nextInt(3) > 0) addVine(world, upN.south(), BlockVine.NORTH);
			}
		}

		if (this.vinesGrow) {			
			for (int y = 0; y < 4; y++) {
				int radius = 1 + y / 2;
				int diameter = 2*radius;

				// Check just the outer edge of each disk of leaves
				for (int x = -radius; x <= radius; x += diameter) {
					for (int z = -radius; z <= radius; z += diameter) {
						workPos.setPos(pos.getX()+x, pos.getY()+height-1-y, pos.getZ()+z);
						state = world.getBlockState(workPos);
						if (state.getBlock().isLeaves(state, world, workPos)) {
							if (rand.nextInt(4) == 0) addHangingVine(world, workPos.west(), BlockVine.EAST);
							if (rand.nextInt(4) == 0) addHangingVine(world, workPos.east(), BlockVine.WEST);
							if (rand.nextInt(4) == 0) addHangingVine(world, workPos.north(), BlockVine.SOUTH);
							if (rand.nextInt(4) == 0) addHangingVine(world, workPos.south(), BlockVine.NORTH);
						}
					}
				}
			}
		}
		
		if (this.placeCocoa) {
			if (rand.nextInt(5) == 0 && height > 5) {
				for (int y = 0; y < 2; y++) {
					for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
						if (rand.nextInt(4 - y) == 0) {
							EnumFacing offset = facing.getOpposite();
							placeCocoa(world, rand.nextInt(3), pos.add(offset.getFrontOffsetX(), height - 5 + y, offset.getFrontOffsetZ()), facing);
						}
					}
				}
			}
		}

		return true;
	}

	protected boolean hasSpace(World world, BlockPos pos, int height) {
		if (pos.getY() < 1 || pos.getY() + height >= world.getHeight()) return false;
		BlockPos.MutableBlockPos queryPos = new BlockPos.MutableBlockPos();
		
		for (int y = pos.getY(); y <= pos.getY() + height; y++) {
			int radius = 1;
			if (y == pos.getY()) radius = 0;
			else if (y >= pos.getY() + height - 2) radius = 2;

			for (int x = pos.getX() - radius; x <= pos.getX() + radius; x++) {
				for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; z++) {
					queryPos.setPos(x, y, z);
					if (!isReplaceable(world, queryPos)) return false;
				}
			}
		}
		return true;
	}
	
	protected boolean placeCocoa(World world, int age, BlockPos pos, EnumFacing facing) {
		if (!world.isAirBlock(pos)) return false;
		return world.setBlockState(pos, Blocks.COCOA.getDefaultState().withProperty(BlockCocoa.AGE, age).withProperty(BlockHorizontal.FACING, facing), 2);		
	}

	protected boolean addVine(World world, BlockPos pos, PropertyBool face) {
		if (!world.isAirBlock(pos)) return false;
		return world.setBlockState(pos, Blocks.VINE.getDefaultState().withProperty(face, true), 2);
	}
	
    protected int addHangingVine(World world, BlockPos pos, PropertyBool face) {
    	int vinesAdded = 0;
        for (int i = 0; i < 4; i++) {
            boolean success = this.addVine(world, pos, face);
            if (success) vinesAdded++;
            else break;
            pos = pos.down();
        }
        return vinesAdded;
    }
	
}
