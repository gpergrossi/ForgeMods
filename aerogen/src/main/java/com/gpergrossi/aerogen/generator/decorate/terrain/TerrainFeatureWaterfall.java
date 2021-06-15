package com.gpergrossi.aerogen.generator.decorate.terrain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gpergrossi.aerogen.definitions.biomes.IslandBiome;
import com.gpergrossi.aerogen.generator.decorate.PopulatePhase;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.regions.features.river.RiverWaterfall;
import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.util.geom.shapes.Ray;
import com.gpergrossi.util.geom.vectors.Double2D;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class TerrainFeatureWaterfall implements ITerrainFeature {

	protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
	protected static final IBlockState STONE = Blocks.STONE.getDefaultState();

	protected static void setBlockSafe(ChunkPrimer primer, int x, int y, int z, IBlockState blockstate) {
		if (x < 0 || x > 15 || y < 0 || y > 255 || z < 0 || z > 15) return;
		primer.setBlockState(x, y, z, blockstate);
	}
	
	public TerrainFeatureWaterfall(RiverWaterfall waterfall, Random random) {		
		this.waterfall = waterfall;
		this.island = waterfall.getSource().getIsland();
		
		this.vecXZ = this.waterfall.getLocation();
		this.perpXZ = this.vecXZ.createPerpendicular();
		this.posY = this.island.getAltitude();
		this.width = this.island.getHeightmap().getRiverWidth((float) this.vecXZ.getStartX(), (float) this.vecXZ.getStartY());

		int centerX = (int) Math.floor(this.vecXZ.getStartX());
		int centerZ = (int) Math.floor(this.vecXZ.getStartY());
		
		// Calculate the minimum bounding box
		{
			Double2D.Mutable scratch = new Double2D.Mutable();
			Double2D.Mutable center = new Double2D.Mutable();
			this.vecXZ.get(center, 0);
	
			this.vecXZ.get(scratch, -8);
			Double2D pt0 = scratch.copy();
			
			this.vecXZ.get(scratch, 3);
			Double2D pt1 = scratch.copy();
			
			Double2D pt00 = pt0, pt01 = pt0.copy();
			Double2D pt10 = pt1, pt11 = pt1.copy();

			this.perpXZ.get(scratch, this.width+6);
			scratch.subtract(center);
			pt00.subtract(scratch);
			pt10.subtract(scratch);
			pt01.add(scratch);
			pt11.add(scratch);
			
			int minX = (int) Math.floor(Math.min(Math.min(pt00.x(), pt01.x()), Math.min(pt10.x(), pt11.x())));
			int minZ = (int) Math.floor(Math.min(Math.min(pt00.y(), pt01.y()), Math.min(pt10.y(), pt11.y())));
			int maxX = (int) Math.ceil(Math.max(Math.max(pt00.x(), pt01.x()), Math.max(pt10.x(), pt11.x())));
			int maxZ = (int) Math.ceil(Math.max(Math.max(pt00.y(), pt01.y()), Math.max(pt10.y(), pt11.y())));
			
			this.rangeXZ = new Int2DRange(minX, minZ, maxX, maxZ);
			this.minY = this.posY - 3;
			this.maxY = this.posY + 2;
		}
		
		// Create sub feature boulders
		{
			int y = this.posY-1;
			int x0 = (int) Math.floor(centerX + 0.5 + this.perpXZ.getDX()*(this.width*1.5) - this.vecXZ.getDX()*3);
			int z0 = (int) Math.floor(centerZ + 0.5 + this.perpXZ.getDY()*(this.width*1.5) - this.vecXZ.getDY()*3);
			int x1 = (int) Math.floor(centerX + 0.5 - this.perpXZ.getDX()*(this.width*1.5) - this.vecXZ.getDX()*3);
			int z1 = (int) Math.floor(centerZ + 0.5 - this.perpXZ.getDY()*(this.width*1.5) - this.vecXZ.getDY()*3);
			
			this.subFeatures = new ArrayList<>();
			this.subFeatures.add(new TerrainFeatureBoulder(STONE, x0, y, z0, random.nextDouble()*1.5+2.0, random));
			this.subFeatures.add(new TerrainFeatureBoulder(STONE, x1, y, z1, random.nextDouble()*1.5+2.0, random));
		}
	}

	RiverWaterfall waterfall;
	protected Island island;

	protected Ray vecXZ;
	protected Ray perpXZ;
	protected int posY;
	protected double width; 
	
	protected Int2DRange rangeXZ;
	protected int minY, maxY;
	
	protected List<ITerrainFeature> subFeatures;	
	
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
	public void provideChunk(ChunkPrimer primer, Int2DRange chunkRange, Random random) {
		Int2DRange overlap = chunkRange.intersect(this.rangeXZ);
		if (overlap.isEmpty()) return;

		IslandBiome biome = this.island.getBiome();
		
		int centerX = (int) Math.floor(this.vecXZ.getStartX());
		int centerZ = (int) Math.floor(this.vecXZ.getStartY());
		
		for (Int2D xz : overlap.getAllMutable()) {
			Double2D search = new Double2D(xz.x() + 0.5, xz.y() + 0.5);
			
			double j = this.vecXZ.dot(search);
			if (j < -8 || j > 3) continue;
			
			double width = this.width;
			if (j <= -6) width = this.width-1;
			else if (j <= -2) width = this.width-0.5;
			else if (j >= 1) width = this.width+1;
			else if (j >= 2) width = this.width+2;

			double i = this.perpXZ.dot(search);
			if (i < -width || i > width) continue;
			
			int x = (int) Math.floor(centerX + 0.5 + this.perpXZ.getDX()*i + this.vecXZ.getDX()*j);
			int z = (int) Math.floor(centerZ + 0.5 + this.perpXZ.getDY()*i + this.vecXZ.getDY()*j);
			int y = this.posY-1;
			
			int primerX = x - chunkRange.minX;
			int primerZ = z - chunkRange.minY;
			
			if (j >= 0.5) {
				setBlockSafe(primer, primerX, y, primerZ, AIR);
				setBlockSafe(primer, primerX, y-1, primerZ, AIR);
				setBlockSafe(primer, primerX, y-2, primerZ, AIR);
				setBlockSafe(primer, primerX, y+1, primerZ, AIR);
				setBlockSafe(primer, primerX, y+2, primerZ, AIR);
				setBlockSafe(primer, primerX, y+3, primerZ, AIR);
				continue;
			}
		
			if (j <= -4 || j >= -0.5) y--;
			
			setBlockSafe(primer, primerX, y, primerZ, STONE);
			setBlockSafe(primer, primerX, y+1, primerZ, biome.getWater());
			setBlockSafe(primer, primerX, y+2, primerZ, AIR);
			setBlockSafe(primer, primerX, y+3, primerZ, AIR);

			if (j <= -4) setBlockSafe(primer, primerX, y+2, primerZ, biome.getWater());
			else if(j < -0.5) setBlockSafe(primer, primerX, y-1, primerZ, STONE);
		}
		
		for (ITerrainFeature feature : this.subFeatures) {
			feature.provideChunk(primer, chunkRange, random);
		}
	}

	@Override
	public void populateChunk(World world, Int2DRange chunkRange, Random random, PopulatePhase currentPhase) {
		if (currentPhase != PopulatePhase.POST_POPULATE) return;

		Int2DRange overlap = chunkRange.intersect(this.rangeXZ);
		if (overlap.isEmpty()) return;

		IslandBiome biome = this.island.getBiome();
		
		int centerX = (int) Math.floor(this.vecXZ.getStartX());
		int centerZ = (int) Math.floor(this.vecXZ.getStartY());
		
		for (Int2D xz : overlap.getAllMutable()) {
			Double2D search = new Double2D(xz.x() + 0.5, xz.y() + 0.5);
			
			double j = this.vecXZ.dot(search);
			if (j < -1.5 || j > 0.5) continue;
			
			double width = this.width;
			if (j >= 1) width = this.width+1;
			else if (j >= 2) width = this.width+2;

			double i = this.perpXZ.dot(search);
			if (i < -width || i > width) continue;
			
			int x = (int) Math.floor(centerX + 0.5 + this.perpXZ.getDX()*i + this.vecXZ.getDX()*j);
			int z = (int) Math.floor(centerZ + 0.5 + this.perpXZ.getDY()*i + this.vecXZ.getDY()*j);
			int y = this.posY;
			BlockPos pos = new BlockPos(x, y, z);
			
			if (j >= -0.5) pos = pos.down(1);
			
			world.setBlockState(pos, biome.getFlowingWater());
		}
		
		for (ITerrainFeature feature : this.subFeatures) {
			feature.populateChunk(world, chunkRange, random, currentPhase);
		}
	}
	
	
}
