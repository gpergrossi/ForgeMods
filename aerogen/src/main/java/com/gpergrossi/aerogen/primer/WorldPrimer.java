package com.gpergrossi.aerogen.primer;

import java.util.Iterator;

import com.gpergrossi.aerogen.AeroGenerator;
import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.util.spacial.Large2DArray;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldPrimer extends World {

	private static final WorldProvider NULL_WORLD_PROVIDER = new WorldProvider() {
		public DimensionType getDimensionType() { return null; }
		public net.minecraft.world.border.WorldBorder createWorldBorder() { return null; };
	};
	
	protected final AeroGenerator generator;
	protected final Large2DArray<WorldPrimerChunk> chunks;
	protected final WorldPrimerChunkLoader chunkStore;
	
	public WorldPrimer(AeroGenerator generator) {
		super(null, null, NULL_WORLD_PROVIDER, null, false);
		this.generator = generator;
		this.chunks = new Large2DArray<>();
		
		this.chunkStore = new WorldPrimerChunkLoader(this);
	}
	
	private WorldPrimerChunk getPrimerChunkInternal(int chunkX, int chunkZ, boolean canLoad, boolean allowProxy) {
		WorldPrimerChunk chunk = chunks.get(chunkX, chunkZ);
		
		if (chunk == null && canLoad && chunkStore.hasChunk(chunkX, chunkZ)) {
			chunk = chunkStore.loadChunk(chunkX, chunkZ);
			chunks.set(chunkX, chunkZ, chunk);
		}
				
		if (chunk == null && allowProxy && generator.getWorld().isChunkGeneratedAt(chunkX, chunkZ)) {
			WorldPrimerChunk proxy = WorldPrimerChunk.proxy(this, chunkX, chunkZ);
			chunks.set(chunkX, chunkZ, proxy);
			return proxy;
		}
		
		return chunk;
	}

	public void save(WorldPrimerChunk chunk) {
		chunkStore.saveChunk(chunk);
	}
	
	public void saveAll() {
		for (WorldPrimerChunk chunk : chunks) {
			if (!chunk.isSaved() && chunk.isCompleted()) chunkStore.saveChunk(chunk);
		}
		for (WorldPrimerChunk chunk : chunks) {
			if (!chunk.isSaved() && !chunk.isCompleted()) chunkStore.saveChunk(chunk);
		}
	}
	
	public void flush() {
		chunkStore.flush();
	}

	public void close() {
		chunkStore.close();
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
		synchronized (chunks) {
			WorldPrimerChunk chunk = getPrimerChunkInternal(chunkX, chunkZ, true, true);
			if (chunk == null) {
				chunk = new WorldPrimerChunk(this, chunkX, chunkZ);
				chunks.set(chunkX, chunkZ, chunk);
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
		return chunks.iterator();
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
