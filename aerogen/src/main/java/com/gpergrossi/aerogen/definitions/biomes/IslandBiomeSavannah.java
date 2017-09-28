package com.gpergrossi.aerogen.definitions.biomes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gpergrossi.aerogen.AeroGenMod;
import com.gpergrossi.aerogen.generator.GenerationPhase;
import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSurfaceCluster;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureTrees;
import com.gpergrossi.aerogen.generator.decorate.placeables.Placeables;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementHighestBlock;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.extrude.IslandHeightmap;
import com.gpergrossi.util.math.func2d.FractalNoise2D;
import com.gpergrossi.util.math.func2d.Function2D;
import com.gpergrossi.util.math.func2d.RemapOperation;

public class IslandBiomeSavannah extends IslandBiome {
    
	private static BiomeProperties getBiomeProperties() {
		BiomeProperties properties = new BiomeProperties("Savannah");
		properties.setWaterColor(0x0077FF);
		properties.setTemperature(0.7F).setRainfall(0.8F);
		return properties;
	}

	protected IslandDecorator decorator;
	
	public IslandBiomeSavannah(BiomeProperties properties) {
		super(properties);
	}
	
	public IslandBiomeSavannah() {
		this(getBiomeProperties());
		this.setRegistryName(AeroGenMod.MODID, "biome/sky_savannah");
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
			@Override
			protected Function2D createSurfaceNoise(Random random) {
				Function2D func = new FractalNoise2D(random.nextLong(), 1.0/256.0, 4, 0.4);
				return new RemapOperation(func, v -> v*0.5+0.5);
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
				.withPhase(GenerationPhase.PRE_POPULATE)
			)
		);

		addDefaultWaterFeatures(decorator);
		
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

	@Override
	protected List<SpawnListEntry> getCreatureList() {
		List<SpawnListEntry> creatureList = super.getCreatureList();
		creatureList.add(new Biome.SpawnListEntry(EntityHorse.class, 1, 2, 6));
		creatureList.add(new Biome.SpawnListEntry(EntityDonkey.class, 1, 1, 1));
		creatureList.add(new Biome.SpawnListEntry(EntityLlama.class, 8, 4, 4));
		return creatureList;
	}
	
	@Override
	protected List<SpawnListEntry> getWaterCreatureList() {
		List<SpawnListEntry> waterCreatureList = new ArrayList<>();
		return waterCreatureList;
	}
	
}
