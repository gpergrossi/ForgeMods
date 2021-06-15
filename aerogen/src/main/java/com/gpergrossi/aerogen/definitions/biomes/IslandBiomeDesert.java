package com.gpergrossi.aerogen.definitions.biomes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSurfaceCluster;
import com.gpergrossi.aerogen.generator.decorate.placeables.Placeables;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementDesertCluster;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandHeightmap;

public class IslandBiomeDesert extends IslandBiome {
    	
	public IslandBiomeDesert() {}

	@Override
	public Biome getMinecraftBiome() {
		return Biomes.DESERT;
	}
	
	@Override
	public IslandHeightmap createHeightMap(Island island) {
		return new IslandHeightmap(island) {
			@Override
			public void initialize(Random random) {
				super.initialize(random);
				this.surfaceHeightMin = 0;
				this.surfaceHeightMax = 16;
			}
		};
	}

	@Override
	public IBlockState getBlockByDepth(Island island, int x, int y, int z) {
		IslandHeightmap heightmap = island.getHeightmap();
		
		int surfaceY = heightmap.getTop(x, z);
		int sandDepth = heightmap.getDirtLayerDepth(x, z);
		int stoneDepth = sandDepth + 7;
		
		IBlockState block = Blocks.STONE.getDefaultState();
		if (y > surfaceY-stoneDepth) block = Blocks.SANDSTONE.getDefaultState();
		if (y > surfaceY-sandDepth) block = Blocks.SAND.getDefaultState();
		return block;
	}
	
	@Override
	protected IslandDecorator createDecorator() {
		
		this.decorator = new IslandDecorator();
		
		addDefaultOres(decorator);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(1.0f, Placeables.DEAD_BUSH)
			.withCluster(8, 4, 16)
			.withPlacement(
				new PlacementDesertCluster()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/2.0f)
			)
		);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(1, Placeables.CACTUS)
			.withCluster(8, 4, 16)
			.withPlacement(
				new PlacementDesertCluster()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/8.0f)
			)
		);
		
		return decorator;
	}
	
}
