package com.gpergrossi.aerogen.definitions.biomes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import java.util.Random;

import com.gpergrossi.aerogen.generator.GenerationPhase;
import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureTrees;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementHighestBlock;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.extrude.IslandHeightmap;

public class IslandBiomeColdTaiga extends IslandBiome {
	
	public IslandBiomeColdTaiga() {}
	
	@Override
	public Biome getMinecraftBiome() {
		return Biomes.COLD_TAIGA;
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
			.addTreeType(0.33f, FeatureTrees.PINE_TREE)
			.addTreeType(0.67f, FeatureTrees.SPRUCE_TREE)
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(6)
				.withPhase(GenerationPhase.PRE_POPULATE)
			)
		);
		
		// Makes less sense in a COLD taiga
//		decorator.addFeature(
//			new FeatureSingle(Placeables.FOREST_ROCK)
//			.withPlacement(
//				new PlacementHighestBlock()
//				.withDesiredCount(0)
//				.withChanceForExtra(0.25f)
//			)
//		);

		addDefaultWaterFeatures(decorator, false, false);
		
		return decorator;
	}

}
