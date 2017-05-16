package dev.mortus.aerogen;

import java.util.List;

import dev.mortus.cells.Func2DVoronoi;
import dev.mortus.util.math.func2d.Function2D;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;

public class GeneratorSky implements IChunkGenerator {

	World world;
	Function2D noise;
	
	public GeneratorSky(World world) {
		this.world = world;
		//this.noise = new FractalNoise2D(19027095123L, 1.0/1024.0, 8);
		this.noise = new Func2DVoronoi(16*16, 19027095123L);
	}

	@Override
	public Chunk provideChunk(int x, int z) {
		ChunkPrimer primer = new ChunkPrimer();
		
		for (int i = 0; i < 16; i++) {
			for (int k = 0; k < 16; k++) {
				int height = (int) (noise.getValue(x*16+i, z*16+k)*64+32);
				int j = 0;
				for (; j < height*0.9; j++) {
					primer.setBlockState(i, j, k, Blocks.STONE.getDefaultState());
				}
				for (; j < height; j++) {
					primer.setBlockState(i, j, k, Blocks.DIRT.getDefaultState());
				}
				primer.setBlockState(i, j, k, Blocks.GRASS.getDefaultState());
			}	
		}
		
		Chunk chunk = new Chunk(world, primer, x, z);
        chunk.generateSkylightMap();
        
		return chunk;
	}

	@Override
	public void populate(int x, int z) {}

	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		return false;
	}

	@Override
	public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		return null;
	}

	@Override
	public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position, boolean p_180513_4_) {
		return null;
	}

	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z) {}

}
