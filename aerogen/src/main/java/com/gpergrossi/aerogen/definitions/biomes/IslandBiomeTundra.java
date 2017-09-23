package com.gpergrossi.aerogen.definitions.biomes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gpergrossi.aerogen.AeroGenMod;
import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSingle;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSurfaceCluster;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureTrees;
import com.gpergrossi.aerogen.generator.decorate.placeables.Placeables;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementHighestBlock;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.extrude.IslandHeightmap;

public class IslandBiomeTundra extends IslandBiome {
    
	private static BiomeProperties getBiomeProperties() {
		BiomeProperties properties = new BiomeProperties("Tundra");
		properties.setWaterColor(0x0077FF);
		properties.setTemperature(-0.5F).setRainfall(0.4F).setSnowEnabled();
		return properties;
	}

	protected IslandDecorator decorator;
	
	public IslandBiomeTundra(BiomeProperties properties) {
		super(properties);
	}
	
	public IslandBiomeTundra() {
		this(getBiomeProperties());
		this.setRegistryName(AeroGenMod.MODID, "biome/sky_tundra");
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
			)
		);
		
		decorator.addFeature(
			new FeatureSingle(Placeables.FOREST_ROCK)
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(0.25f)
			)
		);

		addDefaultWaterFeatures(decorator);
		
		return decorator;
	}

	@Override
	protected List<SpawnListEntry> getCreatureList() {
		List<SpawnListEntry> creatureList = super.getCreatureList();
		creatureList.add(new Biome.SpawnListEntry(EntityWolf.class, 8, 4, 4));
		creatureList.add(new Biome.SpawnListEntry(EntityRabbit.class, 4, 2, 3));
		return creatureList;
	}
	
	@Override
	protected List<SpawnListEntry> getWaterCreatureList() {
		List<SpawnListEntry> waterCreatureList = new ArrayList<>();
		return waterCreatureList;
	}
	
}
