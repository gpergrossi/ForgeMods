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
import dev.mortus.aerogen.world.gen.FeaturePlacement;
import dev.mortus.aerogen.world.gen.IslandFeatureTrees;
import dev.mortus.aerogen.world.gen.IslandMinable;
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

		System.out.println("Forest decorator");
		
		IslandDecorator decorator = new IslandDecorator();
		
		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.DIRT.getDefaultState())
			.withVeinSize(24)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(3)
			)
		);
		
		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.GRAVEL.getDefaultState())
			.allowUndersideVisible(false)
			.withVeinSize(24)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(3)
			)
		);
		
		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE))
			.withVeinSize(24)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(4)
			)
		);

		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE))
			.withVeinSize(24)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(4)
			)
		);
		
		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE))
			.withVeinSize(24)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(4)
			)
		);
		
		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.COAL_ORE.getDefaultState())
			.withVeinSize(12)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(4)
			)
		);
		
		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.IRON_ORE.getDefaultState())
			.withVeinSize(9)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(3)
				.withMinDepth(5)
			)
		);
		
		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.GOLD_ORE.getDefaultState())
			.withVeinSize(8)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(2)
				.withMinDepth(16)
			)
		);
		
		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.REDSTONE_ORE.getDefaultState())
			.withVeinSize(6)
			.allowUndersideVisible(false)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(2)
				.withMinDepth(16)
			)
		);
		
		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.DIAMOND_ORE.getDefaultState())
			.withVeinSize(8)
			.allowUndersideVisible(false)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(1)
				.withChanceForExtra(0.5f)
				.withMinDepth(24)
			)
		);
		
		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.FLOWING_LAVA.getDefaultState())
			.withVeinSize(4)
			.allowUndersideVisible(false)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(3)
				.withMinDepth(24)
			)
		);
		
		decorator.addFeature(
			new IslandMinable()
			.withOre(Blocks.LAPIS_ORE.getDefaultState())
			.withVeinSize(4)
			.allowUndersideVisible(false)
			.withPlacement(
				new FeaturePlacement.Interior()
				.withDesiredCount(4)
				.withMinDepth(24)
			)
		);
		
		decorator.addFeature(
			new IslandFeatureTrees()
			.addTreeType(0.95f, new WorldGenTrees(false))
			.addTreeType(0.05f, new WorldGenBigTree(false))
			.withPlacement(
				new FeaturePlacement.Surface()
				.withDesiredCount(6)
			)
		);
		
		return decorator;
	}
	
	@Override
	public boolean hasCliffs() {
		return true;
	}
	
}
