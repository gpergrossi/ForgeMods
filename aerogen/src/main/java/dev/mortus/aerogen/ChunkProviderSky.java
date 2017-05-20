package dev.mortus.aerogen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import dev.mortus.aerogen.islands.Island;
import dev.mortus.aerogen.regions.Region;
import dev.mortus.aerogen.regions.RegionManager;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;

public class ChunkProviderSky implements IChunkGenerator {

	World world;
	RegionManager regionManager;
	ChunkProviderSettings settings;

	private final boolean mapFeaturesEnabled = true;
	private MapGenBase caveGenerator = new MapGenCaves();
	private MapGenVillage villageGenerator = new MapGenVillage();
	private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
	private MapGenBase ravineGenerator = new MapGenRavine();
	private StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();

	public ChunkProviderSky(World world, long seed, String settingsJSON) {
		this.world = world;
		this.regionManager = new RegionManager();
		this.settings = ChunkProviderSettings.Factory.jsonToFactory(settingsJSON).build();
		
		{
            caveGenerator = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(caveGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE);
            villageGenerator = (MapGenVillage)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(villageGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE);
            scatteredFeatureGenerator = (MapGenScatteredFeature)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(scatteredFeatureGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.SCATTERED_FEATURE);
            ravineGenerator = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(ravineGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.RAVINE);
            oceanMonumentGenerator = (StructureOceanMonument)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(oceanMonumentGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.OCEAN_MONUMENT);
        }
	}

	@Override
	public Chunk provideChunk(int x, int z) {
		ChunkPrimer primer = new ChunkPrimer();

		Biome[] biomes = new Biome[16 * 16];

		List<Region> regions = new ArrayList<>();
		regionManager.getAll(regions, x * 16, z * 16, x * 16 + 16, z * 16 + 16);

		for (Region region : regions) {
			for (Island island : region.getIslands()) {
				boolean change = island.provideChunk(x, z, primer, biomes);
				if (!change) continue;
				island.replaceBiomeBlocks(this, world, x, z, primer);
			}
		}

		if (this.settings.useCaves)
			this.caveGenerator.generate(this.world, x, z, primer);
		if (this.settings.useRavines)
			this.ravineGenerator.generate(this.world, x, z, primer);

		if (this.mapFeaturesEnabled) {
			if (this.settings.useVillages)
				this.villageGenerator.generate(this.world, x, z, primer);
			if (this.settings.useTemples)
				this.scatteredFeatureGenerator.generate(this.world, x, z, primer);
			if (this.settings.useMonuments)
				this.oceanMonumentGenerator.generate(this.world, x, z, primer);
		}

		Chunk chunk = new Chunk(world, primer, x, z);

		// Biomes
		byte[] chunkBiomes = chunk.getBiomeArray();
		for (int i = 0; i < 256; i++) {
			chunkBiomes[i] = (byte) Biome.getIdForBiome(biomes[i]);
		}

		// Lighting
		chunk.generateSkylightMap();

		return chunk;
	}

