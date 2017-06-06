package dev.mortus.aerogen.world.islands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import dev.mortus.aerogen.world.islands.biomes.IslandBiome;
import dev.mortus.aerogen.world.regions.Region;
import dev.mortus.util.data.Int2D;
import dev.mortus.util.data.Int2DRange;
import dev.mortus.util.math.geom.Ray;
import dev.mortus.util.math.geom.Vec2;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class Island {
	
	public static final int LAYER_UNASSIGNED = Integer.MIN_VALUE;

	Region region;
	int id;
	long seed;
	Random random;
	
	boolean constructed = false;
	boolean initialized = false;

	IslandBiome biome;
	IslandShape shape;
	
	protected int altitude;
	IslandHeightmap heightmap;

	int altitudeLayer;
	List<IslandCell> cells;
	
	public Island(Region region, int regionIsleID, long seed) {
		this.region = region;
		this.id = regionIsleID;
		this.seed = seed;
		this.random = new Random(seed);
		this.cells = new ArrayList<>();
		this.altitudeLayer = LAYER_UNASSIGNED;
	}

	public void grantCell(IslandCell cell) {
		if (cell.island != this) throw new IllegalArgumentException("Cell does not belong to island");
		if (cells.contains(cell)) return;
		cells.add(cell);
	}

	public void finishGrant() {
		this.cells = Collections.unmodifiableList(cells);
		this.shape = new IslandShape(this, cells);
		constructed = true;
	}

	public int getAltitudeLayer() {
		return altitudeLayer;
	}

	public void setAltitudeLayer(int altitudeLayer) {
		this.altitudeLayer = altitudeLayer;
	}
	
	public void initialize() {
		if (!constructed) throw new IllegalStateException("Island needs to finishGrant() before being initialized");
		if (this.initialized) return;
		this.initialized = true;

		if (Region.DEBUG_VIEW) {
			this.shape.erode(new IslandErosion(), random);
			return;
		}
		
		this.biome = region.getIslandBiome(this, random);
		this.biome.generateShape(shape, random);

		this.altitude = region.getAltitude(this, random);
		this.heightmap = new IslandHeightmap(this);
		this.heightmap.initialize(random);
	}
	
	public void provideBiomes(Int2DRange range, int[] biomeIDs) {
		Int2DRange overlap = range.intersect(this.shape.range);
		if (overlap.isEmpty()) return;
		this.initialize();
		
		for (Int2D tile : overlap.getAllMutable()) {
			if (!shape.contains(tile)) continue;
			int index = range.indexFor(tile);
			biomeIDs[index] = Biome.getIdForBiome(this.biome);
		}
	}
	
	public boolean provideChunk(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes) {
		Int2DRange chunkRange = new Int2DRange(chunkX*16, chunkZ*16, chunkX*16+15, chunkZ*16+15);
		Int2DRange overlap = chunkRange.intersect(this.shape.range);
		if (overlap.isEmpty()) return false;
		this.initialize();

		for (Int2D tile : overlap.getAllMutable()) {
			if (!shape.contains(tile)) continue;
			
			int startY = heightmap.getBottom(tile.x, tile.y);
			int stopY = heightmap.getTop(tile.x, tile.y);
			
			for (int y = startY; y <= stopY; y++) {
				IBlockState block = biome.getBlockByDepth(this, tile.x, y, tile.y);
				primer.setBlockState(tile.x-chunkRange.minX, y, tile.y-chunkRange.minY, block);
			}
			for (int y = stopY+1; y <= altitude; y++) {
				primer.setBlockState(tile.x-chunkRange.minX, y, tile.y-chunkRange.minY, biome.getWater());
			}
			
			biomes[chunkRange.indexFor(tile)] = biome;
		}
		
		return true;
	}
	
	public void populateChunk(World world, int chunkX, int chunkZ, Random random) {
		Int2DRange chunkRange = new Int2DRange(chunkX*16+8, chunkZ*16+8, chunkX*16+23, chunkZ*16+23);
		Int2DRange overlap = chunkRange.intersect(this.shape.range);
		if (overlap.isEmpty()) return;
		this.initialize();
		
		for (RiverWaterfall waterfall : shape.getWaterfalls()) {
			Ray ray = waterfall.getLocation();
			
			double rayX = ray.getStartX();
			if (rayX < overlap.minX || rayX > overlap.maxX+1) continue;
			double rayZ = ray.getStartY();
			if (rayZ < overlap.minY || rayZ > overlap.maxY+1) continue;
			
			double s = heightmap.getSurfaceNoise(rayX, rayZ);
			double riverWidth = (1-s)*6+1;
			if (waterfall.isSource(this)) genWaterfall(world, ray, riverWidth, random);
			if (waterfall.isDestination(this)) genWaterBasin(world, new Vec2(ray.getX(3), ray.getY(3)), riverWidth+4, 5, random);
		}
		
		biome.decorate(world, this, chunkRange, overlap, random);
	}
	
	private void genWaterBasin(World world, Vec2 location, double radius, double depth, Random random) {
		double edge = 4.0;
		
		double sx = 1.0+random.nextDouble()*0.2;
		double sz = 1.0+random.nextDouble()*0.2;
		
		BlockPos anchor = new BlockPos(location.x(), altitude, location.y());
		
		for (BlockPos pos : BlockPos.getAllInBoxMutable(anchor.add(-radius-edge, -depth-edge-2, -radius-edge), anchor.add(radius+edge, 0, radius+edge))) {
			double dx = (pos.getX()-location.x())*sx;
			double dz = (pos.getZ()-location.y())*sz;
			double dist = Math.sqrt(dx*dx + dz*dz);
			
			double bottom = (Math.pow(dist/radius, 3.0)-1)*depth;
			
			if (pos.getY()-altitude >= bottom) {
				world.setBlockState(pos, biome.getWater());
			} else if (pos.getY()-altitude >= bottom-edge) {
				Block block = world.getBlockState(pos).getBlock();
				if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) continue;
				if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) continue;
				
				if (pos.getY() == altitude) {
					world.setBlockState(pos, Blocks.GRASS.getDefaultState());
				} else {
					world.setBlockState(pos, Blocks.STONE.getDefaultState());
				}
			}
		}
	}

	private void genWaterfall(World world, Ray ray, double riverWidth, Random random) {
		int blockX = (int) Math.floor(ray.getStartX());
		int blockZ = (int) Math.floor(ray.getStartY());
		Ray perp = ray.createPerpendicular();
		
		BlockPos anchor = new BlockPos(blockX, altitude-1, blockZ);
		for (int searchZ = blockZ-7; searchZ <= blockZ+7; searchZ++) {
			for (int searchX = blockX-7; searchX <= blockX+7; searchX++) {
				Vec2 search = new Vec2(searchX, searchZ);
				double j = ray.dot(search);
				if (j < -8 || j > 3) continue;
				
				double i = perp.dot(search);

				double width = riverWidth;
				if (j <= -6) width = riverWidth-1;
				else if (j <= -2) width = riverWidth-0.5;
				else if (j >= 1) width = riverWidth+1;
				else if (j >= 2) width = riverWidth+2;
				
				if (i < -width || i > width) continue;
				
				if (j >= 0.5) {
					BlockPos pos = anchor.add(perp.getDX()*i+ray.getDX()*j, 0, perp.getDY()*i+ray.getDY()*j);
					world.setBlockToAir(pos.add(0, -2, 0));
					world.setBlockToAir(pos.add(0, -1, 0));
					world.setBlockToAir(pos);
					world.setBlockToAir(pos.add(0, 1, 0));
					world.setBlockToAir(pos.add(0, 2, 0));
					world.setBlockToAir(pos.add(0, 3, 0));
					continue;
				}
				
				double y = 0;				
				if (j <= -4 || j >= -0.5) y = -1;
				BlockPos pos = anchor.add(perp.getDX()*i+ray.getDX()*j, y, perp.getDY()*i+ray.getDY()*j);
				world.setBlockState(pos, Blocks.STONE.getDefaultState());
				world.setBlockState(pos.add(0, 1, 0), biome.getWater());
				world.setBlockToAir(pos.add(0, 2, 0));
				world.setBlockToAir(pos.add(0, 3, 0));
				
				if (j <= -4) world.setBlockState(pos.add(0, 2, 0), Blocks.WATER.getDefaultState());
				else world.setBlockState(pos.add(0, -1, 0), Blocks.STONE.getDefaultState());				
			}	
		}
		BlockPos[] boulders = new BlockPos[2];
		boulders[0] = anchor.add(perp.getDX()*(riverWidth+1.5)-3*ray.getDX(), 0, perp.getDY()*(riverWidth+1.5)-3*ray.getDY());
		boulders[1] = anchor.add(-perp.getDX()*(riverWidth+1.5)-3*ray.getDX(), 0, -perp.getDY()*(riverWidth+1.5)-3*ray.getDY());
		
		genBoulder(world, boulders[0], random.nextDouble()*1.5+2.0, random);
		genBoulder(world, boulders[1], random.nextDouble()*1.5+2.0, random);
	}
	
	private void genBoulder(World world, BlockPos pos, double size, Random random) {
		double cx = pos.getX() + random.nextDouble();
		double cy = pos.getY() + random.nextDouble();
		double cz = pos.getZ() + random.nextDouble();
		
		double sx = 1.0+random.nextDouble()*0.2;
		double sy = 1.0+random.nextDouble()*0.2;
		double sz = 1.0+random.nextDouble()*0.2;
		
		double size2 = size*size;
		
		for (BlockPos p : BlockPos.getAllInBoxMutable(pos.add(-size, -size, -size), pos.add(size, size, size))) {
			double dx = (p.getX()-cx)*sx;
			double dy = (p.getY()-cy)*sy;
			double dz = (p.getZ()-cz)*sz;
			if (dx*dx + dy*dy + dz*dz < size2) world.setBlockState(p, Blocks.STONE.getDefaultState());
		}
	}

	@Override
	public String toString() {
		return "Island ("+region.getCoord().x+", "+region.getCoord().y+"):"+id+" (seed: "+seed+")";
	}

	public long getSeed() {
		return seed;
	}

	public int getAltitude() {
		return altitude;
	}
	
	public IslandShape getShape() {
		return shape;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public IslandBiome getBiome() {
		return biome;
	}

	public IslandHeightmap getHeightmap() {
		return heightmap;
	}

}
