package dev.mortus.aerogen.world.islands.biomes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.mortus.aerogen.world.gen.features.FeatureRandomMeta;
import dev.mortus.aerogen.world.gen.features.FeatureSingle;
import dev.mortus.aerogen.world.gen.features.FeatureSurfaceCluster;
import dev.mortus.aerogen.world.gen.features.FeatureTrees;
import dev.mortus.aerogen.world.gen.placeables.Placeables;
import dev.mortus.aerogen.world.gen.placement.PlacementHighestBlock;
import dev.mortus.aerogen.world.gen.placement.PlacementIslandEdge;
import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.aerogen.world.islands.IslandDecorator;
import dev.mortus.aerogen.world.islands.IslandHeightmap;

public class IslandBiomeJungle extends IslandBiome {
	
	private static BiomeProperties getBiomeProperties() {
		BiomeProperties properties = new BiomeProperties("Jungle");
		properties.setWaterColor(0x0077FF);
		properties.setTemperature(0.95F).setRainfall(0.9F);
		return properties;
	}

	protected IslandDecorator decorator;
	
	public IslandBiomeJungle(BiomeProperties properties) {
		super(properties);
	}
	
	public IslandBiomeJungle() {
		super(getBiomeProperties());
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
				hasCliffs = true;
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
				.withDesiredCount(12)
			)
		);

		addDefaultWaterFeatures(decorator);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(0.70f, Placeables.TALL_GRASS)
			.addPlacable(0.20f, Placeables.FERN)
			.addPlacable(0.10f, Placeables.DOUBLE_GRASS)
			.withClusterRadius(8)
			.withClusterHeight(4)
			.withClusterDensity(128)
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(3)
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
		
		decorator.addFeature(
			new FeatureSingle(Placeables.VINES_AROUND_BLOCK)
			.withPlacement(
				new PlacementIslandEdge()
				.withDesiredCount(64)
			)
		);
		
		return decorator;
	}

	@Override
	protected List<SpawnListEntry> getWaterCreatureList() {
		List<SpawnListEntry> waterCreatureList = new ArrayList<>();
		return waterCreatureList;
	}
	
	@Override
	protected List<SpawnListEntry> getCreatureList() {
		List<SpawnListEntry> creatureList = new ArrayList<>();
		creatureList.add(new Biome.SpawnListEntry(EntityChicken.class, 10, 4, 4));
		creatureList.add(new Biome.SpawnListEntry(EntityOcelot.class, 2, 1, 1));
		return creatureList;
	}
	
}
