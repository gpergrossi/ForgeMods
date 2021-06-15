package com.gpergrossi.aerogen.definitions.biomes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;
import com.gpergrossi.aerogen.generator.decorate.PopulatePhase;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSurfaceCluster;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureTrees;
import com.gpergrossi.aerogen.generator.decorate.placeables.Placeables;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementHighestBlock;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandHeightmap;
import com.gpergrossi.util.math.func2d.FractalNoise2D;
import com.gpergrossi.util.math.func2d.IFunction2D;

public class IslandBiomeSavannah extends IslandBiome {

	public IslandBiomeSavannah() {}
	
	@Override
	public Biome getMinecraftBiome() {
		return Biomes.SAVANNA;
	}
	
	@Override
	public IBlockState getBlockByDepth(Island island, int x, int y, int z) {
		return getBlockByDepthStandard(island, x, y, z, Blocks.GRASS.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.STONE.getDefaultState());
	}

	@Override
	public IslandHeightmap createHeightMap(Island island) {
		return new IslandHeightmap(island) {
			@Override
			public void initialize(Random random) {
				super.initialize(random);
				maxCliffTier = 1;
			}
			@Override
			protected IFunction2D createSurfaceNoise(Random random) {
				return FractalNoise2D.builder().withSeed(random.nextLong()).withPeriod(256).withOctaves(4, 0.4).withRange(0, 1).build();
			}
		};
	}
	
	@Override
	protected IslandDecorator createDecorator() {
		
		this.decorator = new IslandDecorator();
		
		addDefaultOres(decorator);
		
		decorator.addFeature(
			new FeatureTrees()
			.addTreeType(1.0f, FeatureTrees.SAVANNAH_TREE)
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(0.125f)
				.withPhase(PopulatePhase.PRE_POPULATE)
			)
		);

		addDefaultWaterFeatures(decorator, false, true);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(0.85f, Placeables.TALL_GRASS)
			.addPlacable(0.15f, Placeables.DOUBLE_GRASS)
			.withClusterRadius(8)
			.withClusterHeight(4)
			.withClusterDensity(128)
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(3)
			)
		);
		
		return decorator;
	}
	
}
