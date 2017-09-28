package com.gpergrossi.aerogen.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandCell;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.RegionManager;
import com.gpergrossi.util.data.Tuple2;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Double2D;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.viewframe.AeroGeneratorView;
import com.gpergrossi.viewframe.ViewerFrame;

import jline.internal.Log;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorSettings;

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
	private ChunkGeneratorSettings mcGenSettings;
	
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

	public ChunkGeneratorSettings getMinecraftGeneratorSettings() {
		return this.mcGenSettings;
	}
	
	public RegionManager getRegionManager() {
		return regionManager;
	}
	
	public void setChunkGeneratorSettings(ChunkGeneratorSettings mcGenSettings) {
		this.mcGenSettings = mcGenSettings;
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
	
	public Island getIslandForBlockPos(BlockPos blockPos) {
        Int2DRange searchBounds = new Int2DRange(blockPos.getX(), blockPos.getZ(), blockPos.getX(), blockPos.getZ());
        List<Region> regions = new ArrayList<>();
		regionManager.getAll(regions, searchBounds);
		
		Double2D checkPos = new Double2D(blockPos.getX(), blockPos.getZ());
		for (Region region : regions) {
			for (IslandCell cell : region.getCells()) {
				if (cell.getPolygon().contains(checkPos)) return cell.getIsland();
			}
		}

		Log.warn("Could not find an island at ("+blockPos.getX()+", "+blockPos.getZ()+")");
		return null;
	}

	public boolean createWorldSpawn() {
		Log.info("Creating spawn point for "+world.getWorldInfo().getWorldName()+"...");
		
		Set<Island> dontUse = new HashSet<>();
		BlockPos spawn = null;
		Island spawnIsland = null;
		
		do {
			float idealSpawnIslandSize = 2.1f; // 2 better than 3 better than 1... 
			
			// Find island closest to ideal size
			int spawnIslandSize = -1;
			for (Island island : regionManager.getSpawnRegion().getIslands()) {
				if (dontUse.contains(island)) continue;
				int islandSize = island.getShape().getCells().size();
				if (spawnIslandSize == -1 || Math.abs(idealSpawnIslandSize-islandSize) < Math.abs(idealSpawnIslandSize-spawnIslandSize)) {
					spawnIslandSize = islandSize;
					spawnIsland = island;
				}
			}
			
			if (spawnIsland == null) {
				Log.warn("Could not find a good spawn! All islands in start region failed.");
				return false;
			}
			
			spawn = spawnIsland.findGoodSpawnLocation();
			
			if (spawn == null) {
				dontUse.add(spawnIsland);
				spawnIsland = null;
			}
		} while (spawn == null);
		
		Log.info("Spawn Island: "+spawnIsland+" ("+spawnIsland.getBiome().getName()+")");
		Log.info("Spawn block: "+spawn);
		
		world.setSpawnPoint(spawn);
		return true;
	}
	
	/**
	 * Generate the biomes[] array for the given chunk. This is an array of 256 values
	 * representing each Biome in the 16x16 chunk tiles. The array is in Z-major order
	 * (therefore index = z*16 + x, or more accurately z << 4 | x)
	 */
	public void generateBiomes(Biome[] biomes, int chunkX, int chunkZ) {
        int chunkMinX = (chunkX << 4) + 8;
        int chunkMinZ = (chunkZ << 4) + 8;
        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
        
		List<Region> regions = new ArrayList<>();
		regionManager.getAll(regions, chunkBounds);

		// Fill void biomes
		for (Int2D.Mutable block : chunkBounds.getAllMutable()) {
			int index = chunkBounds.indexFor(block);
			biomes[index] = Biomes.SKY;
					
			for (Region region : regions) {
				if (!region.getRegionPolygon().contains(block.x(), block.y())) continue;
				for (IslandCell cell : region.getCells()) {
					if (!cell.getPolygon().contains(block.x(), block.y())) continue;
					biomes[index] = cell.getIsland().getBiome().getMinecraftBiomeForSurroundingAir();
				}
			}
		}
		
		// Fill island biomes
		for (Region region : regions) {
			for (Island island : region.getIslands()) {
				island.provideBiomes(biomes, chunkBounds);
			}
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
		List<Region> regions = new ArrayList<>();
		regionManager.getAll(regions, chunkBounds);
		
		for (Region region : regions) {
			for (Island island : region.getIslands()) {
				island.provideChunk(primer, chunkBounds);
			}
		}
	}
	
	/**
	 * Populates features that simply place blocks that do not interact with each other. 
	 * This phase saves on lighting calculations because it does not do any lighting until
	 * the very end. <br /><br />
	 * 
	 * <b>Examples of things that SHOULD be pre-populated:</b><br />
	 * - Things with simple generators (not all features are supported by the WorldPrimer)<br />
	 * - Terrain Features such as rocks, fallen logs, trees<br />
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

		List<Region> regions = new ArrayList<>();
		regionManager.getAll(regions, chunkBounds);
		
		long seed = getChunkSeed(chunkX, chunkZ);
		Random random = new Random();
		
		for (Region region : regions) {
			for (Island island : region.getIslands()) {
				random.setSeed(seed);
				island.populateChunk(world, chunkBounds, random, GenerationPhase.PRE_POPULATE);
			}
		}
	}
	
	/**
	 * Minecraft's standard populate phase. Every block placed in this phase can cause a lighting update.
	 * This is okay (maybe not) for normal MineCraft worlds, but the floating islands have a lot of air
	 * below them and lighting updates can be quite expensive.
	 */
	public void postPopulate(World world, int chunkX, int chunkZ) {
        int chunkMinX = (chunkX << 4) + 8;
        int chunkMinZ = (chunkZ << 4) + 8;
        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
        
		List<Region> regions = new ArrayList<>();
		regionManager.getAll(regions, chunkBounds);
		
		long seed = getChunkSeed(chunkX, chunkZ);
		Random random = new Random(seed);
		
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
	 * created by the WorldPrimer into a MineCraft Chunk object, calls generateSkylightMap(), and 
	 * returns the Chunk object. The actual biome and block generation is carried out by
	 * generateBiomes(), generateTerrain(), and prePopulate().
	 */
	public Chunk generateChunk(int chunkX, int chunkZ) {
		WorldPrimerChunk chunk = worldPrimer.getOrCreatePrimerChunk(chunkX, chunkZ);
		Tuple2<ChunkPrimer, Biome[]> chunkData = chunk.getCompleted();
		ChunkPrimer primer = chunkData.first;
		Biome[] biomes = chunkData.second;
		
		Chunk mcChunk = new Chunk(world, primer, chunkX, chunkZ);

		// Biomes
		byte[] chunkBiomes = mcChunk.getBiomeArray();
		for (int i = 0; i < 256; i++) {
			chunkBiomes[i] = (byte) Biome.getIdForBiome(biomes[i]);
		}

		// Lighting
		mcChunk.generateSkylightMap();
		
		return mcChunk;
	}

}
