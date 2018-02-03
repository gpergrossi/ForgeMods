package com.gpergrossi.aerogen.world;

import java.util.Random;

import com.gpergrossi.aerogen.ui.GuiAerogenWorldSettingsScreen;

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

    /**
     * Gets the translation key for the name of this world type.
     */
    @SideOnly(Side.CLIENT)
    public String getTranslationKey() {
    	// TODO change this back to generator.AEROGEN_SKY and add
    	// the translation to an actual localization file somewhere
    	return "Aerogen Sky";
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
    
    /**
     * Called when the 'Customize' button is pressed on world creation GUI
     * @param mc The Minecraft instance
     * @param guiCreateWorld the createworld GUI
     */
    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(net.minecraft.client.Minecraft mc, net.minecraft.client.gui.GuiCreateWorld guiCreateWorld) {
    	mc.displayGuiScreen(new GuiAerogenWorldSettingsScreen(guiCreateWorld, guiCreateWorld.chunkProviderSettingsJson));
    }

    /**
     * Should world creation GUI show 'Customize' button for this world type?
     * @return if this world type has customization parameters
     */
    public boolean isCustomizable() {
        return true;
    }
    
}
