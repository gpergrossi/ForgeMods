package com.gpergrossi.aerogen.generator.decorate.terrain;

import java.util.Random;

import com.gpergrossi.aerogen.definitions.biomes.IslandBiome;
import com.gpergrossi.aerogen.generator.GenerationPhase;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.regions.features.RiverWaterfall;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.util.geom.shapes.Ray;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class TerrainFeatureBasin implements ITerrainFeature {

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
	
	public TerrainFeatureBasin(RiverWaterfall waterfall, Random random) {		
		this.waterfall = waterfall;
		this.island = waterfall.getDestination().getIsland();
		
		Ray ray = this.waterfall.getLocation();
		
		this.centerX = ray.getX(3);
		this.centerZ = ray.getY(3);
		this.centerY = this.island.getAltitude();

		double width = this.island.getHeightmap().getRiverWidth(ray.getX(centerX), ray.getY(centerZ));

		this.edge = 4.0;
		this.depth = 5.0;
		this.radius = width+5;
		
		int minX = (int) Math.floor(centerX-radius-edge);
		int minZ = (int) Math.floor(centerZ-radius-edge);
		int maxX = (int) Math.ceil(centerX+radius+edge);
		int maxZ = (int) Math.ceil(centerZ+radius+edge);
		
		this.rangeXZ = new Int2DRange(minX, minZ, maxX, maxZ);
		this.minY = (int) (centerY-12);
		this.maxY = (int) centerY;
		
		this.scaleX = 1.0+random.nextDouble()*0.2;
		this.scaleZ = 1.0+random.nextDouble()*0.2;
		
	}

	RiverWaterfall waterfall;
	protected Island island;
	
	protected Int2DRange rangeXZ;
	protected int minY, maxY;
	
	protected double centerX, centerY, centerZ;
	protected double edge, depth, radius;
	protected double scaleX, scaleZ;

	@Override
	public Int2DRange getRangeXZ() {
		return rangeXZ;
	}

	@Override
	public int getMinY() {
		return minY;
	}

	@Override
	public int getMaxY() {
		return maxY;
	}

	@Override
	public boolean provideChunk(ChunkPrimer primer, Int2DRange chunkRange, Random random) {
		Int2DRange overlap = chunkRange.intersect(this.rangeXZ);
		if (overlap.isEmpty()) return false;

		IslandBiome biome = this.island.getBiome();
		
		for (Int2D xz : overlap.getAllMutable()) {
			for (int y = -12; y <= 4; y++) {
				double dx = (xz.x() + 0.5 - centerX)*scaleX;
				double dz = (xz.y() + 0.5 - centerZ)*scaleZ;
				double dist = Math.sqrt(dx*dx + dz*dz);
				
				double bottom = (Math.pow(dist/radius, 3.0)-1)*depth;

				int primerX = xz.x() - chunkRange.minX;
				int primerY = maxY + y; 
				int primerZ = xz.y() - chunkRange.minY;
				
				if (y > 0) {
					if (y >= bottom) setBlockSafe(primer, primerX, primerY, primerZ, AIR);
				} else if (y >= bottom) {
					setBlockSafe(primer, primerX, primerY, primerZ, biome.getWater());
				} else if (y >= bottom-edge) {
					IBlockState blockState = getBlockSafe(primer, primerX, primerY, primerZ);
					if (blockState == biome.getWater() || blockState == biome.getFlowingWater()) continue;
					
					blockState = biome.getBlockByDepth(this.island, xz.x(), primerY, xz.y());
					setBlockSafe(primer, primerX, primerY, primerZ, blockState);
				}
			}
		}
		return true;
	}

	@Override
	public boolean populateChunk(World world, Int2DRange chunkRange, Random random, GenerationPhase currentPhase) {
		return false;
	}	
	
}
