package dev.mortus.aerogen.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderSky extends WorldProvider {

	private static int SKY_DIMENSION_ID;
	private static DimensionType SKY_DIMENSION_TYPE;
	
	static {
		SKY_DIMENSION_ID = DimensionManager.getNextFreeDimId();
		SKY_DIMENSION_TYPE = DimensionType.register("The Sky", "_sky", SKY_DIMENSION_ID, WorldProviderSky.class, false); 
	}
	
	WorldTypeSky worldType;
	
	public WorldProviderSky() {
		super();
		worldType = new WorldTypeSky();
	}
	
	@Override
	public DimensionType getDimensionType() {    	
    	return SKY_DIMENSION_TYPE;
	}

	public int getDimensionID() {
		return SKY_DIMENSION_ID;
	}

	@Override
	public String getWelcomeMessage() {
		return "Welcome to the Sky!";
	}
	
    public IChunkGenerator createChunkGenerator() {
        return worldType.getChunkGenerator(world, world.getWorldInfo().getGeneratorOptions());
    }

    /**
     * the y level at which clouds are rendered.
     */
    @SideOnly(Side.CLIENT)
    public float getCloudHeight() {
        return worldType.getCloudHeight();
    }
    
    public int getAverageGroundLevel() {
        return worldType.getMinimumSpawnHeight(this.world);
    }
    
    public double getHorizon() {
        return worldType.getHorizon(world);
    }
    
    /**
     * Returns a double value representing the Y value relative to the top of the map at which void fog is at its
     * maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at
     * (256*0.03125), or 8.
     */
    @SideOnly(Side.CLIENT)
    public double getVoidFogYFactor() {
        return worldType.voidFadeMagnitude();
    }
	
}
