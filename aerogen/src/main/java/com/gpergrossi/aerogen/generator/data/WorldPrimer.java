package com.gpergrossi.aerogen.generator.data;

import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.util.data.Large2DArray;

import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldPrimer extends World {

	private static final WorldProvider NULL_WORLD_PROVIDER = new WorldProvider() {
		@Override
		public DimensionType getDimensionType() {
			return null;
		}
		
		public net.minecraft.world.border.WorldBorder createWorldBorder() {
			return null;
		};
	};
	
	AeroGenerator generator;
	Large2DArray<WorldPrimerChunk> chunks;
	
	public WorldPrimer(AeroGenerator generator) {
		super(null, null, NULL_WORLD_PROVIDER, null, false);
		this.generator = generator;
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
}
