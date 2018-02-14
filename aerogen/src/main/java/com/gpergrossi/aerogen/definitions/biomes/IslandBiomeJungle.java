package com.gpergrossi.aerogen.definitions.biomes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;
import com.gpergrossi.aerogen.generator.decorate.PopulatePhase;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureRandomMeta;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSingle;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSurfaceCluster;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureTrees;
import com.gpergrossi.aerogen.generator.decorate.placeables.Placeables;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementHighestBlock;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementIslandEdge;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.extrude.IslandHeightmap;

public class IslandBiomeJungle extends IslandBiome {

	public IslandBiomeJungle() {}
	
	@Override
	public Biome getMinecraftBiome() {
		return Biomes.JUNGLE;
	}
	
	@Override
	public IBlockState getBlockByDepth(Island island, int x, int y, int z) {
		return getBlockByDepthStandard(island, x, y, z, Blocks.GRASS.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.STONE.getDefaultState());
	}

	@Override
	public IslandHeightmap getHeightMap(Island island) {
		return new IslandHeightmap(island) {
			@Override
			public void initialize(Random random) {
				super.initialize(random);
				maxCliffTier = 1;
			}
		};
	}
	
	@Override
	protected IslandDecorator createDecorator() {
		
		this.decorator = new IslandDecorator();
		
		addDefaultOres(decorator);
		
		decorator.addFeature(
			new FeatureTrees()
			.addTreeType(0.1f, FeatureTrees.BIG_OAK_TREE)
			.addTreeType(0.3f, FeatureTrees.JUNGLE_SHRUB)
			.addTreeType(0.15f, FeatureTrees.JUNGLE_MEGA_TREE)
			.addTreeType(0.55f, FeatureTrees.JUNGLE_TREE)
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(50)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);
		
//		decorator.addFeature(
//			new FeatureSingle(new PlaceableBigTree())
//			.withPlacement(
//				new PlacementHighestBlock()
//				.withDesiredCount(1)
//			)
//		);

		addDefaultWaterFeatures(decorator, true, true);
		
		decorator.addFeature(
			new FeatureSingle(Placeables.TALL_GRASS)
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(256)
			)
		);
		
//		decorator.addFeature(
//			new FeatureSurfaceCluster()
//			.addPlacable(0.70f, Placeables.TALL_GRASS)
//			.addPlacable(0.20f, Placeables.FERN)
//			.addPlacable(0.10f, Placeables.DOUBLE_GRASS)
//			.withCluster(8, 4, 128)
//			.withPlacement(
//				new PlacementHighestBlock()
//				.withDesiredCount(25)
//			)
//		);
		
		decorator.addFeature(
			new FeatureRandomMeta()
			.addFeature(1, new FeatureSurfaceCluster().withCluster(4, 4, 24).addPlacable(1, Placeables.MELON).allowPlacementOn(Blocks.GRASS.getDefaultState()))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(4, 4, 24).addPlacable(1, Placeables.PUMPKIN).allowPlacementOn(Blocks.GRASS.getDefaultState()))
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/32.0f)
			)
		);
		
		decorator.addFeature(
			new FeatureRandomMeta()
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.BROWN_MUSHROOM))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.RED_MUSHROOM))
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/8.0f)
			)
		);
		
		decorator.addFeature(
			new FeatureRandomMeta()
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.ALLIUM))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.BLUE_ORCHID))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.DANDELION))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.HOUSTONIA))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.ORANGE_TULIP))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.OXEYE_DAISY))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.PAEONIA))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.PINK_TULIP))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.POPPY))		
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.ROSE))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.SUNFLOWER))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.SYRINGA))
			.addFeature(1, new FeatureSurfaceCluster().withCluster(8, 4, 64).addPlacable(1, Placeables.WHITE_TULIP))
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/16.0f)
			)
		);
		
		decorator.addFeature(
			new FeatureSingle(Placeables.VINES_AROUND_BLOCK)
			.withPlacement(
				new PlacementIslandEdge()
				.withDesiredCount(64)
			)
		);
		
		return decorator;
	}
	
}
