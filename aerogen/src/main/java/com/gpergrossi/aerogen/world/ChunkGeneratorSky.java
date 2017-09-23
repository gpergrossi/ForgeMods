package com.gpergrossi.aerogen.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.util.data.Tuple2;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorSettings;

public class ChunkGeneratorSky implements IChunkGenerator {
	
	AeroGenerator generator;
	
	World world;
	ChunkGeneratorSettings settings;
	
	private long seedK, seedL;
	private Random random;
	
	public ChunkGeneratorSky(World world, long seed, String settingsJSON) {
		this.generator = AeroGenerator.getGeneratorForWorld(world);
		
		this.world = world;
		this.settings = ChunkGeneratorSettings.Factory.jsonToFactory(settingsJSON).build();
		
		this.random = new Random(this.world.getSeed());
		seedK = random.nextLong() / 2L * 2L + 1L;
		seedL = random.nextLong() / 2L * 2L + 1L;
	}

	@Override
	public Chunk generateChunk(int chunkX, int chunkZ) {
		
		Tuple2<ChunkPrimer, Biome[]> chunkData = generator.requestChunk(chunkX, chunkZ);
		ChunkPrimer primer = chunkData.first;
		Biome[] biomes = chunkData.second;
		
		Chunk chunk = new Chunk(world, primer, chunkX, chunkZ);

		// Biomes
		byte[] chunkBiomes = chunk.getBiomeArray();
		for (int i = 0; i < 256; i++) {
			chunkBiomes[i] = (byte) Biome.getIdForBiome(biomes[i]);
		}

		// Lighting
		chunk.generateSkylightMap();

		return chunk;
	}

	public void populate(int chunkX, int chunkZ) {		
		int chunkMinX = chunkX * 16 + 8;	// Plus 8 offset according to the way minecraft does chunk population
		int chunkMinZ = chunkZ * 16 + 8;	// this creates a good buffer for already loaded chunks around the populating region
		Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
        
		List<Region> regions = new ArrayList<>();
		generator.getRegionManager().getAll(regions, chunkBounds);

		long seed = (long) chunkX * seedK + (long) chunkZ * seedL ^ this.world.getSeed();
		
		for (Region region : regions) {
			for (Island island : region.getIslands()) {
				random.setSeed(seed);
				island.populateChunk(world, chunkBounds, random);
			}
		}

		// Add snow to cold places and freeze water
        for (Int2D.Mutable block : chunkBounds.getAllMutable()) {
            BlockPos precipitationHeight = this.world.getPrecipitationHeight(new BlockPos(block.x(), 0, block.y()));
            BlockPos waterLevel = precipitationHeight.down();

            if (this.world.canBlockFreezeWater(waterLevel))
            {
                this.world.setBlockState(waterLevel, Blocks.ICE.getDefaultState(), 2);
            }

            if (this.world.canSnowAt(precipitationHeight, true))
            {
                this.world.setBlockState(precipitationHeight, Blocks.SNOW_LAYER.getDefaultState(), 2);
            }
        }
		
		// Pre-spawn some animals for this chunk
		random.setSeed(seed);
        BlockPos blockpos = new BlockPos(chunkMinX, 0, chunkMinZ);
        Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
		WorldEntitySpawner.performWorldGenSpawning(this.world, biome, chunkMinX, chunkMinZ, 16, 16, random);
	}

	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		return false;
	}

	public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		Biome biome = this.world.getBiome(pos);
		return biome.getSpawnableList(creatureType);
	}

	@Nullable
	public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position, boolean p_180513_4_) {
		return null;
	}

	public void recreateStructures(Chunk chunkIn, int x, int z) {}

	@Override
	public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
		return null;
	}

	@Override
	public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
		return false;
	}

}
