package com.gpergrossi.aerogen.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.gpergrossi.aerogen.generator.data.WorldPrimer;
import com.gpergrossi.aerogen.generator.data.WorldPrimerChunk;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.RegionManager;
import com.gpergrossi.util.data.Tuple2;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.viewframe.AeroGeneratorView;
import com.gpergrossi.viewframe.ViewerFrame;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class AeroGenerator {

	// ========================================
	// ============== Generators ==============
	// ========================================
	
	private static Map<World, AeroGenerator> generators = new HashMap<>();
	
	public static AeroGenerator getGeneratorForWorld(World world) {
		AeroGenerator generator = generators.get(world);
		if (generator == null) {
			generator = new AeroGenerator(world);
			generators.put(world, generator);
		}
		return generator;
	}
	
	public static Set<Map.Entry<World, AeroGenerator>> getGenerators() {
		return generators.entrySet();
	}

	
	
	// ========================================
	// =========== GUI Viewer Frame ===========
	// ========================================
	
	private static boolean guiEnabled = false;
	private static ViewerFrame viewFrame;
	
	public static void toggleGUI() {
		setGUIEnabled(!guiEnabled);
	}
	
	public static void guiClosed() {
		viewFrame = null;
		guiEnabled = false;
	}
	
	public static void setGUIEnabled(boolean enabled) {
		guiEnabled = enabled;
		if (enabled) {
			viewFrame = new ViewerFrame(new AeroGeneratorView());
			viewFrame.setVisible(true);
		} else {
			if (viewFrame != null) {
				viewFrame.close();
				viewFrame = null;
			}
		}
	}
	
	
	
	
	// ========================================
	// ============= Actual Class =============
	// ========================================
	
	private World world;
	private AeroGeneratorSettings settings;
	
	private WorldPrimer worldPrimer;
	private RegionManager regionManager;
	
	private final long seedX;
	private final long seedY;
	
	private AeroGenerator(World world) {
		this.world = world;
		this.settings = new AeroGeneratorSettings(this);
		this.settings.load();

		this.worldPrimer = new WorldPrimer(this);
		this.regionManager = new RegionManager(this);
				
		Random random = new Random(settings.seed);
		seedX = random.nextLong() / 2L * 2L + 1L;
		seedY = random.nextLong() / 2L * 2L + 1L;
		
		this.load();
	}
	
	/**
	 * Attempt to load save state from world AeroGeneratorSave file
	 */
	private void load() {		
	}
	
	public World getWorld() {
		return world;
	}

	public AeroGeneratorSettings getSettings() {
		return this.settings;
	}

	public RegionManager getRegionManager() {
		return regionManager;
	}
	
	public Tuple2<ChunkPrimer, Biome[]> requestChunk(int chunkX, int chunkZ) {
		WorldPrimerChunk chunk = worldPrimer.getOrCreatePrimerChunk(chunkX, chunkZ);
		return chunk.getCompleted();
	}

	public void getBiomeInts(Int2DRange.Integers returnIntsRange) {
		this.worldPrimer.getBiomeInts(returnIntsRange);
	}

	public WorldPrimer getWorldPrimer() {
		return worldPrimer;
	}

	public long getChunkSeed(int chunkX, int chunkZ) {
		return (long) chunkX * seedX + (long) chunkZ * seedY ^ settings.seed;
	}

	public void postPopulate(int chunkX, int chunkZ) {
        int chunkMinX = (chunkX << 4) + 8;
        int chunkMinZ = (chunkZ << 4) + 8;
        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
        
		List<Region> regions = new ArrayList<>();
		regionManager.getAll(regions, chunkBounds);
		
		long seed = getChunkSeed(chunkX, chunkZ);
		Random random = new Random();
		
		for (Region region : regions) {
			for (Island island : region.getIslands()) {
				random.setSeed(seed);
				island.populateChunk(world, chunkBounds, random, GenerationPhase.POST_POPULATE);
			}
		}
		
		// Add snow to cold places and freeze water
        for (Int2D.Mutable block : chunkBounds.getAllMutable()) {
            BlockPos precipitationHeight = this.world.getPrecipitationHeight(new BlockPos(block.x(), 0, block.y()));
            BlockPos waterLevel = precipitationHeight.down();

            if (this.world.canBlockFreezeWater(waterLevel))  {
                this.world.setBlockState(waterLevel, Blocks.ICE.getDefaultState(), 2);
            }

            if (this.world.canSnowAt(precipitationHeight, true)) {
                this.world.setBlockState(precipitationHeight, Blocks.SNOW_LAYER.getDefaultState(), 2);
            }
        }
				
		// Pre-spawn some animals for this chunk
        BlockPos blockpos = new BlockPos(chunkMinX, 0, chunkMinZ);
        Biome biome = this.world.getBiome(blockpos.add(8, 0, 8));
		WorldEntitySpawner.performWorldGenSpawning(this.world, biome, chunkMinX, chunkMinZ, 16, 16, random);
	}

}
