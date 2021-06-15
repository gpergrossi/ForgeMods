package com.gpergrossi.aerogen.generator.decorate.terrain;

import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.PopulatePhase;
import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class TerrainFeatureBoulder implements ITerrainFeature {

	protected static void setBlockSafe(ChunkPrimer primer, int x, int y, int z, IBlockState blockstate) {
		if (x < 0 || x > 15 || y < 0 || y > 255 || z < 0 || z > 15) return;
		primer.setBlockState(x, y, z, blockstate);
	}
	
	protected Int2DRange rangeXZ;
	protected int minY, maxY;
	
	protected double centerX, centerY, centerZ;
	protected double sizeFactorX, sizeFactorY, sizeFactorZ;
	protected double sizeSquared;
	protected IBlockState blockstate;
	
	public TerrainFeatureBoulder(IBlockState blockstate, int x, int y, int z, double size, Random random) {
		this.blockstate = blockstate;
		
		centerX = x + random.nextDouble();
		centerY = y + random.nextDouble();
		centerZ = z + random.nextDouble();
		
		sizeFactorX = 1.0+random.nextDouble()*0.2;
		sizeFactorY = 1.0+random.nextDouble()*0.2;
		sizeFactorZ = 1.0+random.nextDouble()*0.2;
		
		minY = (int) Math.floor(y - size / sizeFactorY);
		maxY = (int) Math.ceil(y + size / sizeFactorY);
		
		int minX = (int) Math.floor(x - size / sizeFactorX);
		int maxX = (int) Math.ceil(x + size / sizeFactorX);
		int minZ = (int) Math.floor(z - size / sizeFactorZ);
		int maxZ = (int) Math.ceil(z + size / sizeFactorZ);
		
		this.rangeXZ = new Int2DRange(minX, minZ, maxX, maxZ);
		
		sizeSquared = size * size;
	}
	
	@Override
	public Int2DRange getRangeXZ() {
		return this.rangeXZ;
	}

	@Override
	public int getMinY() {
		return this.minY;
	}

	@Override
	public int getMaxY() {
		return this.maxY;
	}

	@Override
	public void provideChunk(ChunkPrimer primer, Int2DRange chunkRange, Random random) {
		Int2DRange overlap = chunkRange.intersect(this.rangeXZ);
		if (overlap.isEmpty()) return;
		
		for (Int2D xz : overlap.getAllMutable()) {
			for (int y = minY; y <= maxY; y++) {
				double dx = (xz.x()-centerX)*sizeFactorX;
				double dz = (xz.y()-centerZ)*sizeFactorZ;
				double dy = (y-centerY)*sizeFactorY;

				int primerX = xz.x() - chunkRange.minX;
				int primerZ = xz.y() - chunkRange.minY;
				
				if (dx*dx + dy*dy + dz*dz < sizeSquared) {
					setBlockSafe(primer, primerX, y, primerZ, blockstate);
				}
			}
		}
	}

	@Override
	public void populateChunk(World world, Int2DRange chunkRange, Random random, PopulatePhase currentPhase) {}
	
	

}
