package com.gpergrossi.aerogen.definitions.biomes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gpergrossi.aerogen.AeroGenMod;
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
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityRabbit;
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
    
	private static BiomeProperties getBiomeProperties() {
		BiomeProperties properties = new BiomeProperties("Forest");
		properties.setWaterColor(0x0077FF);
		properties.setTemperature(0.7F).setRainfall(0.8F);
		return properties;
	}

	protected IslandDecorator decorator;
	
	public IslandBiomeMesa(BiomeProperties properties) {
		super(properties);
	}
	
	public IslandBiomeMesa() {
		this(getBiomeProperties());
		this.setRegistryName(AeroGenMod.MODID, "biome/sky_mesa");
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
	
	@Override
	protected List<SpawnListEntry> getMonsterList() {
		List<SpawnListEntry> monsterList = new ArrayList<>();
		monsterList.add(new Biome.SpawnListEntry(EntitySpider.class, 100, 4, 4));
		monsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 100, 4, 4));
		monsterList.add(new Biome.SpawnListEntry(EntityCreeper.class, 100, 4, 4));
		monsterList.add(new Biome.SpawnListEntry(EntitySlime.class, 100, 4, 4));
		monsterList.add(new Biome.SpawnListEntry(EntityEnderman.class, 10, 1, 4));
		monsterList.add(new Biome.SpawnListEntry(EntityWitch.class, 5, 1, 1));
		
		// Desert zombies
        monsterList.add(new Biome.SpawnListEntry(EntityZombie.class, 19, 4, 4));
        monsterList.add(new Biome.SpawnListEntry(EntityZombieVillager.class, 1, 1, 1));
        monsterList.add(new Biome.SpawnListEntry(EntityHusk.class, 80, 4, 4));
		
		return monsterList;
	}

	@Override
	protected List<SpawnListEntry> getCreatureList() {
		List<SpawnListEntry> creatureList = new ArrayList<>();
        creatureList.add(new Biome.SpawnListEntry(EntityRabbit.class, 4, 2, 3));
		return creatureList;
	}
	
	@Override
	protected List<SpawnListEntry> getWaterCreatureList() {
		List<SpawnListEntry> waterCreatureList = new ArrayList<>();
		return waterCreatureList;
	}
	
}
