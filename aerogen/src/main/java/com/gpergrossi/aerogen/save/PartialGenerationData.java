package com.gpergrossi.aerogen.save;

import com.gpergrossi.aerogen.AeroGenMod;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class PartialGenerationData extends WorldSavedData {

	private static final String DATA_NAME = AeroGenMod.MODID + "PartialGenerationData";

	public static PartialGenerationData forWorld(World world) {
		MapStorage storage = world.getPerWorldStorage();
		PartialGenerationData instance = (PartialGenerationData) storage.getOrLoadData(PartialGenerationData.class, DATA_NAME);
		if (instance == null) {
			instance = new PartialGenerationData(DATA_NAME);
			storage.setData(DATA_NAME, instance);
		}
		return instance;
	}
	
	
	
	public PartialGenerationData(String dataIdentifier) {
		super(dataIdentifier);
	}

	
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
