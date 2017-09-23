package com.gpergrossi.aerogen.generator;

public class AeroGeneratorSettings {


	AeroGenerator generator;

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
		// TODO Auto-generated method stub
		
	}

	
	
}
