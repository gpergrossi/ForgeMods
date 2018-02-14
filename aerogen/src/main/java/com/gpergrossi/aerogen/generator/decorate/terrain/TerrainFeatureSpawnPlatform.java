package com.gpergrossi.aerogen.generator.decorate.terrain;

import java.util.Random;

import com.gpergrossi.aerogen.definitions.biomes.IslandBiome;
import com.gpergrossi.aerogen.generator.decorate.PopulatePhase;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.util.geom.vectors.Int3D;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class TerrainFeatureSpawnPlatform implements ITerrainFeature {

	protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
	protected static final IBlockState STONE = Blocks.STONE.getDefaultState();
	protected static final IBlockState GOLD = Blocks.GOLD_BLOCK.getDefaultState();

	protected static void setBlockSafe(ChunkPrimer primer, int x, int y, int z, IBlockState blockstate) {
		if (x < 0 || x > 15 || y < 0 || y > 255 || z < 0 || z > 15) return;
		primer.setBlockState(x, y, z, blockstate);
	}
	
	protected static IBlockState getBlockSafe(ChunkPrimer primer, int x, int y, int z) {
		if (x < 0 || x > 15 || y < 0 || y > 255 || z < 0 || z > 15) return AIR;
		return primer.getBlockState(x, y, z);
	}

	
	Island island;
	
	Int3D position;
	int radius = 3;
	int depth = 3;
	
	Int2DRange rangeXZ;
	
	public TerrainFeatureSpawnPlatform(Island island, BlockPos spawnPos) {
		this.island = island;
		this.position = new Int3D(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
		this.rangeXZ = new Int2DRange(position.x()-radius, position.z()-radius, position.x()+radius, position.z()+radius);
	}

	public BlockPos getSpawnPos() {
		return new BlockPos(position.x(), position.y(), position.z());
	}
	
	@Override
	public Int2DRange getRangeXZ() {
		return rangeXZ;
	}

	@Override
	public int getMinY() {
		return position.y()-depth;
	}

	@Override
	public int getMaxY() {
		return position.y()-1;
	}

	@Override
	public void provideChunk(ChunkPrimer primer, Int2DRange chunkRange, Random random) {
		Int2DRange overlap = chunkRange.intersect(this.rangeXZ);
		if (overlap.isEmpty()) return;

		IslandBiome biome = this.island.getBiome();
		
		for (Int2D xz : overlap.getAllMutable()) {
			for (int y = -4; y <= -1; y++) {
				double dx = (xz.x() - position.x());
				double dz = (xz.y() - position.z());
				double dist = Math.sqrt(dx*dx + dz*dz);
				
				double bottom = (Math.pow(dist/radius, 3.0)-1)*depth;

				int primerX = xz.x() - chunkRange.minX;
				int primerY = getMaxY() + y;
				int primerZ = xz.y() - chunkRange.minY;
				
				if (y >= bottom) {
					IBlockState blockState = biome.getBlockByDepth(this.island, xz.x(), primerY, xz.y());
					setBlockSafe(primer, primerX, primerY, primerZ, blockState);
				}
			}
		}
	}

	@Override
	public void populateChunk(World world, Int2DRange chunkRange, Random random, PopulatePhase currentPhase) {}

}
