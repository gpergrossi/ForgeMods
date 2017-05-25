package dev.mortus.aerogen.world.islands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import dev.mortus.aerogen.world.islands.biomes.IslandBiome;
import dev.mortus.aerogen.world.regions.Region;
import dev.mortus.util.data.Int2D;
import dev.mortus.util.data.Int2DRange;
import dev.mortus.util.math.func2d.FractalNoise2D;
import dev.mortus.util.math.func2d.Function2D;
import dev.mortus.util.math.geom.Ray;
import dev.mortus.util.math.geom.Vec2;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenTrees;

public class Island {
	
	public static final int LAYER_UNASSIGNED = Integer.MIN_VALUE;

	Region region;
	int id;
	long seed;
	Random random;
	
	List<IslandCell> cells;
	
	boolean constructed = false;
	boolean built = false;
	
	IslandShape shape;
	int altitudeLayer;
	int altitude;
	
	IslandBiome biome;

	int surfaceHeightMin;
	int surfaceHeightMax;
	int extendYDown;
	Function2D noiseD;
	Function2D noiseS;
	double undersideSteepness;
	
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
	
	public void build() {
		if (!constructed) throw new IllegalStateException("Island needs to finishGrant() before being initialized");
		if (built) return;
		built = true;
		
		if (Region.DEBUG_VIEW) {
			shape.build(random);
			return;
		}

		biome = region.getBiome().getRandomIslandBiome(random);
		biome.generateShape(random, shape);
				
		int minHeight = 64;
		int maxHeight = 128;
		
		if (altitudeLayer != LAYER_UNASSIGNED) {
			int heightRange = maxHeight - minHeight + 1;
			double heightPerLayer = heightRange / region.numHeightLayers();
			maxHeight = (int) (minHeight + (this.altitudeLayer+1) * heightPerLayer);
			minHeight = (int) (minHeight + this.altitudeLayer * heightPerLayer);
		}
		
		altitude  = random.nextInt(maxHeight-minHeight+1)+minHeight;
		altitude += random.nextInt(maxHeight-minHeight+1)+minHeight;
		altitude /= 2;

		extendYDown = (int) (shape.maxEdgeDistance*1.5);
		surfaceHeightMin = -extendYDown/8;
		surfaceHeightMax = extendYDown/4;
		
		int minY = altitude-extendYDown;
		int maxY = altitude+surfaceHeightMax;
		if (minY < 0) minY = 0;
		if (maxY > 255) maxY = 255;
		extendYDown = altitude-minY;
		surfaceHeightMax = maxY-altitude;
		if (surfaceHeightMin > surfaceHeightMax) surfaceHeightMin = surfaceHeightMax;
		
		noiseD = new FractalNoise2D(random.nextLong(), 1.0/64.0, 5, 0.6);
		noiseS = new FractalNoise2D(random.nextLong(), 1.0/256.0, 5, 0.4);
		undersideSteepness = 1.4;
		
		if (altitude < 63) undersideSteepness += 0.001 * (63-altitude);
		undersideSteepness += 0.0005 * shape.maxEdgeDistance;
	}
	
	public boolean provideBiomes(int chunkX, int chunkZ, Biome[] biomes) {
		Int2DRange chunkRange = new Int2DRange(chunkX*16, chunkZ*16, chunkX*16+15, chunkZ*16+15);
		Int2DRange overlap = chunkRange.intersect(this.shape.range);
		if (overlap.isEmpty()) return false;
		this.build();
		
		return true;
	}
	
