package com.gpergrossi.aerogen.world;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldTypeSky extends WorldType {
	
	public WorldTypeSky() {
		super("AEROGEN_SKY");
	}
	
    @SideOnly(Side.CLIENT)
    public String getTranslateName() {
        return "AeroGen Sky World";
    }

    @SideOnly(Side.CLIENT)
    public String getTranslatedInfo() {
        return "AeroGen Sky World";
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
    	return new BiomeProviderSky(world);
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        return new ChunkGeneratorSky(world, world.getSeed(), generatorOptions);
    }

    @Override
    public int getMinimumSpawnHeight(World world) {
        return 5;
    }

    @Override
    public double getHorizon(World world) {
        return 0;
    }

    @Override
    public double voidFadeMagnitude() {
    	// How quickly above y=0 that the black atmosphere fades out
        return 5.0;
    }
    
    @Override
    public float getCloudHeight() {
    	// Height to render the clouds for this world type
        return 0.0F;
    }
    
    @Override
    public boolean handleSlimeSpawnReduction(Random random, World world) {
    	// Seems to return true when a slime should not be spawned or maybe when they should despawn
        return false; //random.nextInt(4) != 1 : 
    }
    
}
