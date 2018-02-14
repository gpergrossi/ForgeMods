package com.gpergrossi.aerogen;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

/**
 * Provides the method implementations for IChunkGenerator. Most work is done
 * in the associated AeroGenerator object.
 */
public class ChunkGeneratorSky implements IChunkGenerator {

	World world;
	AeroGenerator generator;
	
	protected ChunkGeneratorSky(World world) {
		this.world = world;		
		world.getWorldInfo().getGeneratorOptions();
		this.generator = AeroGenerator.getGeneratorForWorld(world);
	}

	@Override
	public Chunk generateChunk(int chunkX, int chunkZ) {
		if (generator == null) {
			ChunkPrimer primer = new ChunkPrimer();
			Chunk mcChunk = new Chunk(world, primer, chunkX, chunkZ);
			return mcChunk;
		}
		return generator.generateChunk(chunkX, chunkZ);
	}

	public void populate(int chunkX, int chunkZ) {
		if (generator == null) return;
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
