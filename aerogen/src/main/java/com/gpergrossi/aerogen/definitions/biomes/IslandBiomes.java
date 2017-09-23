package com.gpergrossi.aerogen.definitions.biomes;

import jline.internal.Log;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class IslandBiomes {

	@SubscribeEvent
	public static void registerBiomes(RegistryEvent.Register<Biome> event) {
		IForgeRegistry<Biome> registry = event.getRegistry();
		registry.registerAll(VOID, FOREST, FOREST_CLEARING, FOREST_BIRCH, DESERT, DESERT_DUNES, JUNGLE, MESA, SAVANNAH, TUNDRA, ROOFED_FOREST);
	}


	public static boolean isVoid(int id) {
		Biome biome = Biome.getBiomeForId(id);
		if (!(biome instanceof IslandBiome)) {
			Log.warn("Invalid island biome!");
			return true;
		}
		
		IslandBiome islandBiome = (IslandBiome) biome;
		return islandBiome.isVoid();
	}
	
	public static final IslandBiome VOID = new IslandBiomeVoid();
	public static final IslandBiome FOREST = new IslandBiomeForest();
	public static final IslandBiome FOREST_BIRCH = new IslandBiomeForestBirch();
	public static final IslandBiome FOREST_CLEARING = new IslandBiomeForestClearing();
	public static final IslandBiome DESERT = new IslandBiomeDesert();
	public static final IslandBiome DESERT_DUNES = new IslandBiomeDesertDunes();
	public static final IslandBiome JUNGLE = new IslandBiomeJungle();
	public static final IslandBiome MESA = new IslandBiomeMesa();
	public static final IslandBiome SAVANNAH = new IslandBiomeSavannah();
	public static final IslandBiome TUNDRA = new IslandBiomeTundra();
	public static final IslandBiome ROOFED_FOREST = new IslandBiomeForestRoofed();
	
}
