package com.gpergrossi.aerogen.world;

import java.util.List;
import javax.annotation.Nullable;

import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.util.data.Tuple2;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorSettings;

public class ChunkGeneratorSky implements IChunkGenerator {
	
	AeroGenerator generator;
	
	World world;
	ChunkGeneratorSettings settings;
	
	public ChunkGeneratorSky(World world, long seed, String settingsJSON) {
		this.generator = AeroGenerator.getGeneratorForWorld(world);
		
		this.world = world;
		this.settings = ChunkGeneratorSettings.Factory.jsonToFactory(settingsJSON).build();
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
		/**
		 * The Phase.PRE_POPULATE features are done inside WorldPrimerChunk.populate()
		 */
		
		/**
		 * The Phase.POST_POPULATE features are done here:
		 */
		generator.postPopulate(chunkX, chunkZ);
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
