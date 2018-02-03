package com.gpergrossi.aerogen.generator;

import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

public class AeroGeneratorSettings {

	AeroGenerator generator;

	public boolean genStructures;
	
	public long seed = 875849390123L;
	public double regionGridSize = 512;
	public double islandCellBaseSize = 64;
	
	public AeroGeneratorSettings(AeroGenerator generator) {
		this.generator = generator;
	}

	/**
	 * Attempt to load settings from world AeroGenSettings file
	 */
	public void load() {
		World world = generator.getWorld();
		seed = world.getSeed();
		
		WorldInfo worldInfo = world.getWorldInfo();
		genStructures  = worldInfo.isMapFeaturesEnabled();
		
		String settingsJSON = worldInfo.getGeneratorOptions();
		System.out.println("Aerogen settings JSON: "+settingsJSON);
	}
	
}
