package com.gpergrossi.aerogen.definitions.biomes;

import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSurfaceCluster;
import com.gpergrossi.aerogen.generator.decorate.placeables.Placeables;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementDesertCluster;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.extrude.IslandHeightmap;

import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.biome.Biome;

public class IslandBiomeMesa extends IslandBiome {

    protected static final IBlockState COARSE_DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    protected static final IBlockState GRASS = Blocks.GRASS.getDefaultState();
    protected static final IBlockState HARDENED_CLAY = Blocks.HARDENED_CLAY.getDefaultState();
    protected static final IBlockState STAINED_HARDENED_CLAY = Blocks.STAINED_HARDENED_CLAY.getDefaultState();
    protected static final IBlockState RED_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.RED);
    protected static final IBlockState ORANGE_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE);
    protected static final IBlockState YELLOW_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.YELLOW);
    protected static final IBlockState RED_SAND = Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND);
	
	public IslandBiomeMesa() {}
	
	@Override
	public Biome getMinecraftBiome() {
		return Biomes.MESA;
	}

	@Override
	public void prepare(Island island) {
		
	}
	
	@Override
	public IslandHeightmap getHeightMap(Island island) {
		return new IslandHeightmap(island) {
			@Override
			public void initialize(Random random) {
				super.initialize(random);
				this.surfaceHeightMin = 0;
				this.surfaceHeightMax = 3;
				this.maxCliffTier = 2;
				this.cliffHeight = 8;
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
