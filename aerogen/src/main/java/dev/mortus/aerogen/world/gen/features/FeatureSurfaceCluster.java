package dev.mortus.aerogen.world.gen.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.mortus.aerogen.world.gen.placeables.IPlaceable;
import dev.mortus.aerogen.world.islands.Island;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FeatureSurfaceCluster extends AbstractFeature {
	
	protected int clusterRadius = 8;
	protected int clusterHeight = 4;
	protected int clusterDensity = 128;

	protected List<IPlaceable> placeables;
	protected List<Float> placeableWeights;
	protected float totalWeights;
	
    protected List<IBlockState> growableOn;
    
    public FeatureSurfaceCluster() {
    	this.placeables = new ArrayList<>();
    	this.placeableWeights = new ArrayList<>();
    }
    
	public FeatureSurfaceCluster addPlacable(float weight, IPlaceable placeable) {
		if (weight <= 0) throw new IllegalArgumentException();
		this.placeables.add(placeable);
		this.placeableWeights.add(weight);
		this.totalWeights += weight;
		return this;
	}
    
    public FeatureSurfaceCluster withCluster(int radius, int height, int density) {
    	this.clusterRadius = radius;
    	this.clusterHeight = height;
    	this.clusterDensity = density;
		return this;
    }
	
    public FeatureSurfaceCluster withClusterRadius(int radius) {
    	this.clusterRadius = radius;
		return this;
    }
    
    public FeatureSurfaceCluster withClusterHeight(int height) {
    	this.clusterHeight = height;
		return this;
    }
    
    /**
     * The number of attempted block placements in an area of <pre>
     * x from -radius to radius,
     * y from -radius/2 to radius/2,
     * z from -radius to radius, </pre> 
     * with all positions waited to the center via random(radius) - random(radius)<br><br>
     */
    public FeatureSurfaceCluster withClusterDensity(int density) {
    	this.clusterDensity = density;
		return this;
    }
    
    /**
     * Allows the cluster blocks to be spawned on this type of block.
     * If this method is never called, there are no restriction at all.
     */
    public FeatureSurfaceCluster allowPlacementOn(IBlockState blockstate) {
    	if (growableOn == null) growableOn = new ArrayList<>();
    	growableOn.add(blockstate);
		return this;
    }
    
	protected IPlaceable getRandomPlaceable(Random rand) {
		if (placeables.size() == 1) return placeables.get(0);
		if (totalWeights == 0f) return null;
		float roll = rand.nextFloat()*totalWeights;
		for (int i = 0; i < placeables.size(); i++) {
			roll -= placeableWeights.get(i);
			if (roll <= 0) return placeables.get(i);
		}
		return null;
	}
	
	@Override
	public boolean generate(World world, Island island, BlockPos position, Random rand) {
		boolean anyPlaced = false;
		
		// Find the ground
        for (; position.getY() > 0; position = position.down()) {
        	IBlockState iblockstate = world.getBlockState(position);
        	boolean isAir = iblockstate.getBlock().isAir(iblockstate, world, position);
        	boolean isLeaves = iblockstate.getBlock().isLeaves(iblockstate, world, position);
        	if (!isAir && !isLeaves) break;
        }
        position = position.up();

        // Place the cluster
        for (int i = 0; i < clusterDensity; i++) {
        	
        	// Individual positioning
        	int x = 0, y = 0, z = 0;
        	if (clusterRadius > 0) {
        		x = rand.nextInt(clusterRadius) - rand.nextInt(clusterRadius);
        		z = rand.nextInt(clusterRadius) - rand.nextInt(clusterRadius);
        	}
        	if (clusterHeight > 0) {
        		y = rand.nextInt(clusterHeight) - rand.nextInt(clusterHeight);
        	}
            BlockPos blockpos = position.add(x, y, z);
            
            // Fail conditions
            if (!world.isAirBlock(blockpos)) continue;
            if (!canGrowOn(world.getBlockState(blockpos.down()))) continue;
            
            // Block placement and conditions by type
            IPlaceable placeable = getRandomPlaceable(rand);
            anyPlaced |= placeable.place(world, blockpos, rand);
        }
        
        return anyPlaced;
	}

	private boolean canGrowOn(IBlockState blockstate) {
		if (growableOn == null) return true;
		return growableOn.contains(blockstate);
	}

}
