package dev.mortus.aerogen.world.islands.biomes;

import dev.mortus.aerogen.world.islands.IslandDecorator;

public class IslandBiomeVoid extends IslandBiome {
	
	private static BiomeProperties getBiomeProperties() {
		BiomeProperties properties = new BiomeProperties("Void");
		properties.setWaterColor(0x0077FF);
		return properties;
	}
	
	public IslandBiomeVoid() {
		super(getBiomeProperties());
	}
	
	@Override
	public boolean isVoid() {
		return true;
	}

	@Override
	protected IslandDecorator createDecorator() {
		return null;
	}
	
}