package com.gpergrossi.aerogen.generator.decorate.placeables.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.placeables.IPlaceable;

import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PlaceableBigTree extends AbstractPlaceableTree {

	public static final IPlaceable BIG_TREE = new PlaceableBigTree();

	int heightLimitLimit = 12;
	
	int height;
	int heightLimit;
	double heightAttenuation = 0.618D;
	double branchSlope = 0.381D;
	double scaleWidth = 1.0D;
	double leafDensity = 1.0D;
	int trunkSize = 1;
	
	IBlockState metaLeaves = OAK_LEAF;
	BlockLog logBlock = (BlockLog) Blocks.LOG;
	
	/**
	 * Sets the distance limit for how far away the generator will populate
	 * leaves from the base leaf node.
	 */
	int leafDistanceLimit = 4;

	public PlaceableBigTree() {}
	
	@Override
	public boolean place(World world, BlockPos pos, Random rand) {
		this.heightLimit = 5 + rand.nextInt(this.heightLimitLimit);
		
		heightLimit = this.getMaxHeightForLocation(world, pos, heightLimit);
		if (heightLimit < 6) return false;
		
		List<FoliageCoordinates> foliageCoords = new ArrayList<>();
		
		this.generateLeaves(world, pos, rand, foliageCoords);

		for (FoliageCoordinates coord : foliageCoords) {
			this.generateLeafNode(world, coord);
		}
		
		this.generateTrunk(world, pos);
		
		for (FoliageCoordinates coord : foliageCoords) {
			this.generateLeafNodeBase(world, pos, coord);
		}
		
		return true;
	}

	/**
	 * Generates a list of leaf nodes for the tree, to be populated by
	 * generateLeaves.
	 */
	void generateLeaves(World world, BlockPos pos, Random rand, List<FoliageCoordinates> foliageCoords) {
		this.height = (int) (this.heightLimit * this.heightAttenuation);
		
		if (this.height >= this.heightLimit) {
			this.height = this.heightLimit - 1;
		}

		int i = (int) (1.382 + Math.pow(this.leafDensity * this.heightLimit / 13.0, 2.0));
		
		if (i < 1) i = 1;

		int branchBaseY = pos.getY() + this.height;
		int k = this.heightLimit - this.leafDistanceLimit;
		
		
		foliageCoords.add(new FoliageCoordinates(pos.up(k), branchBaseY));

		for (; k >= 0; --k) {
			float f = this.getLayerSize(k);

			if (f >= 0.0F) {
				for (int l = 0; l < i; ++l) {
					double d0 = this.scaleWidth * (double) f * ((double) rand.nextFloat() + 0.328D);
					double d1 = (double) (rand.nextFloat() * 2.0F) * Math.PI;
					double d2 = d0 * Math.sin(d1) + 0.5D;
					double d3 = d0 * Math.cos(d1) + 0.5D;
					BlockPos blockpos = pos.add(d2, (double) (k - 1), d3);
					BlockPos blockpos1 = blockpos.up(this.leafDistanceLimit);

					if (this.checkBlockLine(world, blockpos, blockpos1) == -1) {
						int i1 = pos.getX() - blockpos.getX();
						int j1 = pos.getZ() - blockpos.getZ();
						double d4 = (double) blockpos.getY() - Math.sqrt((double) (i1 * i1 + j1 * j1)) * this.branchSlope;
						int k1 = d4 > (double) branchBaseY ? branchBaseY : (int) d4;
						BlockPos blockpos2 = new BlockPos(pos.getX(), k1, pos.getZ());

						if (this.checkBlockLine(world, blockpos2, blockpos) == -1) {
							foliageCoords.add(new FoliageCoordinates(blockpos, blockpos2.getY()));
						}
					}
				}
			}
		}
		
	}

	protected static void fillCircleXZ(World world, BlockPos pos, float radius, IBlockState blockstate) {
		int searchRadius = (int) (radius + 0.618f); // golden ratio... ?
		float radius2 = radius * radius;

		for (int x = -searchRadius; x <= searchRadius; x++) {
			float xRadius2 = Math.abs(x) + 0.5f;
			xRadius2 = xRadius2 * xRadius2;
			
			for (int z = -searchRadius; z <= searchRadius; z++) {
				float zRadius2 = Math.abs(z) + 0.5f;
				zRadius2 = zRadius2 * zRadius2;
				
				if (xRadius2 + zRadius2 > radius2) continue;
					
				BlockPos blockpos = pos.add(x, 0, z);
				if (!isAirLeavesOrVine(world, blockpos)) continue;
				
				world.setBlockState(blockpos, blockstate, 2);
			}
		}
	}

	/**
	 * Gets the rough size of a layer of the tree.
	 */
	protected float getLayerSize(int y) {
		if (y < this.heightLimit * 0.3f) {
			return -1.0f;
		} else {
			float f = (float) this.heightLimit / 2.0F;
			float f1 = f - (float) y;
			float f2 = MathHelper.sqrt(f * f - f1 * f1);

			if (f1 == 0.0F) {
				f2 = f;
			} else if (Math.abs(f1) >= f) {
				return 0.0F;
			}

			return f2 * 0.5F;
		}
	}

	protected float getLayerRadius(int y) {
		if (y < 0 || y >= this.leafDistanceLimit) return -1.0f;
		if (y == 0 || y == this.leafDistanceLimit - 1) return 2.0f;
		return 3.0f;
	}	
	
	/**
	 * Generates the leaves surrounding an individual entry in the leafNodes
	 * list.
	 */
	protected void generateLeafNode(World world, BlockPos pos) {
		for (int y = 0; y < this.leafDistanceLimit; y++) {
			fillCircleXZ(world, pos.up(y), this.getLayerRadius(y), metaLeaves);
		}
	}

	protected void generateLimb(World world, BlockPos start, BlockPos end) {
		BlockPos difference = end.add(-start.getX(), -start.getY(), -start.getZ());
		int numSteps = getLongestAxis(difference);
		
		float dx = (float) difference.getX() / (float) numSteps;
		float dy = (float) difference.getY() / (float) numSteps;
		float dz = (float) difference.getZ() / (float) numSteps;

		for (int step = 0; step <= numSteps; step++) {
			BlockPos workPos = start.add((double) (0.5F + (float) step * dx), (double) (0.5F + (float) step * dy), (double) (0.5F + (float) step * dz));
			BlockLog.EnumAxis enumAxis = getLogAxis(start, workPos);
			world.setBlockState(workPos, logBlock.getDefaultState().withProperty(BlockLog.LOG_AXIS, enumAxis), 2);
		}
	}

	/**
	 * Places the trunk for the big tree that is being generated. Able to
	 * generate double-sized trunks by changing a field that is always 1 to 2.
	 */
	void generateTrunk(World world, BlockPos pos) {
		BlockPos bottom = pos;
		BlockPos top = pos.up(this.height);
		generateLimb(world, bottom, top);

		if (this.trunkSize == 2) {
			generateLimb(world, bottom.south(), top.south());
			generateLimb(world, bottom.east(), top.east());
			generateLimb(world, bottom.south().east(), top.south().east());
		}
	}

	/**
	 * Indicates whether or not a leaf node requires additional wood to be added
	 * to preserve integrity.
	 */
	boolean leafNodeNeedsBase(int deltaY) {
		return deltaY >= this.heightLimit * 0.2;
	}
	
	/**
	 * Generates additional wood blocks to fill out the bases of different leaf
	 * nodes that would otherwise decay.
	 */
	void generateLeafNodeBase(World world, BlockPos pos, FoliageCoordinates coord) {
		int branchBaseY = coord.getBranchBase();
		BlockPos blockpos = new BlockPos(pos.getX(), branchBaseY, pos.getZ());

		if (!blockpos.equals(coord)	&& leafNodeNeedsBase(branchBaseY - pos.getY())) {
			generateLimb(world, blockpos, coord);
		}
	}

	/**
	 * Checks a line of blocks in the world from the first coordinate to triplet
	 * to the second, returning the distance (in blocks) before a non-air,
	 * non-leaf block is encountered and/or the end is encountered.
	 */
	int checkBlockLine(World world, BlockPos start, BlockPos end) {
		BlockPos delta = end.add(-start.getX(), -start.getY(), -start.getZ());
		int i = getLongestAxis(delta);
		if (i == 0) return -1;
		
		float dx = (float) delta.getX() / (float) i;
		float dy = (float) delta.getY() / (float) i;
		float dz = (float) delta.getZ() / (float) i;
		
		for (int j = 0; j <= i; j++) {
			BlockPos workpos = start.add(0.5F + j*dx, 0.5F + j*dy, 0.5F + j*dz);
			if (!isReplaceable(world, workpos)) return j;
		}
		return -1;
	}

	public void setDecorationDefaults() {
		this.leafDistanceLimit = 5;
	}

	/**
	 * Returns a boolean indicating whether or not the current location for the
	 * tree, spanning basePos to to the height limit, is valid.
	 */
	private int getMaxHeightForLocation(World world, BlockPos pos, int heightLimit) {
		BlockPos down = pos.down();
		IBlockState state = world.getBlockState(down);
		
		boolean isSoil = state.getBlock().canSustainPlant(state, world, down, net.minecraft.util.EnumFacing.UP, SAPLING);
		if (!isSoil) return -1;

		return this.checkBlockLine(world, pos, pos.up(heightLimit - 1));
	}

	protected static class FoliageCoordinates extends BlockPos {
		private final int branchBaseY;

		public FoliageCoordinates(BlockPos pos, int branchBaseY) {
			super(pos.getX(), pos.getY(), pos.getZ());
			this.branchBaseY = branchBaseY;
		}

		public int getBranchBase() {
			return this.branchBaseY;
		}
	}

}
