package dev.mortus.aerogen.world.islands.biomes;

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
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.mortus.aerogen.world.gen.features.FeatureSurfaceCluster;
import dev.mortus.aerogen.world.gen.placeables.Placeables;
import dev.mortus.aerogen.world.gen.placement.PlacementDesertCluster;
import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.aerogen.world.islands.IslandDecorator;
import dev.mortus.aerogen.world.islands.IslandHeightmap;

public class IslandBiomeDesert extends IslandBiome {
    
	private static BiomeProperties getBiomeProperties() {
		BiomeProperties properties = new BiomeProperties("Desert");
		properties.setWaterColor(0x22DDFF);
		properties.setTemperature(2.0F).setRainfall(0.0F).setRainDisabled();
		return properties;
	}

	protected IslandDecorator decorator;
	
	public IslandBiomeDesert(BiomeProperties properties) {
		super(properties);
	}
	
	public IslandBiomeDesert() {
		super(getBiomeProperties());
	}
	
	@Override
	public IslandHeightmap getHeightMap(Island island) {
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
