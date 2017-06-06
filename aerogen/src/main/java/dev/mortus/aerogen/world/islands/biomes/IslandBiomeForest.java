package dev.mortus.aerogen.world.islands.biomes;

import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenTrees;
import dev.mortus.aerogen.world.gen.Placement;
import dev.mortus.aerogen.world.gen.FeatureRandomMeta;
import dev.mortus.aerogen.world.gen.FeatureSurfaceCluster;
import dev.mortus.aerogen.world.gen.FeatureTrees;
import dev.mortus.aerogen.world.gen.FeatureUnderwaterBlocks;
import dev.mortus.aerogen.world.gen.FeatureMinable;
import dev.mortus.aerogen.world.gen.Placeable;
import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.aerogen.world.islands.IslandDecorator;

public class IslandBiomeForest extends IslandBiome {
	
	private static BiomeProperties getBiomeProperties() {
		BiomeProperties properties = new BiomeProperties("Forest");
		properties.setWaterColor(0x0077FF);
		return properties;
	}
	
	public IslandBiomeForest() {
		super(getBiomeProperties());
	}
	
    protected static final IBlockState COARSE_DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    protected static final IBlockState GRASS = Blocks.GRASS.getDefaultState();
    protected static final IBlockState HARDENED_CLAY = Blocks.HARDENED_CLAY.getDefaultState();
    protected static final IBlockState STAINED_HARDENED_CLAY = Blocks.STAINED_HARDENED_CLAY.getDefaultState();
    protected static final IBlockState RED_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.RED);
    protected static final IBlockState ORANGE_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE);
    protected static final IBlockState YELLOW_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.YELLOW);
    protected static final IBlockState RED_SAND = Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND);
	
	public IBlockState getBlockByDepth(Island island, int x, int y, int z) {
		return getBlockByDepthStandard(island, x, y, z, Blocks.GRASS.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.STONE.getDefaultState());
	}

	@Override
	protected IslandDecorator createDecorator() {
		
		IslandDecorator decorator = new IslandDecorator();
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.DIRT.getDefaultState())
			.withVeinSize(24)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(3)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.GRAVEL.getDefaultState())
			.allowUndersideVisible(false)
			.withVeinSize(24)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(3)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE))
			.withVeinSize(24)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(4)
			)
		);

		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE))
			.withVeinSize(24)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(4)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE))
			.withVeinSize(24)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(4)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.COAL_ORE.getDefaultState())
			.withVeinSize(12)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(4)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.IRON_ORE.getDefaultState())
			.withVeinSize(9)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(3)
				.withMinDepth(5)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.GOLD_ORE.getDefaultState())
			.withVeinSize(8)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(2)
				.withMinDepth(16)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.REDSTONE_ORE.getDefaultState())
			.withVeinSize(6)
			.allowUndersideVisible(false)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(2)
				.withMinDepth(16)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.DIAMOND_ORE.getDefaultState())
			.withVeinSize(8)
			.allowUndersideVisible(false)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(1)
				.withChanceForExtra(0.5f)
				.withMinDepth(24)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.FLOWING_LAVA.getDefaultState())
			.withVeinSize(4)
			.allowUndersideVisible(false)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(3)
				.withMinDepth(24)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.LAPIS_ORE.getDefaultState())
			.withVeinSize(4)
			.allowUndersideVisible(false)
			.withPlacement(
				new Placement.Interior()
				.withDesiredCount(4)
				.withMinDepth(24)
			)
		);
		
		decorator.addFeature(
			new FeatureTrees()
			.addTreeType(0.95f, new WorldGenTrees(false))
			.addTreeType(0.05f, new WorldGenBigTree(false))
			.withPlacement(
				new Placement.HighestBlock()
				.withDesiredCount(6)
			)
		);
		
		decorator.addFeature(
			new FeatureUnderwaterBlocks(Blocks.CLAY, 4)
			.withPlacement(
				new Placement.WaterSurface()
				.withDesiredCount(2)
			)
		);
		
		decorator.addFeature(
			new FeatureUnderwaterBlocks(Blocks.SAND, 7)
			.withPlacement(
				new Placement.WaterSurface()
				.withDesiredCount(2)
			)
		);
		
		decorator.addFeature(
			new FeatureUnderwaterBlocks(Blocks.GRAVEL, 6)
			.withPlacement(
				new Placement.WaterSurface()
				.withDesiredCount(2)
			)
		);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(0.85f, Placeable.TALL_GRASS)
			.addPlacable(0.10f, Placeable.FERN)
			.addPlacable(0.05f, Placeable.DOUBLE_GRASS)
			.withClusterRadius(8)
			.withClusterHeight(4)
			.withClusterDensity(128)
			.withPlacement(
				new Placement.HighestBlock()
				.withDesiredCount(2)
			)
		);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(1f, Placeable.REEDS)
			.withClusterRadius(4)
			.withClusterHeight(0)
			.withClusterDensity(20)
			.withPlacement(
				new Placement.HighestBlock()
				.withDesiredCount(1)
			)
		);
		
		decorator.addFeature(
			new FeatureRandomMeta()
			.addFeature(1, new FeatureSurfaceCluster().withCluster(4, 4, 24).addPlacable(1, Placeable.MELON).allowPlacementOn(Blocks.GRASS.getDefaultState()))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(4, 4, 24).addPlacable(1, Placeable.PUMPKIN).allowPlacementOn(Blocks.GRASS.getDefaultState()))
			.withPlacement(
				new Placement.HighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/32.0f)
			)
		);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(1f, Placeable.LILY_PAD)
			.withClusterRadius(8)
			.withClusterHeight(0)
			.withClusterDensity(8)
			.withPlacement(
				new Placement.HighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/4.0f)
			)
		);
		
		decorator.addFeature(
			new FeatureRandomMeta()
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.BROWN_MUSHROOM))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.RED_MUSHROOM))
			.withPlacement(
				new Placement.HighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/8.0f)
			)
		);
		
		decorator.addFeature(
			new FeatureRandomMeta()
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.ALLIUM))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.BLUE_ORCHID))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.DANDELION))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.HOUSTONIA))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.ORANGE_TULIP))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.OXEYE_DAISY))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.PAEONIA))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.PINK_TULIP))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.POPPY))		
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.ROSE))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.SUNFLOWER))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.SYRINGA))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeable.WHITE_TULIP))
			.withPlacement(
				new Placement.HighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/16.0f)
			)
		);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(1, Placeable.CACTUS)
			.withCluster(8, 4, 32)
			.withPlacement(
				new Placement.HighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/8.0f)
			)
		);
		
		return decorator;
	}
	
	@Override
	public boolean hasCliffs() {
		return true;
	}
	
}