	public boolean provideChunk(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes) {
		Int2DRange chunkRange = new Int2DRange(chunkX*16, chunkZ*16, chunkX*16+15, chunkZ*16+15);
		Int2DRange overlap = chunkRange.intersect(this.shape.range);
		if (overlap.isEmpty()) return false;
		this.build();

		for (Int2D.WithIndex tile : overlap.getAllMutable()) {
			double edgeDistance = shape.getEdgeDistance(tile.x, tile.y);
			double edgeWeight = edgeDistance/shape.maxEdgeDistance;
			if (edgeWeight <= 0) continue;
			
			double d = (noiseD.getValue(tile.x, tile.y)*0.5 + 0.5);
			double s = (noiseS.getValue(tile.x, tile.y)*0.5 + 0.5);
			
			// Start surface
			double surface = ((surfaceHeightMax-surfaceHeightMin+1)*s + surfaceHeightMin);
			
			// Edge smoothing
			double edgeSmoothing = 0;
			if (edgeDistance > 3) edgeSmoothing = 1.0 - 1.0/(0.03*Math.pow(edgeDistance-3, 2)+1); 
			
			// River calculation
			double riverDist = getShape().getRiverDistance(tile.x, tile.y);
			double riverDepth = (s*4+1);
			double riverWidth = (1-s)*6+1;
			double riverBank = 1.2+d;
			double river = Math.pow(riverDist/riverWidth, riverBank);
			double riverSmoothing = 0.0;
			if (river > 1.0) riverSmoothing = 1.0 - 1.0/(0.01*river+1);
			
			// River carving / smoothing
			double carvedSurface = (river-1)*riverDepth-1;
			if (carvedSurface > surface) carvedSurface = surface;
			
			// Water & River+Edge smoothing
			if (surface < 0) surface *= edgeSmoothing;
			else surface *= riverSmoothing + edgeSmoothing*(1-riverSmoothing);
			if (carvedSurface < 0) carvedSurface *= edgeSmoothing;
			else carvedSurface *= riverSmoothing + edgeSmoothing*(1-riverSmoothing);

			// Finish surface
			carvedSurface += altitude;
			surface += altitude;
			
			double dirtDepth = edgeWeight*2+d*2+1;
			double bottom = carvedSurface - extendYDown * Math.pow(edgeWeight, 1.0/undersideSteepness) * (d*0.6 + 0.4);
			
			int surfaceY = (int) (surface);
			int stopY = (int) (carvedSurface);
			int startY = (int) (bottom);
			if (startY < 0) startY = 0;
			if (stopY > 255) stopY = 255;
			
			for (int y = startY; y <= stopY; y++) {
				IBlockState block = Blocks.STONE.getDefaultState();
				if (y > surfaceY-dirtDepth) block = biome.fillerBlock;
				if (y == stopY) {
					if (stopY < altitude) block = Blocks.SAND.getDefaultState();
					else if (block.getBlock() == Blocks.DIRT) block = Blocks.GRASS.getDefaultState();
				}
				primer.setBlockState(tile.x-chunkRange.minX, y, tile.y-chunkRange.minY, block);
			}
			for (int y = stopY+1; y <= altitude; y++) {
				IBlockState block = Blocks.WATER.getDefaultState();
				primer.setBlockState(tile.x-chunkRange.minX, y, tile.y-chunkRange.minY, block);
			}
			
			
			biomes[chunkRange.indexFor(tile)] = biome;
		}
		
		return true;
	}
	
	public void populateChunk(World world, int chunkX, int chunkZ, Random random) {
		Int2DRange chunkRange = new Int2DRange(chunkX*16, chunkZ*16, chunkX*16+15, chunkZ*16+15);
		Int2DRange overlap = chunkRange.intersect(this.shape.range);
		if (overlap.isEmpty()) return;
		this.build();
		
		for (RiverWaterfall waterfall : shape.getWaterfalls()) {
			Ray ray = waterfall.getLocation();
			
			double rayX = ray.getStartX();
			if (rayX < overlap.minX || rayX > overlap.maxX+1) continue;
			double rayZ = ray.getStartY();
			if (rayZ < overlap.minY || rayZ > overlap.maxY+1) continue;
			
			double s = (noiseS.getValue(rayX, rayZ)*0.5 + 0.5);
			double riverWidth = (1-s)*6+1;
			if (waterfall.isSource(this)) genWaterfall(world, ray, riverWidth, random);
			if (waterfall.isDestination(this)) genWaterBasin(world, new Vec2(ray.getX(3), ray.getY(3)), riverWidth+4, 5, random);
		}
		
		Int2DRange chunkPopulateRange = chunkRange.offset(8, 8);
		
		WorldGenTrees treeGen = new WorldGenTrees(false);
		WorldGenBigTree bigTreeGen = new WorldGenBigTree(false);
		
		double bigTreeChance = 0.05;
		int treesPerChunk = 6;
		
        for (int i = 0; i < treesPerChunk; i++) {
        	Int2D pos = chunkPopulateRange.randomTile(random);
            if (!shape.contains(pos)) continue;
            
            BlockPos blockpos = world.getHeight(new BlockPos(pos.x, 0, pos.y));
            if (random.nextDouble() < bigTreeChance) {
            	if (bigTreeGen.generate(world, random, blockpos)) {
	            	bigTreeGen.generateSaplings(world, random, blockpos);
	            }
            } else {
            	if (treeGen.generate(world, random, blockpos)) {
	            	treeGen.generateSaplings(world, random, blockpos);
	            }
            }
        }

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
				world.setBlockState(pos, Blocks.WATER.getDefaultState());
			} else if (pos.getY()-altitude >= bottom-edge) {
				Block block = world.getBlockState(pos).getBlock();
				if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) continue;
				
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
				world.setBlockState(pos.add(0, 1, 0), Blocks.WATER.getDefaultState());
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

	public boolean isComplete() {
		return built;
	}

}
