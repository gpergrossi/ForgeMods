package com.gpergrossi.aerogen.definitions.biomes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;
import com.gpergrossi.aerogen.generator.decorate.PopulatePhase;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureRandomMeta;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSurfaceCluster;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureTrees;
import com.gpergrossi.aerogen.generator.decorate.placeables.Placeables;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementHighestBlock;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.extrude.IslandHeightmap;

public class IslandBiomeForest extends IslandBiome {
    
	public IslandBiomeForest() {}
	
	@Override
	public Biome getMinecraftBiome() {
		return Biomes.FOREST;
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
			.addTreeType(0.95f, FeatureTrees.OAK_TREE)
			.addTreeType(0.05f, FeatureTrees.BIG_OAK_TREE)
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(6)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);

		addDefaultWaterFeatures(decorator, true, true);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(0.85f, Placeables.TALL_GRASS)
			.addPlacable(0.10f, Placeables.FERN)
			.addPlacable(0.05f, Placeables.DOUBLE_GRASS)
			.withClusterRadius(8)
			.withClusterHeight(4)
			.withClusterDensity(128)
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(2)
			)
		);
		
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
		
		return decorator;
	}
	
}
