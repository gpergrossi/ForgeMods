package com.gpergrossi.aerogen.definitions.biomes;

import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;
import com.gpergrossi.aerogen.generator.decorate.PopulatePhase;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureMinable;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSurfaceCluster;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureUnderwaterDeposit;
import com.gpergrossi.aerogen.generator.decorate.placeables.Placeables;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementHighestBlock;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementIslandInterior;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementWaterSurface;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandErosion;
import com.gpergrossi.aerogen.generator.islands.IslandHeightmap;
import com.gpergrossi.aerogen.generator.islands.IslandShape;
import com.gpergrossi.util.geom.ranges.Int2DRange;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public abstract class IslandBiome {

	private static final IBlockState WATER = Blocks.WATER.getDefaultState();
	private static final IBlockState FLOWING_WATER = Blocks.FLOWING_WATER.getDefaultState();
	
	
	
	protected IslandDecorator decorator;

	protected final IslandDecorator getDecorator() {
		if (decorator == null) decorator = createDecorator();
		return decorator;
	}
	
	protected abstract IslandDecorator createDecorator();
	
	public final void decorate(World world, Island island, Int2DRange chunkRange, Int2DRange overlapRange, Random random, PopulatePhase currentPhase) {
		IslandDecorator decorator = this.getDecorator();
		if (decorator == null) {
			System.out.println("Null decorator");
			return;
		}
        decorator.decorate(world, island, chunkRange, overlapRange, random, currentPhase);
	}
	
	public String getName() {
		return this.getClass().getName();
	}
	
	public abstract Biome getMinecraftBiome();
	
	public Biome getMinecraftBiomeForSurroundingAir() {
		return getMinecraftBiome();
	}
	
	public final int getBiomeID() {
		return Biome.getIdForBiome(getMinecraftBiome());
	}
	
	/**
	 * Used to attach additional data to an island before it is generated (optional)
	 * @param island
	 */
	public void prepare(Island island) {}

	public void generateShape(IslandShape shape, Random random) {
		shape.erode(new IslandErosion(), random);
	}
	
	public IslandHeightmap createHeightMap(Island island) {
		return new IslandHeightmap(island);
	}
	
	public IBlockState getBlockByDepth(Island island, int x, int y, int z) {
		return getBlockByDepthStandard(island, x, y, z, Blocks.GRASS.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.STONE.getDefaultState());
	}
	
	public IBlockState getWater() {
		return WATER;
	}

	public IBlockState getFlowingWater() {
		return FLOWING_WATER;
	}
	
	public boolean isWater(IBlockState blockState) {
		Block block = blockState.getBlock();
		return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
	}
	
	
	
	
	public static IBlockState getBlockByDepthStandard(Island island, int x, int y, int z, IBlockState grassLayer, IBlockState dirtLayer, IBlockState stoneLayer) {
		IslandHeightmap heightmap = island.getHeightmap();
		
		int surfaceY = heightmap.getTop(x, z);
		int dirtDepth = heightmap.getDirtLayerDepth(x, z);
		
		IBlockState block = stoneLayer;
		if (y > surfaceY-dirtDepth) block = dirtLayer;
		if (y == surfaceY) {
			if (surfaceY < island.getAltitude()) block = Blocks.SAND.getDefaultState();
			else if (block == dirtLayer) block = grassLayer;
		}
		return block;
	}
	
	public static void addDefaultOres(IslandDecorator decorator) {
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.DIRT.getDefaultState())
			.withVeinSize(24)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(4)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.GRAVEL.getDefaultState())
			.allowUndersideVisibleChance(0.0)
			.withVeinSize(24)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(4)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE))
			.withVeinSize(36)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(8)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);

		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE))
			.withVeinSize(36)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(8)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE))
			.withVeinSize(36)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(8)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.COAL_ORE.getDefaultState())
			.withVeinSize(12)
			.allowUndersideVisibleChance(1)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(10)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.IRON_ORE.getDefaultState())
			.withVeinSize(9)
			.allowUndersideVisibleChance(1)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(10)
				.withMinDepth(5)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.GOLD_ORE.getDefaultState())
			.withVeinSize(8)
			.allowUndersideVisibleChance(0.5)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(5)
				.withMinDepth(16)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.LAPIS_ORE.getDefaultState())
			.withVeinSize(4)
			.allowUndersideVisibleChance(0.2)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(5)
				.withMinDepth(24)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.REDSTONE_ORE.getDefaultState())
			.withVeinSize(6)
			.allowUndersideVisibleChance(0.2)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(5)
				.withMinDepth(16)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.DIAMOND_ORE.getDefaultState())
			.withVeinSize(8)
			.allowUndersideVisibleChance(0.2)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(1)
				.withChanceForExtra(0.2f)
				.withMinDepth(24)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.LAVA.getDefaultState())
			.withVeinSize(4)
			.allowUndersideVisibleChance(0.1)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(0)
				.withChanceForExtra(0.5f)
				.withMinDepth(24)
				.scaleWithChunkMass(true)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
	}
	
	public static void addDefaultWaterFeatures(IslandDecorator decorator, boolean lilies, boolean reeds) {
		decorator.addFeature(
			new FeatureUnderwaterDeposit(Blocks.CLAY, 4)
			.withPlacement(
				new PlacementWaterSurface()
				.withDesiredCount(1)
			)
		);
		
		decorator.addFeature(
			new FeatureUnderwaterDeposit(Blocks.DIRT, 3)
			.withPlacement(
				new PlacementWaterSurface()
				.withDesiredCount(1)
			)
		);
		
		decorator.addFeature(
			new FeatureUnderwaterDeposit(Blocks.GRAVEL, 4)
			.withPlacement(
				new PlacementWaterSurface()
				.withDesiredCount(1)
			)
		);
		
		if (lilies) {
			decorator.addFeature(
				new FeatureSurfaceCluster()
				.addPlacable(1f, Placeables.LILY_PAD)
				.withClusterRadius(8)
				.withClusterHeight(0)
				.withClusterDensity(8)
				.withPlacement(
					new PlacementHighestBlock()
					.withDesiredCount(0)
					.withChanceForExtra(1.0f/4.0f)
				)
			);
		}
		
		if (reeds) {
			decorator.addFeature(
				new FeatureSurfaceCluster()
				.addPlacable(1f, Placeables.REEDS)
				.withClusterRadius(4)
				.withClusterHeight(0)
				.withClusterDensity(20)
				.withPlacement(
					new PlacementHighestBlock()
					.withDesiredCount(0)
					.withChanceForExtra(1.0f/4.0f)
				)
			);
		}
	}
	
}
