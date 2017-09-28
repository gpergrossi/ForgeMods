package com.gpergrossi.aerogen.generator.data;

import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.util.data.Large2DArray;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;

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
	
	public final AeroGenerator generator;
	public final Large2DArray<WorldPrimerChunk> chunks;
	
	public WorldPrimer(AeroGenerator generator) {
		super(null, null, NULL_WORLD_PROVIDER, null, false);
		this.generator = generator;
		this.chunks = new Large2DArray<>(t -> new WorldPrimerChunk[t]);
	}

	public WorldPrimerChunk getPrimerChunk(int chunkX, int chunkZ) {
		synchronized (chunks) {
			return chunks.get(chunkX, chunkZ);
		}
	}
	
	public WorldPrimerChunk getOrCreatePrimerChunk(int chunkX, int chunkZ) {
		synchronized (chunks) {
			WorldPrimerChunk chunk = chunks.get(chunkX, chunkZ);
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
				for (Int2D.Mutable tile : overlap.getAllMutable()) {
					data[returnIntsRange.indexFor(tile)] = Biome.getIdForBiome(chunk.getBiome(tile.x(), tile.y()));
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
			WorldPrimerChunk chunk = chunks.get(chunkX, chunkZ);
			return (chunk != null);
		}
	}
	
	@Override
    public Biome getBiome(final BlockPos pos) {
		return getOrCreatePrimerChunkForBlockPos(pos).getBiome(pos.getX() & 15, pos.getZ() & 15);
    }
	
	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return getOrCreatePrimerChunkForBlockPos(pos).getBlockState(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
	}
	
	@Override
	public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
		getOrCreatePrimerChunkForBlockPos(pos).setBlockState(pos.getX() & 15, pos.getY(), pos.getZ() & 15, newState);
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
        if (!this.isChunkLoaded(x >> 4, z >> 4, true)) return 0;
        return getOrCreatePrimerChunk(x >> 4, z >> 4).getHeight(x & 15, z & 15);
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
