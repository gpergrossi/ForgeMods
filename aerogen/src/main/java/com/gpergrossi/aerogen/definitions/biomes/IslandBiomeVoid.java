package com.gpergrossi.aerogen.definitions.biomes;

import java.util.ArrayList;
import java.util.List;

import com.gpergrossi.aerogen.AeroGenMod;
import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;

public class IslandBiomeVoid extends IslandBiome {
	
	private static BiomeProperties getBiomeProperties() {
		BiomeProperties properties = new BiomeProperties("Void");
		properties.setWaterColor(0x0077FF);
		return properties;
	}
	
	public IslandBiomeVoid(BiomeProperties properties) {
		super(properties);
	}
	
	public IslandBiomeVoid() {
		this(getBiomeProperties());
		this.setRegistryName(AeroGenMod.MODID, "biome/sky_void");
	}
	
	@Override
	public boolean isVoid() {
		return true;
	}

	@Override
	protected IslandDecorator createDecorator() {
		return null;
	}
	
	@Override
	protected List<SpawnListEntry> getCreatureList() {
		return new ArrayList<>();
	}
	
	@Override
	protected List<SpawnListEntry> getCaveCreatureList() {
		return new ArrayList<>();
	}
	
	@Override
	protected List<SpawnListEntry> getMonsterList() {
		return new ArrayList<>();
	}
	
	@Override
	protected List<SpawnListEntry> getWaterCreatureList() {
		return new ArrayList<>();
	}
	
}