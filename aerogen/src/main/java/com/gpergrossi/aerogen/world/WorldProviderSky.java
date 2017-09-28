package com.gpergrossi.aerogen.world;

import com.gpergrossi.aerogen.AeroGenMod;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderSky extends WorldProvider {
	
	public WorldProviderSky() {
		super();
	}
	
	@Override
	public DimensionType getDimensionType() {    	
    	return AeroGenMod.SKY_DIMENSION_TYPE;
	}

	public int getDimensionID() {
		return AeroGenMod.SKY_DIMENSION_ID;
	}

    public IChunkGenerator createChunkGenerator() {
        return AeroGenMod.WORLD_TYPE_SKY.getChunkGenerator(world, world.getWorldInfo().getGeneratorOptions());
    }

    /**
     * the y level at which clouds are rendered.
     */
    @SideOnly(Side.CLIENT)
    public float getCloudHeight() {
        return AeroGenMod.WORLD_TYPE_SKY.getCloudHeight();
    }
    
    public int getAverageGroundLevel() {
        return AeroGenMod.WORLD_TYPE_SKY.getMinimumSpawnHeight(this.world);
    }
    
    public double getHorizon() {
        return AeroGenMod.WORLD_TYPE_SKY.getHorizon(world);
    }
    
    /**
     * Returns a double value representing the Y value relative to the top of the map at which void fog is at its
     * maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at
     * (256*0.03125), or 8.
     */
    @SideOnly(Side.CLIENT)
    public double getVoidFogYFactor() {
        return AeroGenMod.WORLD_TYPE_SKY.voidFadeMagnitude();
    }
	
}
