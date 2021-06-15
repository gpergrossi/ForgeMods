package com.gpergrossi.aerogen.generator.primer;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import com.gpergrossi.aerogen.AeroGenMod;
import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.util.data.Tuple2;
import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.util.spacial.Int2DSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldPrimer extends World {

	private static final WorldProvider NULL_WORLD_PROVIDER = new WorldProvider() {
		@Override
		public DimensionType getDimensionType() { return null; }
		@Override
		public net.minecraft.world.border.WorldBorder createWorldBorder() { return null; };
	};
	
	/**
	 * Lowest value (earliest) timestamp first
	 */
	private static final Comparator<Tuple2<Long, WorldPrimerChunk>> CHUNK_AGE_COMPARATOR = (a, b) -> Long.compare(a.first, b.first);

	/**
	 * If set to true, the console will be positively SPAMMED with information about
	 * how chunks were loaded/constructed and then generated/populated/finished.
	 */
	public static final boolean DEBUG_PRINT_CHUNK_LOG = false;

	/**
	 * Minimum time in milliseconds after the last time a chunk was modified before it is saved
	 */
	private static final long MIN_SAVE_AGE = 10000;
	
	protected final AeroGenerator generator;
	protected final WorldPrimerChunkLoader chunkLoader;

	protected final ConcurrentMap<Int2D, WorldPrimerChunk> chunks;
	protected final PriorityBlockingQueue<Tuple2<Long, WorldPrimerChunk>> saveQueue;
	
	/**
	 * A set of all chunks that have been finished and dumped (transferred to minecraft)
	 */
	protected final Int2DSet dumpedChunks;
	
	public WorldPrimer(AeroGenerator generator) {
		super(null, null, NULL_WORLD_PROVIDER, null, false);
		this.generator = generator;
		
		this.chunkLoader = new WorldPrimerChunkLoader(this);
		
		final int numThreads = Math.max(AeroGenMod.numThreads, 1);
		this.chunks = new MapMaker().concurrencyLevel(numThreads).initialCapacity(128).makeMap();
		this.saveQueue = new PriorityBlockingQueue<>(128, CHUNK_AGE_COMPARATOR);
		
		this.dumpedChunks = new Int2DSet(numThreads);
	}
	
	private boolean isChunkFinished(final int chunkX, final int chunkZ) {
		if (dumpedChunks.contains(chunkX, chunkZ)) return true;
		
		if (generator.getWorld().isChunkGeneratedAt(chunkX, chunkZ)) {
			dumpedChunks.add(chunkX, chunkZ);
			return true;
		}
		
		return false;
	}
	
	private WorldPrimerChunk getPrimerChunkInternal(int chunkX, int chunkZ, boolean canLoad, boolean allowProxy) {
		final Int2D coord = new Int2D(chunkX, chunkZ);
		
		WorldPrimerChunk chunk = chunks.get(coord);
		
		if (chunk == null && canLoad && chunkLoader.hasChunk(chunkX, chunkZ)) {
			WorldPrimerChunk toLoad = WorldPrimerChunk.createNeedsLoad(this, chunkX, chunkZ);
			chunks.put(coord, toLoad);
			return toLoad;
		}
				
		if (chunk == null && allowProxy && isChunkFinished(chunkX, chunkZ)) {
			WorldPrimerChunk proxy = WorldPrimerChunk.createProxy(this, chunkX, chunkZ);
			return proxy;
		}
		
		return chunk;
	}

	public void doSaveTick() {
		if (saveQueue.isEmpty()) return;
		
		final long now = System.currentTimeMillis();		
		final Tuple2<Long, WorldPrimerChunk> saveEntry = saveQueue.peek();
		final long timestamp = saveEntry.first;
		
		if (now - timestamp < MIN_SAVE_AGE) return;
		
		saveQueue.poll(); // Remove save entry
		final WorldPrimerChunk chunk = saveEntry.second;
		
		if (chunk.needsSave()) {
			if (now - chunk.modifyTimestamp < MIN_SAVE_AGE) {
				saveQueue.offer(new Tuple2<>(chunk.modifyTimestamp, chunk));
				return;
			}
			save(chunk);
		}
		chunk.inSaveQueue = false;
	}
	
	public void save(WorldPrimerChunk chunk) {
		chunk.save();
	}
	
	/**
	 * Dumps a chunks data. Called by the save task if chunk isDumped upon completion.
	 * @param chunk
	 */
	public void dump(WorldPrimerChunk chunk) {
		dumpedChunks.add(chunk.chunkCoord);
		chunks.remove(chunk.chunkCoord);
	}
	
	public void saveAll() {
		for (WorldPrimerChunk chunk : chunks.values()) {
			if (chunk.needsSave() && chunk.isCompleted()) save(chunk);
		}
		for (WorldPrimerChunk chunk : chunks.values()) {
			if (chunk.needsSave() && !chunk.isCompleted()) save(chunk);
		}
	}
	
	public void flush() {
		generator.getTaskManager().getIOHandler().flush();
	}

	public void close() {
		chunkLoader.close();
	}
	
	public WorldPrimerChunk peakPrimerChunk(int chunkX, int chunkZ) {
		synchronized (chunks) {
			return getPrimerChunkInternal(chunkX, chunkZ, false, false);
		}
	}
	
	public WorldPrimerChunk getPrimerChunk(int chunkX, int chunkZ) {
		synchronized (chunks) {
			return getPrimerChunkInternal(chunkX, chunkZ, true, true);
		}
	}
	
	public WorldPrimerChunk getOrCreatePrimerChunk(int chunkX, int chunkZ) {
		final Int2D coord = new Int2D(chunkX, chunkZ);
		
		synchronized (chunks) {
			WorldPrimerChunk chunk = getPrimerChunkInternal(chunkX, chunkZ, true, true);
			if (chunk == null) {
				chunk = new WorldPrimerChunk(this, chunkX, chunkZ);
				chunks.put(coord, chunk);
			}
			return chunk;
		}
	}
	
	public WorldPrimerChunk getOrCreatePrimerChunkForBlockPos(BlockPos pos) {
		int chunkX = (pos.getX() >> 4);
		int chunkZ = (pos.getZ() >> 4);
		return getOrCreatePrimerChunk(chunkX, chunkZ);
	}
	
	public AeroGenerator getGenerator() {
		return generator;
	}

	public World getMinecraftWorld() {
		return generator.getWorld();
	}

	public Iterator<WorldPrimerChunk> getChunks() {
		return chunks.values().iterator();
	}
	
	public void getBiomeInts(Int2DRange.Integers returnIntsRange) {		
		int minChunkX = (returnIntsRange.minX >> 4);
		int minChunkZ = (returnIntsRange.minY >> 4);
		int maxChunkX = (returnIntsRange.maxX >> 4);
		int maxChunkZ = (returnIntsRange.maxY >> 4);
		
		int[] data = returnIntsRange.data;
		
		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			int chunkMinX = (chunkX << 4);
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				int chunkMinZ = (chunkZ << 4);
				WorldPrimerChunk chunk = getOrCreatePrimerChunk(chunkX, chunkZ);
				
				Int2DRange chunkRange = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
				Int2DRange overlap = chunkRange.intersect(returnIntsRange);
				
				byte[] chunkBiomes = chunk.getBiomes();
				for (Int2D.Mutable tile : overlap.getAllMutable()) {
					data[returnIntsRange.indexFor(tile)] = chunkBiomes[tile.x() & 15 | ((tile.y() & 15) << 4)];
				}
			}
		}
	}
	
	
	
	
	
	
	@Override
	protected IChunkProvider createChunkProvider() {
		return null;
	}

	@Override
	protected boolean isChunkLoaded(int chunkX, int chunkZ, boolean allowEmpty) {
		synchronized (chunks) {
			return getPrimerChunkInternal(chunkX, chunkZ, false, true) != null;
		}
	}
	
	@Override
    public Biome getBiome(final BlockPos pos) {
		return Biome.getBiomeForId(getOrCreatePrimerChunkForBlockPos(pos).getBiome(pos.getX() & 15, pos.getZ() & 15));
    }
	
	@Override
	public IBlockState getBlockState(BlockPos pos) {
		WorldPrimerChunk chunk = getPrimerChunk(pos.getX() >> 4, pos.getZ() >> 4);
		if (chunk == null) {
			throw new IndexOutOfBoundsException("Trying to getBlockState in a WorldPrimerChunk that has not yet been created");
		}
		
		return chunk.getBlockState(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
	}
	
	@Override
	public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
		WorldPrimerChunk chunk = getPrimerChunk(pos.getX() >> 4, pos.getZ() >> 4);
		if (chunk == null) {
			throw new IndexOutOfBoundsException("Trying to setBlockState in a WorldPrimerChunk that has not yet been created");
		}
		
		chunk.setBlockState(pos.getX() & 15, pos.getY(), pos.getZ() & 15, newState);
		return true;
	}
	
	@Override
	public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
		WorldPrimerChunk chunk = getOrCreatePrimerChunkForBlockPos(pos);

		int i = (pos.getX() & 15);
		int k = (pos.getZ() & 15);

		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
		
        for (int j = chunk.getHeight(i, k); j >= 0; j--) {
            IBlockState state = chunk.getBlockState(i, j, k);
            
            mutablePos.setY(j);
            if (!state.getMaterial().blocksMovement()) continue;
            if (state.getBlock().isLeaves(state, this, mutablePos)) continue;
            if (state.getBlock().isFoliage(this, mutablePos)) continue;
            
            break;
        }

        return mutablePos.toImmutable();
	}
	
	@Override
	public int getHeight() {
		return generator.getWorld().getHeight();
	}
	
	@Override
	public int getActualHeight() {
		return generator.getWorld().getActualHeight();
	}
	
	@Override
	public int getHeight(int x, int z) {
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000) return 0;
        if (generator.getWorld().isChunkGeneratedAt(x >> 4, z >> 4)) {
        	return generator.getWorld().getChunkFromChunkCoords(x >> 4, z >> 4).getHeightValue(x & 15, z & 15);
        }
        if (!this.isChunkLoaded(x >> 4, z >> 4, true)) return 0;
        return getPrimerChunk(x >> 4, z >> 4).getHeight(x & 15, z & 15);
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return super.isAirBlock(pos);
	}
	
	@Override
	public boolean setBlockToAir(BlockPos pos) {
		return super.setBlockToAir(pos);
	}
	
	@Override
	public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean canSeeSky(BlockPos pos) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public BlockPos getPrecipitationHeight(BlockPos pos) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdj) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean canSnowAt(BlockPos pos, boolean checkLight) {
		throw new UnsupportedOperationException();
	}
	
}