	public void populate(int x, int z) {
		BlockFalling.fallInstantly = false;
		int i = x * 16;
		int j = z * 16;
		BlockPos blockpos = new BlockPos(i, 0, j);
		Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
		Random random = new Random(this.world.getSeed());
		long k = random.nextLong() / 2L * 2L + 1L;
		long l = random.nextLong() / 2L * 2L + 1L;
		random.setSeed((long) x * k + (long) z * l ^ this.world.getSeed());
		boolean flag = false;
		ChunkPos chunkpos = new ChunkPos(x, z);

		net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(true, this, this.world, random, x, z, flag);

		if (this.mapFeaturesEnabled) {
			if (this.settings.useVillages)
				flag = this.villageGenerator.generateStructure(this.world, random, chunkpos);
			if (this.settings.useTemples)
				this.scatteredFeatureGenerator.generateStructure(this.world, random, chunkpos);
			if (this.settings.useMonuments)
				this.oceanMonumentGenerator.generateStructure(this.world, random, chunkpos);
		}

		if (biome != Biomes.DESERT && biome != Biomes.DESERT_HILLS && this.settings.useWaterLakes && !flag
				&& random.nextInt(this.settings.waterLakeChance) == 0)
			if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, random, x, z, flag,
					net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAKE)) {
				int i1 = random.nextInt(16) + 8;
				int j1 = random.nextInt(256);
				int k1 = random.nextInt(16) + 8;
				(new WorldGenLakes(Blocks.WATER)).generate(this.world, random, blockpos.add(i1, j1, k1));
			}

		if (!flag && random.nextInt(this.settings.lavaLakeChance / 10) == 0 && this.settings.useLavaLakes)
			if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, random, x, z, flag,
					net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAVA)) {
				int i2 = random.nextInt(16) + 8;
				int l2 = random.nextInt(random.nextInt(248) + 8);
				int k3 = random.nextInt(16) + 8;

				if (l2 < this.world.getSeaLevel() || random.nextInt(this.settings.lavaLakeChance / 8) == 0) {
					(new WorldGenLakes(Blocks.LAVA)).generate(this.world, random, blockpos.add(i2, l2, k3));
				}
			}

		if (this.settings.useDungeons)
			if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, random, x, z, flag,
					net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.DUNGEON)) {
				for (int j2 = 0; j2 < this.settings.dungeonChance; ++j2) {
					int i3 = random.nextInt(16) + 8;
					int l3 = random.nextInt(256);
					int l1 = random.nextInt(16) + 8;
					(new WorldGenDungeons()).generate(this.world, random, blockpos.add(i3, l3, l1));
				}
			}

		biome.decorate(this.world, random, new BlockPos(i, 0, j));
		if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, random, x, z, flag,
				net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ANIMALS))
			WorldEntitySpawner.performWorldGenSpawning(this.world, biome, i + 8, j + 8, 16, 16, random);
		blockpos = blockpos.add(8, 0, 8);

		if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, random, x, z, flag,
				net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ICE)) {
			for (int k2 = 0; k2 < 16; ++k2) {
				for (int j3 = 0; j3 < 16; ++j3) {
					BlockPos blockpos1 = this.world.getPrecipitationHeight(blockpos.add(k2, 0, j3));
					BlockPos blockpos2 = blockpos1.down();

					if (this.world.canBlockFreezeWater(blockpos2)) {
						this.world.setBlockState(blockpos2, Blocks.ICE.getDefaultState(), 2);
					}

					if (this.world.canSnowAt(blockpos1, true)) {
						this.world.setBlockState(blockpos1, Blocks.SNOW_LAYER.getDefaultState(), 2);
					}
				}
			}
		} // Forge: End ICE

		net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(false, this, this.world, random, x, z, flag);

		BlockFalling.fallInstantly = false;
	}

	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		boolean flag = false;

		if (this.settings.useMonuments && this.mapFeaturesEnabled && chunkIn.getInhabitedTime() < 3600L) {
			Random random = new Random(this.world.getSeed());
			flag |= this.oceanMonumentGenerator.generateStructure(this.world, random, new ChunkPos(x, z));
		}

		return flag;
	}

	public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		Biome biome = this.world.getBiome(pos);

		if (this.mapFeaturesEnabled) {
			if (creatureType == EnumCreatureType.MONSTER && this.scatteredFeatureGenerator.isSwampHut(pos)) {
				return this.scatteredFeatureGenerator.getScatteredFeatureSpawnList();
			}

			if (creatureType == EnumCreatureType.MONSTER && this.settings.useMonuments
					&& this.oceanMonumentGenerator.isPositionInStructure(this.world, pos)) {
				return this.oceanMonumentGenerator.getScatteredFeatureSpawnList();
			}
		}

		return biome.getSpawnableList(creatureType);
	}

	@Nullable
	public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position, boolean p_180513_4_) {
		return !this.mapFeaturesEnabled ? null
				: ("Monument".equals(structureName) && this.oceanMonumentGenerator != null
						? this.oceanMonumentGenerator.getClosestStrongholdPos(worldIn, position, p_180513_4_)
						: ("Village".equals(structureName) && this.villageGenerator != null
								? this.villageGenerator.getClosestStrongholdPos(worldIn, position, p_180513_4_)
								: ("Temple".equals(structureName) && this.scatteredFeatureGenerator != null
										? this.scatteredFeatureGenerator.getClosestStrongholdPos(worldIn, position,
												p_180513_4_)
										: null)));
	}

	public void recreateStructures(Chunk chunkIn, int x, int z) {
		if (this.mapFeaturesEnabled) {
			if (this.settings.useVillages)
				this.villageGenerator.generate(this.world, x, z, (ChunkPrimer) null);
			if (this.settings.useTemples)
				this.scatteredFeatureGenerator.generate(this.world, x, z, (ChunkPrimer) null);
			if (this.settings.useMonuments)
				this.oceanMonumentGenerator.generate(this.world, x, z, (ChunkPrimer) null);
		}
	}

}
