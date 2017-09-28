package com.gpergrossi.aerogen.definitions.biomes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.gpergrossi.aerogen.generator.GenerationPhase;
import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureMinable;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSurfaceCluster;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureUnderwaterDeposit;
import com.gpergrossi.aerogen.generator.decorate.placeables.Placeables;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementHighestBlock;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementIslandInterior;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementWaterSurface;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.contour.IslandErosion;
import com.gpergrossi.aerogen.generator.islands.contour.IslandShape;
import com.gpergrossi.aerogen.generator.islands.extrude.IslandHeightmap;
import com.gpergrossi.util.data.ranges.Int2DRange;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public abstract class IslandBiome extends Biome {

	private static final IBlockState WATER = Blocks.WATER.getDefaultState();
	private static final IBlockState FLOWING_WATER = Blocks.FLOWING_WATER.getDefaultState();
	
	public static IBlockState getBlockByDepthStandard(Island island, int x, int y, int z, IBlockState grassLayer, IBlockState dirtLayer, IBlockState stoneLayer) {
		IslandHeightmap heightmap = island.getHeightmap();
		
		int surfaceY = heightmap.getTop(x, z);
		int dirtDepth = heightmap.getDirtLayerDepth(x, z);
		
		IBlockState block = stoneLayer;
		if (y > surfaceY-dirtDepth) block = dirtLayer;
		if (y == surfaceY) {
			if (surfaceY < island.getAltitude()) block = Blocks.SAND.getDefaultState();
			else if (block == dirtLayer) block = grassLayer;
		}
		return block;
	}
	
	
	
	protected int biomeID;
	IslandDecorator decorator;
	
	List<Biome.SpawnListEntry> creatureList;
	List<Biome.SpawnListEntry> monsterList;
	List<Biome.SpawnListEntry> waterCreatureList;
	List<Biome.SpawnListEntry> caveCreatureList;
	
	public IslandBiome(BiomeProperties properties) {
		super(properties);
	}

	protected final IslandDecorator getDecorator() {
		if (decorator == null) decorator = createDecorator();
		return decorator;
	}
	
	@Override
	public void decorate(World worldIn, Random rand, BlockPos pos) {
		throw new UnsupportedOperationException("IslandBiomes need to provide a generation phase to decorate()");
	}
	
	public final void decorate(World world, Island island, Int2DRange chunkRange, Int2DRange overlapRange, Random random, GenerationPhase currentPhase) {
		IslandDecorator decorator = this.getDecorator();
		if (decorator == null) {
			System.out.println("Null decorator");
			return;
		}
        decorator.decorate(world, island, chunkRange, overlapRange, random, currentPhase);
	}
	
	public final int getBiomeID() {
		return Biome.getIdForBiome(this);
	}
	
	
	
	public void generateShape(IslandShape shape, Random random) {
		shape.erode(new IslandErosion(), random);
	}

	
	public void prepare(Island island) {}
	
	protected abstract IslandDecorator createDecorator();

	public IslandHeightmap getHeightMap(Island island) {
		return new IslandHeightmap(island);
	}
	
	public IBlockState getBlockByDepth(Island island, int x, int y, int z) {
		return getBlockByDepthStandard(island, x, y, z, Blocks.GRASS.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.STONE.getDefaultState());
	}
	
	public IBlockState getWater() {
		return WATER;
	}

	public IBlockState getFlowingWater() {
		return FLOWING_WATER;
	}
	
	public boolean isWater(IBlockState blockState) {
		Block block = blockState.getBlock();
		return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
	}
	
	public boolean isVoid() {
		return false;
	}
	
	@Override
	public List<SpawnListEntry> getSpawnableList(EnumCreatureType creatureType) {
        switch (creatureType) {
            case MONSTER:
            	if (this.monsterList == null) this.monsterList = getMonsterList();
                return this.monsterList;
            case CREATURE:
            	if (this.creatureList == null) this.creatureList = getCreatureList();
                return this.creatureList;
            case WATER_CREATURE:
            	if (this.waterCreatureList == null) this.waterCreatureList = getWaterCreatureList();
                return this.waterCreatureList;
            case AMBIENT:
            	if (this.caveCreatureList == null) this.caveCreatureList = getCaveCreatureList();
                return this.caveCreatureList;
            default:
                // Forge: Return a non-null list for non-vanilla EnumCreatureTypes
                if (!this.modSpawnableLists.containsKey(creatureType)) this.modSpawnableLists.put(creatureType, Lists.<Biome.SpawnListEntry>newArrayList());
                return this.modSpawnableLists.get(creatureType);
        }
	}

	protected List<SpawnListEntry> getMonsterList() {
		List<SpawnListEntry> monsterList = new ArrayList<>();
		monsterList.add(new Biome.SpawnListEntry(EntitySpider.class, 100, 4, 4));
		monsterList.add(new Biome.SpawnListEntry(EntityZombie.class, 95, 4, 4));
		monsterList.add(new Biome.SpawnListEntry(EntityZombieVillager.class, 5, 1, 1));
		monsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 100, 4, 4));
		monsterList.add(new Biome.SpawnListEntry(EntityCreeper.class, 100, 4, 4));
		monsterList.add(new Biome.SpawnListEntry(EntitySlime.class, 100, 4, 4));
		monsterList.add(new Biome.SpawnListEntry(EntityEnderman.class, 10, 1, 4));
		monsterList.add(new Biome.SpawnListEntry(EntityWitch.class, 5, 1, 1));
		return monsterList;
	}
	
	protected List<SpawnListEntry> getCreatureList() {
		List<SpawnListEntry> creatureList = new ArrayList<>();
		creatureList.add(new Biome.SpawnListEntry(EntitySheep.class, 12, 4, 4));
		creatureList.add(new Biome.SpawnListEntry(EntityPig.class, 10, 4, 4));
		creatureList.add(new Biome.SpawnListEntry(EntityChicken.class, 10, 4, 4));
		creatureList.add(new Biome.SpawnListEntry(EntityCow.class, 8, 4, 4));
		return creatureList;
	}
    
	protected List<SpawnListEntry> getWaterCreatureList() {
		List<SpawnListEntry> waterCreatureList = new ArrayList<>();
	    waterCreatureList.add(new Biome.SpawnListEntry(EntitySquid.class, 10, 4, 4));
		return waterCreatureList;
	}
	
	protected List<SpawnListEntry> getCaveCreatureList() {
		List<SpawnListEntry> caveCreatureList = new ArrayList<>();
	    caveCreatureList.add(new Biome.SpawnListEntry(EntityBat.class, 10, 8, 8));
		return caveCreatureList;
	}
	
	
	
	public static void addDefaultOres(IslandDecorator decorator) {
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.DIRT.getDefaultState())
			.withVeinSize(24)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(2)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.GRAVEL.getDefaultState())
			.allowUndersideVisibleChance(0.0)
			.withVeinSize(24)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(2)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE))
			.withVeinSize(36)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(6)
			)
		);

		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE))
			.withVeinSize(36)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(6)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE))
			.withVeinSize(36)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(6)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.COAL_ORE.getDefaultState())
			.withVeinSize(12)
			.allowUndersideVisibleChance(0.5)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(8)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.IRON_ORE.getDefaultState())
			.withVeinSize(9)
			.allowUndersideVisibleChance(0.5)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(8)
				.withMinDepth(5)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.GOLD_ORE.getDefaultState())
			.withVeinSize(8)
			.allowUndersideVisibleChance(0.5)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(4)
				.withMinDepth(16)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.LAPIS_ORE.getDefaultState())
			.withVeinSize(4)
			.allowUndersideVisibleChance(0.0)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(4)
				.withMinDepth(24)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.REDSTONE_ORE.getDefaultState())
			.withVeinSize(6)
			.allowUndersideVisibleChance(0.0)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(4)
				.withMinDepth(16)
			)
		);
		
		decorator.addFeature(
			new FeatureMinable()
			.withOre(Blocks.DIAMOND_ORE.getDefaultState())
			.withVeinSize(8)
			.allowUndersideVisibleChance(0.0)
			.withPlacement(
				new PlacementIslandInterior()
				.withDesiredCount(0)
				.withChanceForExtra(0.75f)
				.withMinDepth(24)
			)
		);
		
		decorator.addFeature(
				new FeatureMinable()
				.withOre(Blocks.LAVA.getDefaultState())
				.withVeinSize(1)
				.allowUndersideVisibleChance(0.0)
				.withPlacement(
					new PlacementIslandInterior()
					.withDesiredCount(0)
					.withChanceForExtra(0.2f)
					.withMinDepth(24)
				)
			);
	}
	
	public static void addDefaultWaterFeatures(IslandDecorator decorator) {
		decorator.addFeature(
			new FeatureUnderwaterDeposit(Blocks.CLAY, 4)
			.withPlacement(
				new PlacementWaterSurface()
				.withDesiredCount(1)
			)
		);
		
		decorator.addFeature(
			new FeatureUnderwaterDeposit(Blocks.DIRT, 3)
			.withPlacement(
				new PlacementWaterSurface()
				.withDesiredCount(1)
			)
		);
		
		decorator.addFeature(
			new FeatureUnderwaterDeposit(Blocks.GRAVEL, 4)
			.withPlacement(
				new PlacementWaterSurface()
				.withDesiredCount(1)
			)
		);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(1f, Placeables.LILY_PAD)
			.withClusterRadius(8)
			.withClusterHeight(0)
			.withClusterDensity(8)
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/4.0f)
			)
		);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(1f, Placeables.REEDS)
			.withClusterRadius(4)
			.withClusterHeight(0)
			.withClusterDensity(20)
			.withPlacement(
				new PlacementHighestBlock()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/4.0f)
			)
		);		
	}
	
}
