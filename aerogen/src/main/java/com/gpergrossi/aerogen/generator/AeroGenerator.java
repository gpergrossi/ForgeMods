package com.gpergrossi.aerogen.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.gpergrossi.aerogen.AeroGenMod;
import com.gpergrossi.aerogen.generator.decorate.PopulatePhase;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandCell;
import com.gpergrossi.aerogen.generator.primer.WorldPrimer;
import com.gpergrossi.aerogen.generator.primer.WorldPrimerChunk;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.RegionManager;
import com.gpergrossi.aerogen.save.WorldSettingsHistory;
import com.gpergrossi.tasks.TaskManager;
import com.gpergrossi.tasks.ThreadedFileIOHandler;
import com.gpergrossi.util.data.Tuple2;
import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.viewframe.AeroGeneratorView;
import com.gpergrossi.viewframe.ViewerFrame;

import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.CreateSpawnPosition;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class AeroGenerator {

	// ========================================
	// ============== Generators ==============
	// ========================================
	
	private static TaskManager taskManager;
	
	private static Map<World, AeroGenerator> generators = new HashMap<>();
	
	public static AeroGenerator getGeneratorForWorld(World world) {
		synchronized (generators) {			
			if (!AeroGenMod.isWorldAerogen(world)) return null;
			
			AeroGenerator generator = generators.get(world);
			if (generator == null) {
				generator = new AeroGenerator(world);
				generators.put(world, generator);
			}
			return generator;
		}
	}

	public static Collection<AeroGenerator> getGenerators() {
		return Collections.unmodifiableCollection(generators.values());		
	}
	
	
	// ========================================
	// =========== GUI Viewer Frame ===========
	// ========================================
	
	private static boolean guiEnabled = false;
	private static int guiDimension = 0;
	private static ViewerFrame viewFrame;
	
	public static void toggleGUI() {
		setGUIEnabled(!guiEnabled);
	}
	
	public static void showGUIDimension(int dimension) {
		guiDimension = dimension;
		if (guiEnabled) setGUIEnabled(false);
		setGUIEnabled(true);
	}
	
	public static void guiClosed() {
		viewFrame = null;
		guiEnabled = false;
	}
	
	public static void setGUIEnabled(boolean enabled) {
		guiEnabled = enabled;
		if (enabled) {			
			World world = DimensionManager.getWorld(guiDimension);
			if (world == null) return;
				
			AeroGenerator generator = AeroGenerator.getGeneratorForWorld(world);
			if (generator == null) return;
			
			viewFrame = new ViewerFrame(new AeroGeneratorView(generator));
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
	private boolean initialized = false;
	
	private AeroGeneratorSettings settings;
	private WorldSettingsHistory worldSavedData;
	
	private WorldPrimer worldPrimer;
	private RegionManager regionManager;
	
	boolean seedInitialized = false;
	long seed, seedX, seedY;
	
	private AeroGenerator(World world) {
		this.world = world;
	}

	/**
	 * Init() cannot be called from the AeroGenerator constructor because the perWorld data store
	 * will not behave correctly until later in the server startup process. Init is called by 
	 * onWorldLoad() or onCreateWorldSpawn().
	 */
	private void initialize() {
		if (initialized) return;
		
		this.worldSavedData = WorldSettingsHistory.forWorld(world);
		
		this.settings = worldSavedData.getCurrentSettings();
		AeroGenMod.log.info("Settings: "+settings);

		this.worldPrimer = new WorldPrimer(this);
		this.regionManager = new RegionManager(this, settings);
		
		this.initialized = true;
	}

	public World getWorld() {
		return world;
	}
	
	public RegionManager getRegionManager() {
		return regionManager;
	}
	
	public void getBiomeInts(Int2DRange.Integers returnIntsRange) {
		this.worldPrimer.getBiomeInts(returnIntsRange);		
	}

	public WorldPrimer getWorldPrimer() {
		return worldPrimer;
	}

	public TaskManager getTaskManager() {
		if (taskManager == null) {
			final int numThreads = AeroGenMod.numThreads;
			taskManager = TaskManager.create("AeroGen", numThreads);
			if (numThreads > 0) taskManager.setIOHandler(new ThreadedFileIOHandler());
			taskManager.setTaskMonitorVisible(true);
		}
		return taskManager;
	}
	
	public long getChunkSeed(int chunkX, int chunkZ) {
		if (!this.seedInitialized) {
			this.seed = settings.seed;
			final Random random = new Random(seed);
			this.seedX = random.nextLong();
			this.seedY = random.nextLong();
			seedInitialized = true;
		}
		return chunkX * seedX + chunkZ * seedY ^ seed;
	}
	
	public Island getSpawnIsland() {
		Island spawnIsland = null;
		float idealSpawnIslandSize = 2.1f; // 2 better than 3 better than 1... 
		
		// Find island closest to ideal size
		int spawnIslandSize = -1;
		Region region = regionManager.getSpawnRegion();
		for (Island island : region.getIslands()) {
			int islandSize = island.getShape().getCells().size();
			if (spawnIslandSize == -1 || Math.abs(idealSpawnIslandSize-islandSize) < Math.abs(idealSpawnIslandSize-spawnIslandSize)) {
				spawnIslandSize = islandSize;
				spawnIsland = island;
			}
		}
		
		return spawnIsland;
	}
	
	public BlockPos getSafeWorldSpawnPos() {
		Island spawnIsland = getSpawnIsland();

		if (spawnIsland == null) {
			AeroGenMod.log.warn("Could not find a good spawn! All islands in start region failed.");
			return new BlockPos(0, 64, 0);
			
		}
		
		if (!spawnIsland.isInitialized()) spawnIsland.initialize();
		if (!spawnIsland.isGenerated()) spawnIsland.generate();
			
		BlockPos spawn = spawnIsland.findGoodSpawnLocation();
		
		AeroGenMod.log.info("Spawn Island: "+spawnIsland+" ("+spawnIsland.getBiome().getName()+")");
		AeroGenMod.log.info("Spawn block: "+spawn);
		
		return spawn;
	}
	
	/**
	 * Generate the biomes[] array for the given chunk. This is an array of 256 values
	 * representing each Biome in the 16x16 chunk tiles. The array is in Z-major order
	 * (therefore index = z*16 + x, or more accurately z << 4 | x)
	 */
	public void generateBiomes(byte[] biomes, int chunkX, int chunkZ) {		
        int chunkMinX = (chunkX << 4) + 8;
        int chunkMinZ = (chunkZ << 4) + 8;
        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
        
		List<Island> islands = new ArrayList<>();
		islandProvider.getIslands(islands, chunkBounds);

		final byte defaultBiome= (byte) Biome.getIdForBiome(Biomes.OCEAN);
		
		// Fill void biomes
		for (Int2D.Mutable block : chunkBounds.getAllMutable()) {
			int index = chunkBounds.indexFor(block);
			biomes[index] = defaultBiome;
			
			for (Island island : islands) {
				for (IslandCell cell : island.getCells()) {
					if (!cell.getPolygon().contains(block.x(), block.y())) continue;
					if (!island.isInitialized()) island.initialize();
					biomes[index] = (byte) Biome.getIdForBiome(island.getBiome().getMinecraftBiomeForSurroundingAir());
				}
			}
		}
		
		// Fill island biomes
		for (Island island : islands) {
			island.provideBiomes(biomes, chunkBounds);
		}
	}
	
	/**
	 * Generates the terrain blocks for the given chunk into the provided ChunkPrimer.
	 * The terrain blocks should contain the bulk of the island, with caves carved out,
	 * as well as rivers and most bodies of water carved and filled with water.
	 */
	public void generateTerrain(ChunkPrimer primer, int chunkX, int chunkZ) {
        int chunkMinX = (chunkX << 4);
        int chunkMinZ = (chunkZ << 4);
        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);

		// Fill the chunk with island blocks
		List<Island> islands = new ArrayList<>();
		islandProvider.getIslands(islands, chunkBounds);
		for (Island island : islands) {
			island.provideChunk(primer, chunkBounds);
		}
	}
	
	/**
	 * Populates features that simply place blocks that do not interact with each other. 
	 * This phase saves on lighting calculations because it does not do any lighting until
	 * the very end, which is VERY important for islands with high altitudes.<br /><br />
	 * 
	 * <b>Examples of things that SHOULD be pre-populated:</b><br />
	 * - Things with simple generators (not all features are supported by the WorldPrimer)<br />
	 * - Terrain Features such as trees, rocks, or fallen logs<br />
	 * - Dirt and clay patches, ores, granite, diorite, andesite<br />
	 * - Anything that won't be affected by things being placed nearby<br /><br />
	 * 
	 * <b>Examples of things that should NOT be pre-populated:</b><br />
	 * - Fences (They connect with each other and need to block-update their neighbors)<br />
	 * - Water/Lava/Liquids that need to flow. Blocks placed in the pre-populate phase do not update<br />
	 * - Redstone for obvious reasons<br />
	 * - When in doubt, put it in post-populate (the default)<br />
	 */
	public void prePopulate(World world, int chunkX, int chunkZ) {		
        int chunkMinX = (chunkX << 4) + 8;
        int chunkMinZ = (chunkZ << 4) + 8;
        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
		
		Random random = new Random();		
		List<Island> islands = new ArrayList<>();
		islandProvider.getIslands(islands, chunkBounds);
		for (Island island : islands) {
			random.setSeed(island.getProvider().getChunkSeed(chunkX, chunkZ));
			island.populateChunk(world, chunkBounds, random, PopulatePhase.PRE_POPULATE);
		}
	}
	
	/**
	 * Minecraft's standard populate phase. Every block placed in this phase can cause a lighting update.
	 * This is okay (maybe not) for normal Minecraft worlds, but the floating islands have a lot of air
	 * below them and lighting updates can be quite expensive.
	 */
	public void postPopulate(World world, int chunkX, int chunkZ) {		
        int chunkMinX = (chunkX << 4) + 8;
        int chunkMinZ = (chunkZ << 4) + 8;
        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);

		Random random = new Random();
		List<Island> islands = new ArrayList<>();
		islandProvider.getIslands(islands, chunkBounds);
		for (Island island : islands) {
			random.setSeed(island.getProvider().getChunkSeed(chunkX, chunkZ));
			island.populateChunk(world, chunkBounds, random, PopulatePhase.POST_POPULATE);
		}
		
		// Add snow to cold places and freeze water
        for (Int2D.Mutable block : chunkBounds.getAllMutable()) {
            BlockPos precipitationHeight = this.world.getPrecipitationHeight(new BlockPos(block.x(), 0, block.y()));
            BlockPos waterLevel = precipitationHeight.down();

            if (world.canBlockFreezeWater(waterLevel))  {
                world.setBlockState(waterLevel, Blocks.ICE.getDefaultState(), 2);
            }

            if (world.canSnowAt(precipitationHeight, true)) {
                world.setBlockState(precipitationHeight, Blocks.SNOW_LAYER.getDefaultState(), 2);
            }
        }
				
		// Pre-spawn some animals for this chunk
        BlockPos blockpos = new BlockPos(chunkMinX, 0, chunkMinZ);
        Biome biome = world.getBiome(blockpos.add(8, 0, 8));
		WorldEntitySpawner.performWorldGenSpawning(world, biome, chunkMinX, chunkMinZ, 16, 16, random);
	}

	/**
	 * Produces the actual Chunk object. This method combines the Biome[] array and ChunkPrimer
	 * created by the WorldPrimer into a Minecraft Chunk object, calls generateSkylightMap(), and 
	 * returns the Chunk object. The actual biome and block generation is carried out by
	 * generateBiomes(), generateTerrain(), and prePopulate(). postPopulate() will be called
	 * by Minecraft after the chunk has been transferred to the Minecraft World.
	 */
	public Chunk generateChunk(int chunkX, int chunkZ) {
		WorldPrimerChunk chunk = worldPrimer.getOrCreatePrimerChunk(chunkX, chunkZ);
		Tuple2<ChunkPrimer, byte[]> chunkData = chunk.getCompleted();
		ChunkPrimer primer = chunkData.first;
		byte[] biomes = chunkData.second;
		
		Chunk mcChunk = new Chunk(world, primer, chunkX, chunkZ);

		// Biomes
		System.arraycopy(biomes, 0, mcChunk.getBiomeArray(), 0, 256);

		// Lighting
		mcChunk.generateSkylightMap();
		
		return mcChunk;
	}

	/**
	 * This method is called from AeroGenMod when the world 
	 * associated with this AeroGenerator is asked for a spawn location.
	 */
	public void onCreateWorldSpawn(CreateSpawnPosition event) {
		if (!initialized) this.initialize();
		
    	final World world = event.getWorld();
    	AeroGenMod.log.info("Creating spawn point for world \""+world.getWorldInfo().getWorldName()+"\"...");
		
    	BlockPos spawn = this.getSafeWorldSpawnPos();
    	if (spawn != null) {
    		world.setSpawnPoint(spawn);
    		event.setCanceled(true);
    	}
	}
	
	/**
	 * This method is called from AeroGenMod when the world 
	 * associated with this AeroGenerator is loaded.
	 */
	public void onWorldLoad(WorldEvent.Load event) {
		if (!initialized) this.initialize();
		
		AeroGenMod.log.info("Loading world \""+world.getWorldInfo().getWorldName()+"\"...");
	}

	/**
	 * This method is called from AeroGenMod when the world 
	 * associated with this AeroGenerator is saved.
	 */
	public void onWorldSave(Save event) {
		this.worldPrimer.saveAll();
	}
	
	/**
	 * This method is called from AeroGenMod when the world 
	 * associated with this AeroGenerator is unloaded.
	 */
	public void onWorldUnload(Unload event) {
		this.worldPrimer.saveAll();
		this.worldPrimer.flush();
	}

	/**
	 * This method is called from AeroGenMod when the world
	 * associated with this AeroGenerator ticks.
	 */
	public void onWorldTick(WorldTickEvent event) {
		this.worldPrimer.doSaveTick();
	}

	/**
	 * This method is called from AeroGenMod when the server
	 * is shutting down.
	 */
	public void shutdown() {
		setGUIEnabled(false);
		worldPrimer.close();
		worldPrimer = null;
		
		generators.remove(this.world);
		
		if (generators.size() == 0) {
			taskManager.shutdown();
			
			boolean terminated = false;
			try {
				terminated = taskManager.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!terminated) taskManager.shutdownNow();
			
			taskManager = null;
		}
	}

}
