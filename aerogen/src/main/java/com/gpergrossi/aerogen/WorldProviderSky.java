package com.gpergrossi.aerogen;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderSky extends WorldProvider {
	
	public WorldProviderSky() {
		super();
	}
	
	@Override
	protected void init() {}
	
	@Override
	public DimensionType getDimensionType() {
    	return AeroGenMod.SKY_DIMENSION_TYPE;
	}

	public int getDimensionID() {
		return AeroGenMod.SKY_DIMENSION_ID;
	}

    /**
     * the y level at which clouds are rendered.
     */
    @Override
	@SideOnly(Side.CLIENT)
    public float getCloudHeight() {
        return AeroGenMod.DIMENSION_TYPE_SKY.getCloudHeight();
    }
    
    @Override
	public int getAverageGroundLevel() {
        return AeroGenMod.DIMENSION_TYPE_SKY.getMinimumSpawnHeight(this.world);
    }
    
    @Override
	public double getHorizon() {
        return AeroGenMod.DIMENSION_TYPE_SKY.getHorizon(world);
    }
    
    /**
     * Returns a double value representing the Y value relative to the top of the map at which void fog is at its
     * maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at
     * (256*0.03125), or 8.
     */
    @Override
	@SideOnly(Side.CLIENT)
    public double getVoidFogYFactor() {
        return AeroGenMod.DIMENSION_TYPE_SKY.voidFadeMagnitude();
    }
	
}
