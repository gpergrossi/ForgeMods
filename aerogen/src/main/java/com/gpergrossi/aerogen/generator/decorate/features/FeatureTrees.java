package com.gpergrossi.aerogen.generator.decorate.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gpergrossi.aerogen.generator.islands.Island;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenBirchTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenHugeTrees;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;

public class FeatureTrees extends AbstractFeature {

	List<ITreeType> treeTypes;
	List<Float> treeWeights;
	float totalWeights = 0f;
	
	public FeatureTrees() {
		this.treeTypes = new ArrayList<>();
		this.treeWeights = new ArrayList<>();
	}
	
	public FeatureTrees addTreeType(float weight, ITreeType treeType) {
		this.treeTypes.add(treeType);
		this.treeWeights.add(weight);
		this.totalWeights += weight;
		return this;
	}
	
	protected ITreeType getRandomTreeType(Random rand) {
		if (totalWeights == 0f) return null;
		float roll = rand.nextFloat()*totalWeights;
		for (int i = 0; i < treeTypes.size(); i++) {
			roll -= treeWeights.get(i);
			if (roll <= 0) return treeTypes.get(i);
		}
		return null;
	}
	
	protected WorldGenAbstractTree getRandomTree(Random rand) {
		if (totalWeights == 0f) return null;
		ITreeType type = getRandomTreeType(rand);
		if (type != null) return type.getGenerator(rand);
		return null;
	}
	
	@Override
	public boolean generate(World world, Island island, BlockPos position, Random rand) {
		WorldGenAbstractTree treeGen = getRandomTree(rand);
		if (treeGen == null) return false;
		treeGen.setDecorationDefaults();
		if (treeGen.generate(world, rand, position)) {
			treeGen.generateSaplings(world, rand, position);
			return true;
		}
		return false;
	}
	    
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
	
    public static final ITreeType OAK_TREE = new BasicTree(new WorldGenTrees(false));
    public static final ITreeType BIG_OAK_TREE = new BasicTree(new WorldGenBigTree(false));
    public static final ITreeType JUNGLE_SHRUB = new BasicTree(new WorldGenShrub(JUNGLE_LOG, OAK_LEAF));
    public static final ITreeType JUNGLE_MEGA_TREE = new BasicTree(new WorldGenMegaJungle(false, 10, 20, JUNGLE_LOG, JUNGLE_LEAF));
    public static final ITreeType JUNGLE_TREE = new HeightVaryingTree(JUNGLE_LOG, JUNGLE_LEAF, 4, 10, true);
	public static final ITreeType BIRCH_TREE = new BasicTree(new WorldGenBirchTree(false, false));
	public static final ITreeType SAVANNAH_TREE = new BasicTree(new WorldGenSavannaTree(false));
	
    public static final ITreeType PINE_TREE = new BasicTree(new WorldGenTaiga1());
    public static final ITreeType SPRUCE_TREE = new BasicTree(new WorldGenTaiga2(false));
    public static final ITreeType MEGA_PINE_TREE = new BasicTree(new WorldGenMegaPineTree(false, false));
    public static final ITreeType MEGA_SPRUCE_TREE = new BasicTree(new WorldGenMegaPineTree(false, true));
	public static final ITreeType ROOFED_TREE = new BasicTree(new WorldGenCanopyTree(false));
	
	public static interface ITreeType {
		public WorldGenAbstractTree getGenerator(Random random);
		public boolean isMega();
	}
	
	public static class BasicTree implements ITreeType {
		public final WorldGenAbstractTree treeGen;
		
		protected BasicTree(WorldGenAbstractTree treeGen) {
			this.treeGen = treeGen;
		}
		
		@Override
		public WorldGenAbstractTree getGenerator(Random random) {
			return treeGen;
		}

		@Override
		public boolean isMega() {
//			if (treeGen instanceof WorldGenMegaJungle) return true;
//			if (treeGen instanceof WorldGenMegaPineTree) return true;
			if (treeGen instanceof WorldGenHugeTrees) return true;
			if (treeGen instanceof WorldGenCanopyTree) return true;
			return false;
		}
		
	}
	
	public static class HeightVaryingTree implements ITreeType {
		public final IBlockState logState;
		public final IBlockState leafState;
		public final int minHeightMin;
		public final int minHeightMax;
		public final boolean vines;
		
		public HeightVaryingTree(IBlockState log, IBlockState leaf, int minHeightMin, int minHeightMax, boolean vines) {
			super();
			this.logState = log;
			this.leafState = leaf;
			this.minHeightMin = minHeightMin;
			this.minHeightMax = minHeightMax;
			this.vines = vines;
		}
		
		@Override
		public WorldGenAbstractTree getGenerator(Random random) {
			int height = minHeightMin;
			if (minHeightMax > minHeightMin) {
				height += random.nextInt(minHeightMax - minHeightMin + 1);
			}
			return new WorldGenTrees(false, height, logState, leafState, vines);
		}

		@Override
		public boolean isMega() {
			return false;
		}
		
	}
	
}
