package com.gpergrossi.aerogen.world;

import java.util.List;
import javax.annotation.Nullable;

import com.gpergrossi.aerogen.generator.AeroGenerator;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorSettings;

/**
 * Provides the method implementations for IChunkGenerator. Most work is done
 * in the associated AeroGenerator object.
 */
public class ChunkGeneratorSky implements IChunkGenerator {

	World world;
	AeroGenerator generator;
	
	public ChunkGeneratorSky(World world, long seed, String settingsJSON) {
		this.world = world;
		this.generator = AeroGenerator.getGeneratorForWorld(world);
		this.generator.setChunkGeneratorSettings(ChunkGeneratorSettings.Factory.jsonToFactory(settingsJSON).build());
	}

	@Override
	public Chunk generateChunk(int chunkX, int chunkZ) {
		return generator.generateChunk(chunkX, chunkZ);
	}

	public void populate(int chunkX, int chunkZ) {		
		generator.postPopulate(world, chunkX, chunkZ);
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
